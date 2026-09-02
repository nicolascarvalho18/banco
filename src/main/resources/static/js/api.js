/**
 * BANCO SAP - CLIENTE REST OFICIAL COM REFRESH TOKEN E GESTÃO DE SESSÃO
 */

const API_BASE = '/api/v1';

class ApiClient {
  constructor() {
    this.accessToken = localStorage.getItem('sap_token');
    this.refreshToken = localStorage.getItem('sap_refresh_token');
    this.currentUser = JSON.parse(localStorage.getItem('sap_user') || 'null');
  }

  setSession(authResponse) {
    this.accessToken = authResponse.accessToken;
    this.refreshToken = authResponse.refreshToken;
    this.currentUser = authResponse.user;

    localStorage.setItem('sap_token', this.accessToken);
    localStorage.setItem('sap_refresh_token', this.refreshToken);
    localStorage.setItem('sap_user', JSON.stringify(this.currentUser));
  }

  clearSession() {
    this.accessToken = null;
    this.refreshToken = null;
    this.currentUser = null;

    localStorage.removeItem('sap_token');
    localStorage.removeItem('sap_refresh_token');
    localStorage.removeItem('sap_user');
  }

  isAuthenticated() {
    return !!this.accessToken && !!this.currentUser;
  }

  async request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    if (this.accessToken) {
      headers['Authorization'] = `Bearer ${this.accessToken}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      // Token expirado - Tentar Refresh Token
      if (response.status === 401 && this.refreshToken && !endpoint.includes('/auth/')) {
        const refreshed = await this.tryRefreshToken();
        if (refreshed) {
          headers['Authorization'] = `Bearer ${this.accessToken}`;
          const retryResponse = await fetch(url, { ...options, headers });
          return this.handleResponse(retryResponse);
        } else {
          this.clearSession();
          window.location.hash = '#login';
          throw new Error('Sua sessão expirou. Por favor, faça login novamente.');
        }
      }

      return this.handleResponse(response);
    } catch (error) {
      console.error(`API Error [${endpoint}]:`, error);
      throw error;
    }
  }

  async handleResponse(response) {
    if (response.status === 204) return null;

    const contentType = response.headers.get('content-type');
    let data;
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else if (contentType && (contentType.includes('text/csv') || contentType.includes('application/octet-stream'))) {
      return await response.blob();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      const message = (data && (data.detail || data.message || data.error)) 
        ? (data.detail || data.message || data.error) 
        : `Erro na requisição (${response.status})`;
      const err = new Error(message);
      err.status = response.status;
      err.data = data;
      throw err;
    }

    return data;
  }

  async tryRefreshToken() {
    try {
      const res = await fetch(`${API_BASE}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: this.refreshToken })
      });

      if (res.ok) {
        const authData = await res.json();
        this.setSession(authData);
        return true;
      }
    } catch (e) {
      console.warn('Falha no refresh token:', e);
    }
    return false;
  }

  // --- Auth Endpoints ---
  login(login, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ login, password })
    });
  }

  register(data) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  forgotPassword(email) {
    return this.request('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email })
    });
  }

  resetPassword(email, code, newPassword) {
    return this.request('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify({ email, code, newPassword })
    });
  }

  // --- Market & Quotes Endpoints ---
  getMarketTickers() {
    return this.request('/market/tickers');
  }

  getMarketTicker(symbol) {
    return this.request(`/market/tickers/${symbol}`);
  }

  getMarketHistory(symbol, timeframe = '24H') {
    return this.request(`/market/history/${symbol}?timeframe=${timeframe}`);
  }

  // --- Portfolio & Balance Endpoints ---
  getPortfolio() {
    return this.request('/portfolio');
  }

  depositSimulatedBrl(amountBrl) {
    // Top-up educacional de saldo em Reais
    return this.request('/accounts/deposit', {
      method: 'POST',
      body: JSON.stringify({ amount: amountBrl, description: 'Depósito Simulado de Testes' })
    });
  }

  // --- Orders Endpoints ---
  executeBuy(symbol, amountBrl, pin, idempotencyKey) {
    return this.request('/orders/buy', {
      method: 'POST',
      body: JSON.stringify({ symbol, amountBrl, pin, idempotencyKey })
    });
  }

  executeSell(symbol, cryptoAmount, pin, idempotencyKey) {
    return this.request('/orders/sell', {
      method: 'POST',
      body: JSON.stringify({ symbol, cryptoAmount, pin, idempotencyKey })
    });
  }

  executeConvert(fromSymbol, toSymbol, fromAmount, pin, idempotencyKey) {
    return this.request('/orders/convert', {
      method: 'POST',
      body: JSON.stringify({ fromSymbol, toSymbol, fromAmount, pin, idempotencyKey })
    });
  }

  getOrders(page = 0, size = 20) {
    return this.request(`/orders?page=${page}&size=${size}`);
  }

  // --- Transfers Endpoints ---
  executeTransfer(recipientIdentifier, symbol, amount, description, pin) {
    return this.request('/transfers/internal', {
      method: 'POST',
      body: JSON.stringify({ recipientIdentifier, symbol, amount, description, pin })
    });
  }

  getTransfers(page = 0, size = 20) {
    return this.request(`/transfers/internal?page=${page}&size=${size}`);
  }

  // --- Ledger & Statement Endpoints ---
  getLedger(asset = null, page = 0, size = 30) {
    const query = asset ? `?asset=${asset}&page=${page}&size=${size}` : `?page=${page}&size=${size}`;
    return this.request(`/ledger${query}`);
  }

  async downloadLedgerCsv() {
    const blob = await this.request('/ledger/export-csv');
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `extrato_livro_razao_${Date.now()}.csv`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    a.remove();
  }

  // --- Profile & User Endpoints ---
  getProfile() {
    return this.request('/users/profile');
  }

  changePassword(currentPassword, newPassword) {
    return this.request('/users/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword })
    });
  }

  setPin(pin) {
    return this.request('/users/set-pin', {
      method: 'POST',
      body: JSON.stringify({ pin })
    });
  }

  // --- Admin Endpoints ---
  getAdminUsers(page = 0, size = 20) {
    return this.request(`/admin/users?page=${page}&size=${size}`);
  }

  toggleUserStatus(userId, status) {
    return this.request(`/admin/users/${userId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    });
  }

  getAuditLogs(page = 0, size = 50) {
    return this.request(`/admin/audit-logs?page=${page}&size=${size}`);
  }
}

window.api = new ApiClient();

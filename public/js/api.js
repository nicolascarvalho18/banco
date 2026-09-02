/**
 * BANCO SAF - CLIENTE REST OFICIAL COM FALLBACK RESILIENTE PARA DEPLOY ESTÁTICO (VERCEL/NETLIFY)
 */

const API_BASE = '/api/v1';

class ApiClient {
  constructor() {
    this.accessToken = localStorage.getItem('saf_token');
    this.refreshToken = localStorage.getItem('saf_refresh_token');
    this.currentUser = JSON.parse(localStorage.getItem('saf_user') || 'null');
    this.isStaticHost = false;
    this.initMockDatabase();
  }

  setSession(authResponse) {
    this.accessToken = authResponse.accessToken;
    this.refreshToken = authResponse.refreshToken;
    this.currentUser = authResponse.user;

    localStorage.setItem('saf_token', this.accessToken);
    localStorage.setItem('saf_refresh_token', this.refreshToken);
    localStorage.setItem('saf_user', JSON.stringify(this.currentUser));
  }

  clearSession() {
    this.accessToken = null;
    this.refreshToken = null;
    this.currentUser = null;

    localStorage.removeItem('saf_token');
    localStorage.removeItem('saf_refresh_token');
    localStorage.removeItem('saf_user');
  }

  isAuthenticated() {
    return !!this.accessToken && !!this.currentUser;
  }

  async request(endpoint, options = {}) {
    // Se estivermos em host estático (ex: Vercel) sem servidor Java ativo
    if (this.isStaticHost) {
      return this.handleStaticHostRequest(endpoint, options);
    }

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

      // Se o servidor retornar 404 para a API, detectamos que o host é puramente estático (Vercel)
      if (response.status === 404 && endpoint.startsWith('/')) {
        console.warn('Backend Java não detectado neste host. Ativando motor cliente autônomo (Vercel Mode)...');
        this.isStaticHost = true;
        return this.handleStaticHostRequest(endpoint, options);
      }

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
      // Falha de rede / CORS (típico de SPA estática sem backend)
      console.warn('Conexão ao backend falhou. Alternando para modo cliente com cotações CoinGecko em tempo real...', error);
      this.isStaticHost = true;
      return this.handleStaticHostRequest(endpoint, options);
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

  // --- Endpoints Oficiais ---
  async login(login, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ login, password })
    });
  }

  async register(data) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async forgotPassword(email) {
    return this.request('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify({ email })
    });
  }

  async changePassword(currentPassword, newPassword) {
    return this.request('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword })
    });
  }

  async setPin(pin) {
    return this.request('/auth/set-pin', {
      method: 'POST',
      body: JSON.stringify({ pin })
    });
  }

  async getPortfolio() {
    return this.request('/portfolio');
  }

  async getMarketTickers() {
    return this.request('/market/tickers');
  }

  async getMarketHistory(symbol, timeframe = '24H') {
    return this.request(`/market/history/${symbol}?timeframe=${timeframe}`);
  }

  async executeBuy(symbol, amountBrl, pin, idempotencyKey) {
    return this.request('/orders/buy', {
      method: 'POST',
      body: JSON.stringify({ symbol, amountBrl, pin, idempotencyKey })
    });
  }

  async executeSell(symbol, cryptoAmount, pin, idempotencyKey) {
    return this.request('/orders/sell', {
      method: 'POST',
      body: JSON.stringify({ symbol, cryptoAmount, pin, idempotencyKey })
    });
  }

  async executeConvert(fromSymbol, toSymbol, fromAmount, pin, idempotencyKey) {
    return this.request('/orders/convert', {
      method: 'POST',
      body: JSON.stringify({ fromSymbol, toSymbol, fromAmount, pin, idempotencyKey })
    });
  }

  async executeTransfer(recipientIdentifier, symbol, amount, description, pin) {
    return this.request('/transfers/internal', {
      method: 'POST',
      body: JSON.stringify({ recipientIdentifier, symbol, amount, description, pin })
    });
  }

  async getLedger(asset = null, page = 0, size = 20) {
    const params = new URLSearchParams({ page, size });
    if (asset) params.append('asset', asset);
    return this.request(`/ledger?${params.toString()}`);
  }

  async downloadLedgerCsv() {
    try {
      const blob = await this.request('/ledger/export/csv');
      const url = window.URL.createObjectURL(blob instanceof Blob ? blob : new Blob([blob], { type: 'text/csv' }));
      const a = document.createElement('a');
      a.href = url;
      a.download = `extrato_saf_${Date.now()}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      console.error('Erro ao exportar CSV:', e);
    }
  }

  async depositSimulatedBrl(amount) {
    return this.request('/account/faucet', {
      method: 'POST',
      body: JSON.stringify({ amount })
    });
  }

  async getAdminUsers(page = 0, size = 20) {
    return this.request(`/admin/users?page=${page}&size=${size}`);
  }

  async toggleUserStatus(userId, status) {
    return this.request(`/admin/users/${userId}/status?status=${status}`, {
      method: 'PATCH'
    });
  }

  // =========================================================================
  // MOTOR AUTÔNOMO CLIENT-SIDE (VERCEL & STATIC CLOUD DEPLOYMENT)
  // Permite funcionamento 100% interativo com cotações CoinGecko reais em cloud estática
  // =========================================================================
  initMockDatabase() {
    if (!localStorage.getItem('saf_mock_initialized')) {
      const users = [
        {
          id: 1,
          fullName: 'Nicolas Carvalho Ferreira',
          username: 'nicolas',
          email: 'cliente@bancosap.com.br',
          cpfMasked: '123.***.***-00',
          phone: '(11) 99999-8888',
          role: 'ROLE_CLIENTE',
          status: 'ATIVO',
          pin: '1234',
          brlBalance: 14850.75,
          assets: {
            BTC: { balance: 0.125, avgPrice: 350000.00 },
            ETH: { balance: 1.85, avgPrice: 15500.00 },
            SOL: { balance: 12.4, avgPrice: 620.00 },
            USDT: { balance: 850.0, avgPrice: 5.45 },
            ADA: { balance: 450.0, avgPrice: 1.95 }
          }
        },
        {
          id: 2,
          fullName: 'Maria Helena Silva',
          username: 'mariasilva',
          email: 'maria.silva@bancosap.com.br',
          cpfMasked: '987.***.***-11',
          phone: '(21) 98888-7777',
          role: 'ROLE_CLIENTE',
          status: 'ATIVO',
          pin: '1234',
          brlBalance: 8320.50,
          assets: {
            BTC: { balance: 0.05, avgPrice: 345000.00 },
            ETH: { balance: 0.50, avgPrice: 15200.00 }
          }
        },
        {
          id: 3,
          fullName: 'Administrador do Sistema',
          username: 'admin',
          email: 'admin@bancosap.com.br',
          cpfMasked: '000.***.***-00',
          phone: '(11) 98888-0000',
          role: 'ROLE_ADMIN',
          status: 'ATIVO',
          pin: '1234',
          brlBalance: 500000.00,
          assets: {}
        }
      ];

      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      localStorage.setItem('saf_mock_ledger', JSON.stringify([
        {
          entryCode: 'af5dcc03-c4fd-4182-a78a-56583a497c59',
          entryType: 'CREDITO',
          assetSymbol: 'ETH',
          amount: '0.03998520',
          balanceAfter: '1.85000000',
          description: 'Compra de Ethereum',
          createdAt: new Date(Date.now() - 3600000).toISOString(),
          transactionReference: 'af5dcc03-c4fd-4182-a78a-56583a497c59'
        },
        {
          entryCode: '42399c30-9cfb-4a17-a4ed-8161b3caa7bc',
          entryType: 'DEBITO',
          assetSymbol: 'BTC',
          amount: '0.01000000',
          balanceAfter: '0.12500000',
          description: 'Conversão de BTC para ETH',
          createdAt: new Date(Date.now() - 7200000).toISOString(),
          transactionReference: '42399c30-9cfb-4a17-a4ed-8161b3caa7bc'
        }
      ]));
      localStorage.setItem('saf_mock_initialized', 'true');
    }
  }

  async handleStaticHostRequest(endpoint, options) {
    const body = options.body ? JSON.parse(options.body) : {};
    const users = JSON.parse(localStorage.getItem('saf_mock_users') || '[]');
    let ledger = JSON.parse(localStorage.getItem('saf_mock_ledger') || '[]');

    // 1. Cotações CoinGecko em tempo real direto do cliente
    if (endpoint.includes('/market/tickers')) {
      try {
        const url = 'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana,binancecoin,ripple,cardano,dogecoin,chainlink,avalanche-2,matic-network,litecoin,polkadot,tether,usd-coin&vs_currencies=brl,usd&include_24hr_vol=true&include_24hr_change=true&include_market_cap=true';
        const res = await fetch(url);
        const data = await res.json();

        const mapGecko = [
          { symbol: 'BTC', name: 'Bitcoin', id: 'bitcoin', cat: 'LAYER1', icon: 'https://cryptologos.cc/logos/bitcoin-btc-logo.svg' },
          { symbol: 'ETH', name: 'Ethereum', id: 'ethereum', cat: 'LAYER1', icon: 'https://cryptologos.cc/logos/ethereum-eth-logo.svg' },
          { symbol: 'SOL', name: 'Solana', id: 'solana', cat: 'LAYER1', icon: 'https://cryptologos.cc/logos/solana-sol-logo.svg' },
          { symbol: 'BNB', name: 'BNB', id: 'binancecoin', cat: 'LAYER1', icon: 'https://cryptologos.cc/logos/bnb-bnb-logo.svg' },
          { symbol: 'XRP', name: 'XRP', id: 'ripple', cat: 'PAYMENT', icon: 'https://cryptologos.cc/logos/xrp-xrp-logo.svg' },
          { symbol: 'ADA', name: 'Cardano', id: 'cardano', cat: 'LAYER1', icon: 'https://cryptologos.cc/logos/cardano-ada-logo.svg' },
          { symbol: 'DOGE', name: 'Dogecoin', id: 'dogecoin', cat: 'MEME', icon: 'https://cryptologos.cc/logos/dogecoin-doge-logo.svg' },
          { symbol: 'LINK', name: 'Chainlink', id: 'chainlink', cat: 'ORACLE', icon: 'https://cryptologos.cc/logos/chainlink-link-logo.svg' },
          { symbol: 'AVAX', name: 'Avalanche', id: 'avalanche-2', cat: 'LAYER1', icon: 'https://cryptologos.cc/logos/avalanche-avax-logo.svg' },
          { symbol: 'MATIC', name: 'Polygon', id: 'matic-network', cat: 'LAYER2', icon: 'https://cryptologos.cc/logos/polygon-matic-logo.svg' },
          { symbol: 'LTC', name: 'Litecoin', id: 'litecoin', cat: 'PAYMENT', icon: 'https://cryptologos.cc/logos/litecoin-ltc-logo.svg' },
          { symbol: 'DOT', name: 'Polkadot', id: 'polkadot', cat: 'LAYER0', icon: 'https://cryptologos.cc/logos/polkadot-new-dot-logo.svg' },
          { symbol: 'USDT', name: 'Tether USD', id: 'tether', cat: 'STABLECOIN', icon: 'https://cryptologos.cc/logos/tether-usdt-logo.svg' },
          { symbol: 'USDC', name: 'USD Coin', id: 'usd-coin', cat: 'STABLECOIN', icon: 'https://cryptologos.cc/logos/usd-coin-usdc-logo.svg' }
        ];

        return mapGecko.map(item => {
          const d = data[item.id] || {};
          return {
            symbol: item.symbol,
            name: item.name,
            priceBrl: d.brl || 100,
            priceUsd: d.usd || 20,
            change1h: 0.0,
            change24h: d.brl_24h_change || 0.0,
            change7d: 0.0,
            volume24hBrl: d.brl_24h_vol || 10000000,
            marketCapBrl: d.brl_market_cap || 500000000,
            category: item.cat,
            iconUrl: item.icon,
            connectionStatus: 'ONLINE'
          };
        });
      } catch (e) {
        return window.marketManager?.tickers || [];
      }
    }

    // 2. Histórico de Preços
    if (endpoint.includes('/market/history/')) {
      const parts = endpoint.split('/');
      const symbol = parts[3].split('?')[0].toUpperCase();
      const currentPrice = window.marketManager.getPriceInBrl(symbol) || 1000;
      const points = 24;
      const prices = [];
      const labels = [];
      let w = currentPrice * 0.98;
      for (let i = 0; i < points - 1; i++) {
        w += (Math.random() - 0.48) * (currentPrice * 0.015);
        prices.push(Math.max(w, 1));
        labels.push(`T-${points - i}`);
      }
      prices.push(currentPrice);
      labels.push('Agora');
      return { symbol, name: symbol, currentPriceBrl: currentPrice, labels, prices };
    }

    // 3. Login
    if (endpoint.includes('/auth/login')) {
      const ident = body.login?.toLowerCase();
      const u = users.find(x => x.username === ident || x.email === ident);
      if (u) {
        return {
          accessToken: 'mock_jwt_' + Date.now(),
          refreshToken: 'mock_refresh_' + Date.now(),
          user: u
        };
      }
      throw new Error('Credenciais inválidas.');
    }

    // 4. Cadastro
    if (endpoint.includes('/auth/register')) {
      const newUser = {
        id: users.length + 1,
        fullName: body.fullName,
        username: body.username,
        email: body.email,
        cpfMasked: body.cpf,
        phone: body.phone,
        role: 'ROLE_CLIENTE',
        status: 'ATIVO',
        pin: '1234',
        brlBalance: 10000.00,
        assets: { BTC: { balance: 0.01, avgPrice: 350000 } }
      };
      users.push(newUser);
      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      return {
        accessToken: 'mock_jwt_' + Date.now(),
        refreshToken: 'mock_refresh_' + Date.now(),
        user: newUser
      };
    }

    // 5. Portfólio
    if (endpoint.includes('/portfolio')) {
      const user = users.find(u => u.id === (this.currentUser?.id || 1)) || users[0];
      let cryptoTotalBrl = 0;
      const assetList = [];

      for (const [sym, info] of Object.entries(user.assets || {})) {
        const ticker = window.marketManager.getTicker(sym);
        const price = ticker ? ticker.priceBrl : info.avgPrice;
        const val = info.balance * price;
        cryptoTotalBrl += val;

        const cost = info.balance * info.avgPrice;
        const pnlBrl = val - cost;
        const pnlPct = cost > 0 ? (pnlBrl / cost) * 100 : 0;

        assetList.push({
          symbol: sym,
          name: ticker?.name || sym,
          iconUrl: ticker?.iconUrl || 'https://cryptologos.cc/logos/bitcoin-btc-logo.svg',
          balance: info.balance.toFixed(6),
          currentPriceBrl: price,
          totalValueBrl: val,
          allocationPercent: 0,
          averagePurchasePrice: info.avgPrice,
          profitLossBrl: pnlBrl,
          profitLossPercent: pnlPct.toFixed(2)
        });
      }

      const totalNetWorth = user.brlBalance + cryptoTotalBrl;
      assetList.forEach(a => {
        a.allocationPercent = totalNetWorth > 0 ? ((a.totalValueBrl / totalNetWorth) * 100).toFixed(2) : 0;
      });

      return {
        totalNetWorthBrl: totalNetWorth,
        brlBalance: user.brlBalance,
        cryptoTotalBrl: cryptoTotalBrl,
        pnl24hBrl: -120.50,
        pnl24hPercent: -1.25,
        walletAddress: '0xSAF' + user.id + '77a9b8C41Fe23Dd091F8301B6d4f9A02e5C81',
        assets: assetList
      };
    }

    // 6. Comprar
    if (endpoint.includes('/orders/buy')) {
      const user = users.find(u => u.id === (this.currentUser?.id || 1));
      if (user.brlBalance < body.amountBrl) throw new Error('Saldo em reais insuficiente.');
      const price = window.marketManager.getPriceInBrl(body.symbol);
      const acquired = (body.amountBrl * 0.9985) / price;

      user.brlBalance -= body.amountBrl;
      if (!user.assets[body.symbol]) user.assets[body.symbol] = { balance: 0, avgPrice: price };
      user.assets[body.symbol].balance += acquired;
      user.assets[body.symbol].avgPrice = price;

      const code = 'auth_' + Math.random().toString(36).substring(2, 10);
      ledger.unshift({
        entryCode: code,
        entryType: 'CREDITO',
        assetSymbol: body.symbol,
        amount: acquired.toFixed(8),
        balanceAfter: user.assets[body.symbol].balance.toFixed(8),
        description: `Compra de ${body.symbol}`,
        createdAt: new Date().toISOString(),
        transactionReference: code
      });

      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      localStorage.setItem('saf_mock_ledger', JSON.stringify(ledger));

      return {
        authenticationCode: code,
        orderType: 'COMPRA',
        symbolFrom: 'BRL',
        symbolTo: body.symbol,
        amountFrom: body.amountBrl,
        amountTo: acquired.toFixed(8),
        createdAt: new Date().toISOString()
      };
    }

    // 7. Vender
    if (endpoint.includes('/orders/sell')) {
      const user = users.find(u => u.id === (this.currentUser?.id || 1));
      const curr = user.assets[body.symbol]?.balance || 0;
      if (curr < body.cryptoAmount) throw new Error('Saldo insuficiente para venda.');

      const price = window.marketManager.getPriceInBrl(body.symbol);
      const brlGained = body.cryptoAmount * price * 0.9985;

      user.assets[body.symbol].balance -= body.cryptoAmount;
      user.brlBalance += brlGained;

      const code = 'auth_' + Math.random().toString(36).substring(2, 10);
      ledger.unshift({
        entryCode: code,
        entryType: 'DEBITO',
        assetSymbol: body.symbol,
        amount: body.cryptoAmount.toFixed(8),
        balanceAfter: user.assets[body.symbol].balance.toFixed(8),
        description: `Venda de ${body.symbol}`,
        createdAt: new Date().toISOString(),
        transactionReference: code
      });

      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      localStorage.setItem('saf_mock_ledger', JSON.stringify(ledger));

      return {
        authenticationCode: code,
        amountTo: brlGained.toFixed(2),
        createdAt: new Date().toISOString()
      };
    }

    // 8. Converter
    if (endpoint.includes('/orders/convert')) {
      const user = users.find(u => u.id === (this.currentUser?.id || 1));
      const curr = user.assets[body.fromSymbol]?.balance || 0;
      if (curr < body.fromAmount) throw new Error('Saldo insuficiente para conversão.');

      const pFrom = window.marketManager.getPriceInBrl(body.fromSymbol);
      const pTo = window.marketManager.getPriceInBrl(body.toSymbol);
      const toAmount = (body.fromAmount * pFrom * 0.9985) / pTo;

      user.assets[body.fromSymbol].balance -= body.fromAmount;
      if (!user.assets[body.toSymbol]) user.assets[body.toSymbol] = { balance: 0, avgPrice: pTo };
      user.assets[body.toSymbol].balance += toAmount;

      const code = 'auth_' + Math.random().toString(36).substring(2, 10);
      ledger.unshift({
        entryCode: code,
        entryType: 'CREDITO',
        assetSymbol: body.toSymbol,
        amount: toAmount.toFixed(8),
        balanceAfter: user.assets[body.toSymbol].balance.toFixed(8),
        description: `Conversão ${body.fromSymbol} → ${body.toSymbol}`,
        createdAt: new Date().toISOString(),
        transactionReference: code
      });

      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      localStorage.setItem('saf_mock_ledger', JSON.stringify(ledger));

      return {
        authenticationCode: code,
        amountTo: toAmount.toFixed(8),
        createdAt: new Date().toISOString()
      };
    }

    // 9. Transferência Interna
    if (endpoint.includes('/transfers/internal')) {
      const sender = users.find(u => u.id === (this.currentUser?.id || 1));
      const targetIdent = body.recipientIdentifier?.replace('@', '').toLowerCase();
      const recipient = users.find(u => u.username === targetIdent || u.email === targetIdent);
      if (!recipient) throw new Error('Destinatário não encontrado.');

      if (body.symbol === 'BRL') {
        if (sender.brlBalance < body.amount) throw new Error('Saldo em Reais insuficiente.');
        sender.brlBalance -= body.amount;
        recipient.brlBalance += body.amount;
      } else {
        const bal = sender.assets[body.symbol]?.balance || 0;
        if (bal < body.amount) throw new Error(`Saldo de ${body.symbol} insuficiente.`);
        sender.assets[body.symbol].balance -= body.amount;
        if (!recipient.assets[body.symbol]) recipient.assets[body.symbol] = { balance: 0, avgPrice: 0 };
        recipient.assets[body.symbol].balance += body.amount;
      }

      const code = 'auth_' + Math.random().toString(36).substring(2, 10);
      ledger.unshift({
        entryCode: code,
        entryType: 'DEBITO',
        assetSymbol: body.symbol,
        amount: body.amount.toString(),
        balanceAfter: body.symbol === 'BRL' ? sender.brlBalance.toFixed(2) : sender.assets[body.symbol].balance.toFixed(8),
        description: `Transferência para @${recipient.username}`,
        createdAt: new Date().toISOString(),
        transactionReference: code
      });

      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      localStorage.setItem('saf_mock_ledger', JSON.stringify(ledger));

      return {
        authenticationCode: code,
        recipientUsername: recipient.username,
        amount: body.amount,
        symbol: body.symbol,
        createdAt: new Date().toISOString()
      };
    }

    // 10. Extrato
    if (endpoint.includes('/ledger')) {
      if (endpoint.includes('/export/csv')) {
        const header = 'Codigo,Tipo,Ativo,Quantidade,SaldoResultante,Descricao,Data\n';
        const rows = ledger.map(l => `${l.entryCode},${l.entryType},${l.assetSymbol},${l.amount},${l.balanceAfter},"${l.description}",${l.createdAt}`).join('\n');
        return new Blob([header + rows], { type: 'text/csv' });
      }
      return { content: ledger, totalElements: ledger.length };
    }

    // 11. Depósito Faucet
    if (endpoint.includes('/account/faucet')) {
      const user = users.find(u => u.id === (this.currentUser?.id || 1));
      user.brlBalance += Number(body.amount || 1000);
      localStorage.setItem('saf_mock_users', JSON.stringify(users));
      return { success: true, newBalance: user.brlBalance };
    }

    // 12. Admin Users
    if (endpoint.includes('/admin/users')) {
      return { content: users, totalElements: users.length };
    }

    return {};
  }
}

window.api = new ApiClient();

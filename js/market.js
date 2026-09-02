/**
 * BANCO SAF • MOTOR DE MERCADO & VISUALIZAÇÃO GRÁFICA INTERATIVA
 */

class MarketManager {
  constructor() {
    this.tickers = [];
    this.tickerMap = new Map();
    this.lastSync = null;
    this.connectionStatus = 'ONLINE';
    this.detailChart = null;
    this.evolutionChart = null;
    this.donutChart = null;
    this.pollInterval = null;
  }

  async init() {
    await this.fetchTickers();
    this.startPolling();
  }

  startPolling() {
    if (this.pollInterval) clearInterval(this.pollInterval);
    this.pollInterval = setInterval(async () => {
      await this.fetchTickers();
      this.updateStatusIndicators();
    }, 20000); // 20s
  }

  stopPolling() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  async fetchTickers() {
    try {
      const data = await window.api.getMarketTickers();
      if (Array.isArray(data)) {
        this.tickers = data;
        this.tickerMap.clear();
        data.forEach(t => this.tickerMap.set(t.symbol.toUpperCase(), t));
        this.lastSync = new Date();
        this.connectionStatus = data[0]?.connectionStatus || 'ONLINE';
        this.updateStatusIndicators();
      }
    } catch (e) {
      console.warn('Erro ao atualizar cotações:', e);
      this.connectionStatus = 'DEGRADED';
      this.updateStatusIndicators();
    }
  }

  getTicker(symbol) {
    return this.tickerMap.get(symbol?.toUpperCase());
  }

  getPriceInBrl(symbol) {
    if (!symbol || symbol.toUpperCase() === 'BRL') return 1;
    const ticker = this.tickerMap.get(symbol.toUpperCase());
    return ticker ? ticker.priceBrl : 0;
  }

  updateStatusIndicators() {
    const isOnline = this.connectionStatus === 'ONLINE';
    const statusTextEl = document.getElementById('headerMarketStatusText');
    if (statusTextEl) {
      const timeStr = this.lastSync ? this.lastSync.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '--:--';
      statusTextEl.textContent = isOnline ? `Mercado Online • ${timeStr}` : `Cache Local • ${timeStr}`;
    }

    const lastSyncEl = document.getElementById('dashboardLastSyncText');
    if (lastSyncEl && this.lastSync) {
      lastSyncEl.textContent = `Atualizado às ${this.lastSync.toLocaleTimeString('pt-BR')}`;
    }
  }

  // --- Gráfico de Evolução Patrimonial (Dashboard) ---
  async renderDashboardEvolutionChart(canvasId, totalWorth, timeframe = '7D') {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (this.evolutionChart) {
      this.evolutionChart.destroy();
    }

    const pointsCount = timeframe === '24H' ? 12 : (timeframe === '7D' ? 14 : (timeframe === '30D' ? 20 : 30));
    const labels = [];
    const prices = [];
    const base = Number(totalWorth || 10000);
    let walker = base * 0.96;

    for (let i = 0; i < pointsCount - 1; i++) {
      walker += (Math.random() - 0.46) * (base * 0.015);
      prices.push(Math.max(walker, base * 0.5));
      labels.push(`P-${pointsCount - i}`);
    }
    prices.push(base);
    labels.push('Hoje');

    const gradient = ctx.createLinearGradient(0, 0, 0, 260);
    gradient.addColorStop(0, 'rgba(37, 99, 235, 0.25)');
    gradient.addColorStop(1, 'rgba(37, 99, 235, 0.0)');

    this.evolutionChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: 'Patrimônio (BRL)',
          data: prices,
          borderColor: '#2563EB',
          borderWidth: 2.5,
          backgroundColor: gradient,
          fill: true,
          tension: 0.35,
          pointRadius: 0,
          pointHoverRadius: 6,
          pointHoverBackgroundColor: '#2563EB',
          pointHoverBorderColor: '#FFFFFF',
          pointHoverBorderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { intersect: false, mode: 'index' },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#162238',
            titleColor: '#94A3B8',
            bodyColor: '#FFFFFF',
            borderColor: 'rgba(255, 255, 255, 0.12)',
            borderWidth: 1,
            padding: 10,
            displayColors: false,
            callbacks: {
              label: (context) => `R$ ${context.parsed.y.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
            }
          }
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#64748B', font: { size: 11 } } },
          y: {
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: {
              color: '#64748B',
              font: { size: 11 },
              callback: (v) => 'R$ ' + Number(v).toLocaleString('pt-BR', { notation: 'compact' })
            }
          }
        }
      }
    });
  }

  // --- Gráfico de Preços Cripto Interativo ---
  async renderInteractiveChart(canvasId, symbol, timeframe = '24H') {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    try {
      const history = await window.api.getMarketHistory(symbol, timeframe);
      if (!history || !history.prices) return;

      if (this.detailChart) {
        this.detailChart.destroy();
      }

      const isPositive = (history.prices[history.prices.length - 1] >= history.prices[0]);
      const primaryColor = isPositive ? '#10B981' : '#EF4444';
      const gradient = ctx.createLinearGradient(0, 0, 0, 360);
      gradient.addColorStop(0, isPositive ? 'rgba(16, 185, 129, 0.28)' : 'rgba(239, 68, 68, 0.28)');
      gradient.addColorStop(1, 'rgba(0, 0, 0, 0)');

      this.detailChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: history.labels,
          datasets: [{
            label: `Preço ${symbol} (BRL)`,
            data: history.prices,
            borderColor: primaryColor,
            borderWidth: 2.5,
            backgroundColor: gradient,
            fill: true,
            tension: 0.35,
            pointRadius: 0,
            pointHoverRadius: 6,
            pointHoverBackgroundColor: primaryColor,
            pointHoverBorderColor: '#FFFFFF',
            pointHoverBorderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          interaction: { intersect: false, mode: 'index' },
          plugins: {
            legend: { display: false },
            tooltip: {
              backgroundColor: '#162238',
              titleColor: '#94A3B8',
              bodyColor: '#FFFFFF',
              borderColor: 'rgba(255, 255, 255, 0.12)',
              borderWidth: 1,
              padding: 10,
              displayColors: false,
              callbacks: {
                label: (context) => `R$ ${context.parsed.y.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 4 })}`
              }
            }
          },
          scales: {
            x: { grid: { display: false }, ticks: { color: '#64748B', font: { size: 11 } } },
            y: {
              grid: { color: 'rgba(255, 255, 255, 0.05)' },
              ticks: {
                color: '#64748B',
                font: { size: 11 },
                callback: (val) => 'R$ ' + Number(val).toLocaleString('pt-BR', { notation: 'compact' })
              }
            }
          }
        }
      });
    } catch (e) {
      console.error('Erro ao renderizar gráfico:', e);
    }
  }

  // --- Formatadores Padronizados ---
  formatBrl(val) {
    return Number(val || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatUsd(val) {
    return Number(val || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
  }

  formatCrypto(val, symbol = '') {
    const num = Number(val || 0);
    const decimals = num < 1 ? 6 : 4;
    return `${num.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: decimals })} ${symbol}`.trim();
  }

  formatChange(val) {
    const num = Number(val || 0);
    const sign = num > 0 ? '+' : '';
    return `${sign}${num.toFixed(2)}%`;
  }
}

window.marketManager = new MarketManager();

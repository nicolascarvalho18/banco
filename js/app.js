/**
 * BANCO SAF • APLICAÇÃO WEB PRINCIPAL (SPA)
 * Controle de Roteamento, Design System, Modais em Etapas e Operações Financeiras
 */

class App {
  constructor() {
    this.currentView = 'dashboard';
    this.privacyMode = false;
    this.selectedCryptoSymbol = 'BTC';
    this.activeTimeframe = '24H';
    this.dashboardTimeframe = '7D';
    this.selectedMarketCategory = 'ALL';
    this.pinResolve = null;
    this.pinReject = null;
    this.currentPinInput = '';
    this.portfolioData = null;
    this.donutChart = null;
  }

  async init() {
    this.bindEvents();
    this.initRouter();
    await window.marketManager.init();
    this.checkSessionAndRoute();
  }

  bindEvents() {
    window.addEventListener('hashchange', () => this.handleRouting());

    // Theme toggle
    const themeBtn = document.getElementById('btnThemeToggle');
    if (themeBtn) {
      themeBtn.addEventListener('click', () => this.toggleTheme());
    }

    // Privacy toggle
    const privacyBtn = document.getElementById('btnPrivacyToggle');
    if (privacyBtn) {
      privacyBtn.addEventListener('click', () => this.togglePrivacy());
    }

    // Mobile menu toggle
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    if (mobileMenuBtn) {
      mobileMenuBtn.addEventListener('click', () => {
        document.querySelector('.sidebar')?.classList.toggle('open');
      });
    }

    // Close modals on backdrop click
    document.querySelectorAll('.modal-backdrop').forEach(modal => {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          this.closeModal(modal.id);
        }
      });
    });
  }

  // --- Router ---
  initRouter() {
    if (!window.location.hash) {
      window.location.hash = window.api.isAuthenticated() ? '#dashboard' : '#login';
    }
  }

  checkSessionAndRoute() {
    if (!window.api.isAuthenticated()) {
      if (window.location.hash !== '#register' && window.location.hash !== '#forgot-password') {
        window.location.hash = '#login';
      }
    }
    this.handleRouting();
  }

  handleRouting() {
    const hash = window.location.hash.replace('#', '') || 'dashboard';
    const isAuth = window.api.isAuthenticated();

    if (!isAuth && !['login', 'register', 'forgot-password'].includes(hash)) {
      window.location.hash = '#login';
      return;
    }

    if (isAuth && ['login', 'register', 'forgot-password'].includes(hash)) {
      window.location.hash = '#dashboard';
      return;
    }

    this.currentView = hash;
    this.updateActiveNavLink(hash);
    this.renderCurrentView();
  }

  updateActiveNavLink(view) {
    document.querySelectorAll('.nav-item').forEach(link => {
      const target = link.getAttribute('data-view');
      link.classList.toggle('active', target === view);
    });

    document.querySelectorAll('.bottom-nav-item').forEach(item => {
      const target = item.getAttribute('data-view');
      item.classList.toggle('active', target === view);
    });

    // Close mobile menu on navigate
    document.querySelector('.sidebar')?.classList.remove('open');
  }

  renderCurrentView() {
    const authContainer = document.getElementById('authContainer');
    const mainApp = document.getElementById('mainApp');

    if (!window.api.isAuthenticated()) {
      if (mainApp) mainApp.style.display = 'none';
      if (authContainer) authContainer.style.display = 'flex';
      this.renderAuthView(this.currentView);
      return;
    }

    if (authContainer) authContainer.style.display = 'none';
    if (mainApp) mainApp.style.display = 'flex';

    this.updateHeaderTitles(this.currentView);
    this.updateUserSidebar();

    // Hide all view sections
    document.querySelectorAll('.view-section').forEach(sec => sec.style.display = 'none');

    // Show active section
    const activeSec = document.getElementById(`view-${this.currentView}`);
    if (activeSec) {
      activeSec.style.display = 'block';
      this.loadViewData(this.currentView);
    } else {
      const dash = document.getElementById('view-dashboard');
      if (dash) {
        dash.style.display = 'block';
        this.loadDashboardData();
      }
    }
  }

  updateHeaderTitles(view) {
    const titles = {
      'dashboard': { title: 'Visão geral', subtitle: 'Painel de patrimônio e cotações em tempo real' },
      'market': { title: 'Mercado', subtitle: 'Cotações de criptoativos sincronizadas com o mercado global' },
      'crypto-detail': { title: `Detalhes do Ativo • ${this.selectedCryptoSymbol}`, subtitle: 'Histórico de cotações, variações e sua posição' },
      'wallet': { title: 'Minha carteira', subtitle: 'Custódia consolidada, alocação e desempenho dos ativos' },
      'statement': { title: 'Extrato', subtitle: 'Histórico contábil e lançamentos imutáveis em ledger' },
      'profile': { title: 'Perfil e segurança', subtitle: 'Informações cadastrais, chaves de segurança e preferências' },
      'admin': { title: 'Painel Executivo', subtitle: 'Monitoramento de integridade, usuários e conformidade' }
    };

    const config = titles[view] || titles['dashboard'];
    const titleEl = document.getElementById('pageDynamicTitle');
    const subEl = document.getElementById('pageDynamicSubtitle');
    if (titleEl) titleEl.textContent = config.title;
    if (subEl) subEl.textContent = config.subtitle;
  }

  updateUserSidebar() {
    const user = window.api.currentUser;
    if (!user) return;

    const firstName = user.fullName ? user.fullName.split(' ')[0] : 'Usuário';
    const miniName = document.getElementById('sidebarUserName');
    if (miniName) miniName.textContent = user.fullName;

    const miniRole = document.getElementById('sidebarUserRole');
    if (miniRole) miniRole.textContent = `@${user.username || 'cliente'}`;

    const miniAvatar = document.getElementById('sidebarUserAvatar');
    if (miniAvatar) miniAvatar.textContent = firstName.charAt(0).toUpperCase();

    const headerAvatar = document.getElementById('headerUserAvatar');
    if (headerAvatar) headerAvatar.textContent = firstName.charAt(0).toUpperCase();

    const adminNav = document.getElementById('navLinkAdmin');
    if (adminNav) {
      adminNav.style.display = user.role === 'ROLE_ADMIN' ? 'flex' : 'none';
    }
  }

  async loadViewData(view) {
    switch (view) {
      case 'dashboard':
        await this.loadDashboardData();
        break;
      case 'market':
        await this.loadMarketData();
        break;
      case 'crypto-detail':
        await this.loadCryptoDetailData(this.selectedCryptoSymbol);
        break;
      case 'wallet':
        await this.loadWalletData();
        break;
      case 'statement':
        await this.loadStatementData();
        break;
      case 'profile':
        await this.loadProfileData();
        break;
      case 'admin':
        await this.loadAdminData();
        break;
    }
  }

  // ==========================================
  // DASHBOARD
  // ==========================================
  async loadDashboardData() {
    try {
      const summary = await window.api.getPortfolio();
      this.portfolioData = summary;

      this.updatePortfolioHero(summary);
      this.renderAssetDonutChart(summary.assets, summary.totalNetWorthBrl);
      this.renderWatchlist(window.marketManager.tickers);
      this.renderRecentActivity();

      // Renderizar gráfico de evolução patrimonial
      window.marketManager.renderDashboardEvolutionChart('dashboardEvolutionChartCanvas', summary.totalNetWorthBrl, this.dashboardTimeframe);
    } catch (e) {
      console.error('Erro no dashboard:', e);
      this.showToast('Erro ao carregar dados do painel.', 'error');
    }
  }

  setDashboardTimeframe(tf) {
    this.dashboardTimeframe = tf;
    document.querySelectorAll('#view-dashboard .tf-btn').forEach(b => {
      b.classList.toggle('active', b.getAttribute('data-tf') === tf);
    });
    if (this.portfolioData) {
      window.marketManager.renderDashboardEvolutionChart('dashboardEvolutionChartCanvas', this.portfolioData.totalNetWorthBrl, tf);
    }
  }

  updatePortfolioHero(data) {
    const mask = this.privacyMode;
    const totalEl = document.getElementById('heroTotalWorth');
    const brlEl = document.getElementById('heroBrlBalance');
    const cryptoEl = document.getElementById('heroCryptoBalance');
    const pnlEl = document.getElementById('heroPnl24h');

    if (totalEl) totalEl.textContent = mask ? '••••••••' : window.marketManager.formatBrl(data.totalNetWorthBrl);
    if (brlEl) brlEl.textContent = mask ? '••••••' : window.marketManager.formatBrl(data.brlBalance);
    if (cryptoEl) cryptoEl.textContent = mask ? '••••••' : window.marketManager.formatBrl(data.cryptoTotalBrl);

    if (pnlEl) {
      const isPositive = Number(data.pnl24hBrl) >= 0;
      pnlEl.className = `badge ${isPositive ? 'badge-gain' : 'badge-loss'}`;
      pnlEl.textContent = mask ? '••••' : `${window.marketManager.formatChange(data.pnl24hPercent)} (${window.marketManager.formatBrl(data.pnl24hBrl)})`;
    }
  }

  renderAssetDonutChart(assets, totalNetWorth) {
    const canvas = document.getElementById('assetDonutChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (this.donutChart) {
      this.donutChart.destroy();
    }

    const filtered = (assets || []).filter(a => Number(a.balance) > 0);
    const labels = filtered.length > 0 ? filtered.map(a => a.symbol) : ['BRL'];
    const values = filtered.length > 0 ? filtered.map(a => Number(a.totalValueBrl)) : [Number(totalNetWorth || 100)];
    const colors = ['#2563EB', '#10B981', '#8B5CF6', '#F59E0B', '#06B6D4', '#EC4899', '#6366F1'];

    this.donutChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: colors.slice(0, labels.length),
          borderWidth: 2,
          borderColor: '#0F172A'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '72%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94A3B8', font: { size: 12 }, boxWidth: 10, padding: 14 }
          },
          tooltip: {
            backgroundColor: '#162238',
            titleColor: '#94A3B8',
            bodyColor: '#FFFFFF',
            borderColor: 'rgba(255, 255, 255, 0.12)',
            borderWidth: 1,
            padding: 10,
            callbacks: {
              label: (c) => `${c.label}: R$ ${Number(c.parsed).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`
            }
          }
        }
      }
    });
  }

  renderWatchlist(tickers) {
    const tbody = document.getElementById('watchlistTableBody');
    if (!tbody) return;

    const items = (tickers || []).slice(0, 5);
    tbody.innerHTML = items.map(t => {
      const isPos = Number(t.change24h) >= 0;
      return `
        <tr style="cursor: pointer;" onclick="app.openCryptoDetail('${t.symbol}')">
          <td>
            <div style="display: flex; align-items: center; gap: 10px;">
              <img src="${t.iconUrl}" style="width: 24px; height: 24px; border-radius: 50%;" alt="${t.symbol}" />
              <div>
                <div style="font-weight: 700; font-size: 0.9rem;">${t.name}</div>
                <div style="font-size: 0.75rem; color: var(--text-muted); font-weight: 600;">${t.symbol}</div>
              </div>
            </div>
          </td>
          <td class="font-mono" style="text-align: right; font-weight: 700;">${window.marketManager.formatBrl(t.priceBrl)}</td>
          <td style="text-align: right;">
            <span class="badge ${isPos ? 'badge-gain' : 'badge-loss'}">
              ${window.marketManager.formatChange(t.change24h)}
            </span>
          </td>
          <td style="text-align: right;">
            <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); app.openBuyModal('${t.symbol}')">
              Negociar
            </button>
          </td>
        </tr>
      `;
    }).join('');
  }

  async renderRecentActivity() {
    const container = document.getElementById('recentActivityList');
    if (!container) return;

    try {
      const res = await window.api.getLedger(null, 0, 5);
      const items = res.content || [];

      if (items.length === 0) {
        container.innerHTML = '<div style="text-align: center; color: var(--text-muted); padding: 32px; font-size: 0.875rem;">Nenhuma atividade recente registrada.</div>';
        return;
      }

      container.innerHTML = items.map(i => {
        const isCredit = i.entryType === 'CREDITO';
        return `
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-subtle);">
            <div>
              <div style="font-weight: 600; font-size: 0.875rem;">${i.description}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted);">${new Date(i.createdAt).toLocaleString('pt-BR')}</div>
            </div>
            <div class="font-mono" style="font-weight: 700; font-size: 0.9rem; color: ${isCredit ? 'var(--color-gain)' : 'var(--color-loss)'};">
              ${isCredit ? '+' : '-'}${i.amount} ${i.assetSymbol}
            </div>
          </div>
        `;
      }).join('');
    } catch (e) {
      console.error(e);
    }
  }

  // ==========================================
  // MERCADO
  // ==========================================
  async loadMarketData() {
    const tickers = window.marketManager.tickers;
    this.renderMarketTable(tickers);
    this.renderMarketHighlights(tickers);

    const searchInput = document.getElementById('marketSearchInput');
    if (searchInput) {
      searchInput.oninput = (e) => {
        const q = e.target.value.toLowerCase();
        const filtered = tickers.filter(t => {
          const matchCat = (this.selectedMarketCategory === 'ALL' || t.category === this.selectedMarketCategory);
          const matchQuery = t.name.toLowerCase().includes(q) || t.symbol.toLowerCase().includes(q);
          return matchCat && matchQuery;
        });
        this.renderMarketTable(filtered);
      };
    }
  }

  filterMarketCategory(category, btn) {
    this.selectedMarketCategory = category;
    document.querySelectorAll('#view-market .btn-secondary').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');

    const searchInput = document.getElementById('marketSearchInput');
    const q = searchInput ? searchInput.value.toLowerCase() : '';
    const tickers = window.marketManager.tickers;

    const filtered = tickers.filter(t => {
      const matchCat = (category === 'ALL' || t.category === category);
      const matchQuery = t.name.toLowerCase().includes(q) || t.symbol.toLowerCase().includes(q);
      return matchCat && matchQuery;
    });

    this.renderMarketTable(filtered);
  }

  renderMarketHighlights(tickers) {
    if (!tickers || tickers.length === 0) return;

    const sorted = [...tickers].sort((a, b) => Number(b.change24h) - Number(a.change24h));
    const topGainer = sorted[0];
    const topLoser = sorted[sorted.length - 1];

    const gainerEl = document.getElementById('highlightTopGainer');
    if (gainerEl && topGainer) {
      gainerEl.innerHTML = `
        <div style="font-size: var(--font-size-xs); text-transform: uppercase; color: var(--color-gain); font-weight: 700; margin-bottom: 8px;">Maior Alta do Dia</div>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <img src="${topGainer.iconUrl}" style="width: 32px; height: 32px; border-radius: 50%;" />
            <div>
              <div style="font-weight: 700;">${topGainer.name}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted);">${topGainer.symbol}</div>
            </div>
          </div>
          <div style="text-align: right;">
            <div class="font-mono" style="font-size: 1.15rem; font-weight: 800;">${window.marketManager.formatBrl(topGainer.priceBrl)}</div>
            <span class="badge badge-gain">${window.marketManager.formatChange(topGainer.change24h)}</span>
          </div>
        </div>
      `;
    }

    const loserEl = document.getElementById('highlightTopLoser');
    if (loserEl && topLoser) {
      loserEl.innerHTML = `
        <div style="font-size: var(--font-size-xs); text-transform: uppercase; color: var(--color-loss); font-weight: 700; margin-bottom: 8px;">Maior Queda do Dia</div>
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <img src="${topLoser.iconUrl}" style="width: 32px; height: 32px; border-radius: 50%;" />
            <div>
              <div style="font-weight: 700;">${topLoser.name}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted);">${topLoser.symbol}</div>
            </div>
          </div>
          <div style="text-align: right;">
            <div class="font-mono" style="font-size: 1.15rem; font-weight: 800;">${window.marketManager.formatBrl(topLoser.priceBrl)}</div>
            <span class="badge badge-loss">${window.marketManager.formatChange(topLoser.change24h)}</span>
          </div>
        </div>
      `;
    }
  }

  renderMarketTable(tickers) {
    const tbody = document.getElementById('marketFullTableBody');
    if (!tbody) return;

    tbody.innerHTML = (tickers || []).map((t, idx) => {
      const isPos = Number(t.change24h) >= 0;
      return `
        <tr style="cursor: pointer;" onclick="app.openCryptoDetail('${t.symbol}')">
          <td style="color: var(--text-muted); font-weight: 600; width: 40px;">#${idx + 1}</td>
          <td>
            <div style="display: flex; align-items: center; gap: 10px;">
              <img src="${t.iconUrl}" style="width: 28px; height: 28px; border-radius: 50%;" alt="${t.symbol}" />
              <div>
                <div style="font-weight: 700;">${t.name}</div>
                <div style="font-size: 0.75rem; color: var(--text-muted);">${t.symbol} • ${t.category}</div>
              </div>
            </div>
          </td>
          <td class="font-mono" style="text-align: right; font-weight: 800;">${window.marketManager.formatBrl(t.priceBrl)}</td>
          <td class="font-mono" style="text-align: right; color: var(--text-muted);">${window.marketManager.formatUsd(t.priceUsd)}</td>
          <td style="text-align: right;">
            <span class="badge ${isPos ? 'badge-gain' : 'badge-loss'}">
              ${window.marketManager.formatChange(t.change24h)}
            </span>
          </td>
          <td class="font-mono" style="text-align: right; color: var(--text-secondary);">${window.marketManager.formatBrl(t.volume24hBrl)}</td>
          <td class="font-mono" style="text-align: right; color: var(--text-secondary);">${window.marketManager.formatBrl(t.marketCapBrl)}</td>
          <td style="text-align: center;">
            <div style="display: inline-flex; gap: 6px;">
              <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); app.openBuyModal('${t.symbol}')">Comprar</button>
              <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); app.openSellModal('${t.symbol}')">Vender</button>
            </div>
          </td>
        </tr>
      `;
    }).join('');
  }

  // ==========================================
  // DETALHES DA MOEDA
  // ==========================================
  openCryptoDetail(symbol) {
    this.selectedCryptoSymbol = symbol;
    window.location.hash = '#crypto-detail';
  }

  async loadCryptoDetailData(symbol) {
    const ticker = window.marketManager.getTicker(symbol);
    if (!ticker) return;

    document.getElementById('detailCoinName').textContent = ticker.name;
    document.getElementById('detailCoinSymbol').textContent = ticker.symbol;
    document.getElementById('detailCoinLogo').src = ticker.iconUrl;
    document.getElementById('detailCoinPriceBrl').textContent = window.marketManager.formatBrl(ticker.priceBrl);
    document.getElementById('detailCoinPriceUsd').textContent = window.marketManager.formatUsd(ticker.priceUsd);

    const isPos = Number(ticker.change24h) >= 0;
    const badge = document.getElementById('detailCoinChangeBadge');
    badge.className = `badge ${isPos ? 'badge-gain' : 'badge-loss'}`;
    badge.textContent = window.marketManager.formatChange(ticker.change24h);

    document.getElementById('detailStatHigh24h').textContent = window.marketManager.formatBrl(ticker.high24hBrl || (ticker.priceBrl * 1.03));
    document.getElementById('detailStatLow24h').textContent = window.marketManager.formatBrl(ticker.low24hBrl || (ticker.priceBrl * 0.97));
    document.getElementById('detailStatVolume').textContent = window.marketManager.formatBrl(ticker.volume24hBrl);
    document.getElementById('detailStatMarketCap').textContent = window.marketManager.formatBrl(ticker.marketCapBrl);

    // Carregar posição do usuário
    const summary = await window.api.getPortfolio();
    const userAsset = (summary.assets || []).find(a => a.symbol.toUpperCase() === symbol.toUpperCase());
    const holdingAmt = userAsset ? Number(userAsset.balance) : 0;
    const holdingBrl = userAsset ? Number(userAsset.totalValueBrl) : 0;
    const avgPrice = userAsset ? Number(userAsset.averagePurchasePrice) : 0;
    const pnlBrl = userAsset ? Number(userAsset.profitLossBrl) : 0;

    document.getElementById('detailUserHoldingAmount').textContent = `${holdingAmt.toFixed(6)} ${symbol}`;
    document.getElementById('detailUserHoldingBrl').textContent = window.marketManager.formatBrl(holdingBrl);
    document.getElementById('detailUserAvgPrice').textContent = window.marketManager.formatBrl(avgPrice);

    const pnlBadge = document.getElementById('detailUserPnlBadge');
    const isPnlPos = pnlBrl >= 0;
    pnlBadge.className = `badge ${isPnlPos ? 'badge-gain' : 'badge-loss'}`;
    pnlBadge.textContent = `${isPnlPos ? '+' : ''}${window.marketManager.formatBrl(pnlBrl)}`;

    await window.marketManager.renderInteractiveChart('cryptoDetailChartCanvas', symbol, this.activeTimeframe);
  }

  setTimeframe(tf) {
    this.activeTimeframe = tf;
    document.querySelectorAll('#view-crypto-detail .tf-btn').forEach(btn => {
      btn.classList.toggle('active', btn.getAttribute('data-tf') === tf);
    });
    window.marketManager.renderInteractiveChart('cryptoDetailChartCanvas', this.selectedCryptoSymbol, tf);
  }

  // ==========================================
  // CARTEIRA
  // ==========================================
  async loadWalletData() {
    try {
      const summary = await window.api.getPortfolio();
      document.getElementById('walletTotalWorth').textContent = window.marketManager.formatBrl(summary.totalNetWorthBrl);
      document.getElementById('walletBrlBalance').textContent = window.marketManager.formatBrl(summary.brlBalance);
      document.getElementById('walletCryptoTotal').textContent = window.marketManager.formatBrl(summary.cryptoTotalBrl);
      document.getElementById('walletPublicAddress').textContent = summary.walletAddress;

      const tbody = document.getElementById('walletAssetsTableBody');
      if (!tbody) return;

      tbody.innerHTML = (summary.assets || []).map(a => {
        const isPnlPos = Number(a.profitLossBrl) >= 0;
        return `
          <tr>
            <td>
              <div style="display: flex; align-items: center; gap: 10px;">
                <img src="${a.iconUrl}" style="width: 28px; height: 28px; border-radius: 50%;" />
                <div>
                  <div style="font-weight: 700;">${a.name}</div>
                  <div style="font-size: 0.75rem; color: var(--text-muted);">${a.symbol}</div>
                </div>
              </div>
            </td>
            <td class="font-mono" style="text-align: right; font-weight: 700;">${a.balance} ${a.symbol}</td>
            <td class="font-mono" style="text-align: right;">${window.marketManager.formatBrl(a.currentPriceBrl)}</td>
            <td class="font-mono" style="text-align: right; font-weight: 800;">${window.marketManager.formatBrl(a.totalValueBrl)}</td>
            <td class="font-mono" style="text-align: right; color: var(--text-secondary);">${window.marketManager.formatBrl(a.averagePurchasePrice)}</td>
            <td class="font-mono" style="text-align: right; font-weight: 700; color: ${isPnlPos ? 'var(--color-gain)' : 'var(--color-loss)'};">
              ${isPnlPos ? '+' : ''}${window.marketManager.formatBrl(a.profitLossBrl)} (${a.profitLossPercent}%)
            </td>
            <td style="text-align: right;">
              <div style="font-weight: 700;">${a.allocationPercent}%</div>
              <div style="height: 4px; width: 60px; background: var(--border-subtle); border-radius: 2px; overflow: hidden; margin: 4px 0 0 auto;">
                <div style="height: 100%; width: ${a.allocationPercent}%; background: var(--primary-blue);"></div>
              </div>
            </td>
            <td style="text-align: center;">
              <div style="display: inline-flex; gap: 6px;">
                <button class="btn btn-secondary btn-sm" onclick="app.openBuyModal('${a.symbol}')">Comprar</button>
                <button class="btn btn-secondary btn-sm" onclick="app.openSellModal('${a.symbol}')">Vender</button>
                <button class="btn btn-secondary btn-sm" onclick="app.openConvertModal('${a.symbol}')">Converter</button>
              </div>
            </td>
          </tr>
        `;
      }).join('');
    } catch (e) {
      console.error(e);
    }
  }

  // ==========================================
  // EXTRATO
  // ==========================================
  async loadStatementData(asset = null) {
    try {
      const res = await window.api.getLedger(asset, 0, 50);
      const items = res.content || [];
      const tbody = document.getElementById('statementTableBody');
      if (!tbody) return;

      // Calcular Resumo
      let totalIn = 0;
      let totalOut = 0;
      items.forEach(i => {
        const amt = Number(i.amount || 0);
        if (i.entryType === 'CREDITO') totalIn += amt;
        else totalOut += amt;
      });

      document.getElementById('statementTotalInflows').textContent = `+ ${totalIn.toFixed(4)} itens`;
      document.getElementById('statementTotalOutflows').textContent = `- ${totalOut.toFixed(4)} itens`;
      document.getElementById('statementTotalCount').textContent = `${items.length} lançamentos`;

      if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 36px;">Nenhuma movimentação registrada no livro razão.</td></tr>';
        return;
      }

      tbody.innerHTML = items.map(l => {
        const isCredit = l.entryType === 'CREDITO';
        return `
          <tr>
            <td class="font-mono" style="font-size: 0.75rem; color: var(--text-muted);">
              ${l.entryCode.substring(0, 8)}...
            </td>
            <td style="font-size: 0.85rem;">${new Date(l.createdAt).toLocaleString('pt-BR')}</td>
            <td>
              <span class="badge ${isCredit ? 'badge-gain' : 'badge-loss'}">
                ${l.entryType}
              </span>
            </td>
            <td style="font-weight: 600;">${l.description}</td>
            <td class="font-mono" style="text-align: right; font-weight: 700; color: ${isCredit ? 'var(--color-gain)' : 'var(--color-loss)'};">
              ${isCredit ? '+' : '-'}${l.amount} ${l.assetSymbol}
            </td>
            <td class="font-mono" style="text-align: right; color: var(--text-muted);">${l.balanceAfter} ${l.assetSymbol}</td>
            <td style="text-align: center;">
              <button class="btn btn-secondary btn-sm" onclick="app.openReceiptModal('${l.transactionReference}', '${l.description}', '${l.amount} ${l.assetSymbol}', '${l.createdAt}')">
                Comprovante
              </button>
            </td>
          </tr>
        `;
      }).join('');
    } catch (e) {
      console.error(e);
    }
  }

  // ==========================================
  // PERFIL (5 ABAS)
  // ==========================================
  async loadProfileData() {
    const user = window.api.currentUser;
    if (!user) return;

    document.getElementById('profileFullName').textContent = user.fullName;
    document.getElementById('profileUsername').textContent = `@${user.username || 'cliente'}`;
    document.getElementById('profileEmail').textContent = user.email;
    document.getElementById('profileCpf').textContent = user.cpfMasked;
    document.getElementById('profilePhone').textContent = user.phone;
    document.getElementById('profileRoleBadge').textContent = user.role === 'ROLE_ADMIN' ? 'Administrador' : 'Cliente';
    document.getElementById('profileStatusBadge').textContent = 'Conta ativa';
  }

  switchProfileTab(tabName, btn) {
    document.querySelectorAll('.tabs-nav .tab-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');

    document.querySelectorAll('.profile-tab-content').forEach(c => c.style.display = 'none');
    const target = document.getElementById(`tabContent-${tabName}`);
    if (target) target.style.display = 'block';
  }

  // ==========================================
  // PAINEL ADMIN
  // ==========================================
  async loadAdminData() {
    if (window.api.currentUser?.role !== 'ROLE_ADMIN') {
      window.location.hash = '#dashboard';
      return;
    }

    try {
      const usersRes = await window.api.getAdminUsers(0, 20);
      const users = usersRes.content || [];

      const tbody = document.getElementById('adminUsersTableBody');
      if (tbody) {
        tbody.innerHTML = users.map(u => `
          <tr>
            <td><strong>${u.fullName}</strong></td>
            <td>@${u.username || 'n/a'}</td>
            <td>${u.email}</td>
            <td>${u.cpfMasked}</td>
            <td><span class="badge ${u.status === 'ATIVO' ? 'badge-gain' : 'badge-loss'}">${u.status === 'ATIVO' ? 'Ativo' : 'Bloqueado'}</span></td>
            <td style="text-align: center;">
              <button class="btn btn-secondary btn-sm" onclick="app.toggleUserStatus(${u.id}, '${u.status === 'ATIVO' ? 'BLOQUEADO' : 'ATIVO'}')">
                ${u.status === 'ATIVO' ? 'Bloquear' : 'Desbloquear'}
              </button>
            </td>
          </tr>
        `).join('');
      }

      const statusEl = document.getElementById('adminMarketApiStatus');
      if (statusEl) statusEl.textContent = window.marketManager.connectionStatus;
      const lastSyncEl = document.getElementById('adminMarketLastSync');
      if (lastSyncEl) lastSyncEl.textContent = window.marketManager.lastSync?.toLocaleString('pt-BR') || 'N/A';
    } catch (e) {
      console.error(e);
    }
  }

  async toggleUserStatus(userId, newStatus) {
    try {
      await window.api.toggleUserStatus(userId, newStatus);
      this.showToast(`Status atualizado para ${newStatus}.`, 'success');
      this.loadAdminData();
    } catch (e) {
      this.showToast(e.message, 'error');
    }
  }

  // ==========================================
  // OPERAÇÕES & MODAIS
  // ==========================================
  promptPin(title = 'Confirmação de Segurança', subtitle = 'Digite seu PIN de 4 dígitos para autorizar a operação.') {
    return new Promise((resolve, reject) => {
      this.pinResolve = resolve;
      this.pinReject = reject;
      this.currentPinInput = '';
      this.updatePinDots();

      document.getElementById('pinModalTitle').textContent = title;
      document.getElementById('pinModalSubtitle').textContent = subtitle;
      this.openModal('modalPinPad');
    });
  }

  pinKeyPress(digit) {
    if (this.currentPinInput.length < 4) {
      this.currentPinInput += digit;
      this.updatePinDots();

      if (this.currentPinInput.length === 4) {
        setTimeout(() => {
          const pin = this.currentPinInput;
          this.closeModal('modalPinPad');
          if (this.pinResolve) this.pinResolve(pin);
        }, 150);
      }
    }
  }

  pinBackspace() {
    if (this.currentPinInput.length > 0) {
      this.currentPinInput = this.currentPinInput.slice(0, -1);
      this.updatePinDots();
    }
  }

  updatePinDots() {
    const dots = document.querySelectorAll('.pin-dot');
    dots.forEach((dot, idx) => {
      dot.classList.toggle('filled', idx < this.currentPinInput.length);
    });
  }

  // Compra
  openBuyModal(symbol = 'BTC') {
    this.selectedCryptoSymbol = symbol;
    const ticker = window.marketManager.getTicker(symbol);

    document.getElementById('buyModalSymbol').textContent = symbol;
    document.getElementById('buyModalCoinName').textContent = ticker?.name || symbol;
    document.getElementById('buyModalCurrentPrice').textContent = window.marketManager.formatBrl(ticker?.priceBrl);
    document.getElementById('buyInputAmountBrl').value = '100.00';
    this.recalculateBuyEst();

    this.openModal('modalBuyCrypto');
  }

  recalculateBuyEst() {
    const amountBrl = Number(document.getElementById('buyInputAmountBrl')?.value || 0);
    const unitPrice = window.marketManager.getPriceInBrl(this.selectedCryptoSymbol);
    const fee = amountBrl * 0.0015;
    const net = amountBrl - fee;
    const estCrypto = unitPrice > 0 ? (net / unitPrice).toFixed(8) : '0.00000000';

    document.getElementById('buyEstFee').textContent = window.marketManager.formatBrl(fee);
    document.getElementById('buyEstCryptoAcquired').textContent = `${estCrypto} ${this.selectedCryptoSymbol}`;
  }

  async submitBuy() {
    const amountBrl = Number(document.getElementById('buyInputAmountBrl').value);
    if (!amountBrl || amountBrl < 1) {
      this.showToast('Informe um valor mínimo de R$ 1,00.', 'warning');
      return;
    }

    try {
      const pin = await this.promptPin('Autorizar Compra', `Comprar ${this.selectedCryptoSymbol} no valor de ${window.marketManager.formatBrl(amountBrl)}`);
      this.closeModal('modalBuyCrypto');

      const res = await window.api.executeBuy(this.selectedCryptoSymbol, amountBrl, pin, `buy_${Date.now()}`);
      this.showToast(`Compra de ${this.selectedCryptoSymbol} executada com sucesso!`, 'success');
      this.openReceiptModal(res.authenticationCode, `Compra de ${this.selectedCryptoSymbol}`, `${res.amountTo} ${this.selectedCryptoSymbol}`, res.createdAt);
      this.loadDashboardData();
    } catch (e) {
      if (e) this.showToast(e.message || 'Operação cancelada.', 'error');
    }
  }

  // Venda
  openSellModal(symbol = 'BTC') {
    this.selectedCryptoSymbol = symbol;
    const ticker = window.marketManager.getTicker(symbol);

    document.getElementById('sellModalSymbol').textContent = symbol;
    document.getElementById('sellModalCoinName').textContent = ticker?.name || symbol;
    document.getElementById('sellModalCurrentPrice').textContent = window.marketManager.formatBrl(ticker?.priceBrl);
    document.getElementById('sellInputCryptoAmount').value = '0.001';
    this.recalculateSellEst();

    this.openModal('modalSellCrypto');
  }

  recalculateSellEst() {
    const cryptoAmt = Number(document.getElementById('sellInputCryptoAmount')?.value || 0);
    const unitPrice = window.marketManager.getPriceInBrl(this.selectedCryptoSymbol);
    const gross = cryptoAmt * unitPrice;
    const fee = gross * 0.0015;
    const net = gross - fee;

    document.getElementById('sellEstFee').textContent = window.marketManager.formatBrl(fee);
    document.getElementById('sellEstBrlReceived').textContent = window.marketManager.formatBrl(net);
  }

  async submitSell() {
    const cryptoAmt = Number(document.getElementById('sellInputCryptoAmount').value);
    if (!cryptoAmt || cryptoAmt <= 0) {
      this.showToast('Informe a quantidade de criptoativo.', 'warning');
      return;
    }

    try {
      const pin = await this.promptPin('Autorizar Venda', `Vender ${cryptoAmt} ${this.selectedCryptoSymbol}`);
      this.closeModal('modalSellCrypto');

      const res = await window.api.executeSell(this.selectedCryptoSymbol, cryptoAmt, pin, `sell_${Date.now()}`);
      this.showToast(`Venda executada com sucesso!`, 'success');
      this.openReceiptModal(res.authenticationCode, `Venda de ${this.selectedCryptoSymbol}`, `R$ ${res.amountTo}`, res.createdAt);
      this.loadDashboardData();
    } catch (e) {
      if (e) this.showToast(e.message || 'Operação cancelada.', 'error');
    }
  }

  // Conversão
  openConvertModal(fromSymbol = 'BTC') {
    const fromSelect = document.getElementById('convertFromSelect');
    const toSelect = document.getElementById('convertToSelect');

    const symbols = ['BTC', 'ETH', 'SOL', 'USDT', 'BNB', 'ADA', 'XRP', 'LINK', 'AVAX', 'MATIC', 'LTC', 'DOT'];
    fromSelect.innerHTML = symbols.map(s => `<option value="${s}" ${s === fromSymbol ? 'selected' : ''}>${s}</option>`).join('');
    toSelect.innerHTML = symbols.map(s => `<option value="${s}" ${s === 'ETH' && fromSymbol !== 'ETH' ? 'selected' : (s === 'BTC' ? 'selected' : '')}>${s}</option>`).join('');

    this.recalculateConvertEst();
    this.openModal('modalConvertCrypto');
  }

  recalculateConvertEst() {
    const from = document.getElementById('convertFromSelect')?.value || 'BTC';
    const to = document.getElementById('convertToSelect')?.value || 'ETH';
    const amt = Number(document.getElementById('convertFromAmount')?.value || 0);

    const priceFrom = window.marketManager.getPriceInBrl(from);
    const priceTo = window.marketManager.getPriceInBrl(to);

    const grossBrl = amt * priceFrom;
    const feeBrl = grossBrl * 0.0015;
    const netBrl = grossBrl - feeBrl;
    const estTo = priceTo > 0 ? (netBrl / priceTo).toFixed(8) : '0.00000000';

    document.getElementById('convertEstToAmount').textContent = `${estTo} ${to}`;
    document.getElementById('convertEstFee').textContent = window.marketManager.formatBrl(feeBrl);
  }

  async submitConvert() {
    const from = document.getElementById('convertFromSelect').value;
    const to = document.getElementById('convertToSelect').value;
    const amt = Number(document.getElementById('convertFromAmount').value);

    if (!amt || amt <= 0) {
      this.showToast('Informe a quantidade de origem.', 'warning');
      return;
    }

    try {
      const pin = await this.promptPin('Autorizar Conversão', `Converter ${amt} ${from} em ${to}`);
      this.closeModal('modalConvertCrypto');

      const res = await window.api.executeConvert(from, to, amt, pin, `conv_${Date.now()}`);
      this.showToast(`Conversão de ${from} para ${to} concluída!`, 'success');
      this.openReceiptModal(res.authenticationCode, `Conversão ${from} → ${to}`, `${res.amountTo} ${to}`, res.createdAt);
      this.loadDashboardData();
    } catch (e) {
      if (e) this.showToast(e.message || 'Operação cancelada.', 'error');
    }
  }

  // Transferência
  openTransferModal() {
    this.openModal('modalInternalTransfer');
  }

  async submitTransfer() {
    const recipient = document.getElementById('transferRecipientInput').value.trim();
    const symbol = document.getElementById('transferAssetSelect').value;
    const amount = Number(document.getElementById('transferAmountInput').value);
    const desc = document.getElementById('transferDescInput').value.trim();

    if (!recipient) {
      this.showToast('Informe o @username ou e-mail do destinatário.', 'warning');
      return;
    }
    if (!amount || amount <= 0) {
      this.showToast('Informe a quantidade a transferir.', 'warning');
      return;
    }

    try {
      const pin = await this.promptPin('Autorizar Transferência', `Enviar ${amount} ${symbol} para ${recipient}`);
      this.closeModal('modalInternalTransfer');

      const res = await window.api.executeTransfer(recipient, symbol, amount, desc, pin);
      this.showToast(`Transferência enviada para ${recipient}!`, 'success');
      this.openReceiptModal(res.authenticationCode, `Transferência para @${res.recipientUsername}`, `${res.amount} ${res.symbol}`, res.createdAt);
      this.loadDashboardData();
    } catch (e) {
      if (e) this.showToast(e.message || 'Operação cancelada.', 'error');
    }
  }

  // Receber
  openReceiveModal() {
    const user = window.api.currentUser;
    if (!user) return;

    document.getElementById('receiveUsernameDisplay').textContent = `@${user.username || 'usuario'}`;
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${encodeURIComponent(`saf://${user.username}`)}`;
    document.getElementById('receiveQrImg').src = qrUrl;

    this.openModal('modalReceiveCrypto');
  }

  copyReceiveUsername() {
    const user = window.api.currentUser;
    if (user?.username) {
      navigator.clipboard.writeText(`@${user.username}`);
      this.showToast('Identificador @' + user.username + ' copiado!', 'success');
    }
  }

  // Depósito Simulado
  openDepositModal() {
    this.openModal('modalDepositSimulated');
  }

  async submitDepositSimulated() {
    const amt = Number(document.getElementById('depositSimulatedAmountInput').value);
    if (!amt || amt < 10) {
      this.showToast('Informe no mínimo R$ 10,00.', 'warning');
      return;
    }

    try {
      await window.api.depositSimulatedBrl(amt);
      this.closeModal('modalDepositSimulated');
      this.showToast(`Depósito de ${window.marketManager.formatBrl(amt)} adicionado ao seu saldo!`, 'success');
      this.loadDashboardData();
    } catch (e) {
      this.showToast(e.message, 'error');
    }
  }

  // Comprovante
  openReceiptModal(authCode, operation, value, date) {
    document.getElementById('receiptAuthCode').textContent = authCode || '0000-0000-0000-0000';
    document.getElementById('receiptOperationTitle').textContent = operation || 'Operação Financeira';
    document.getElementById('receiptValue').textContent = value || 'R$ 0,00';
    document.getElementById('receiptDate').textContent = date ? new Date(date).toLocaleString('pt-BR') : new Date().toLocaleString('pt-BR');

    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${encodeURIComponent(`https://bancosaf.com.br/validar/${authCode}`)}`;
    document.getElementById('receiptQrImg').src = qrUrl;

    this.openModal('modalReceipt');
  }

  printReceipt() {
    window.print();
  }

  openModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.add('active');
  }

  closeModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.remove('active');
    if (id === 'modalPinPad' && this.pinReject) {
      this.pinReject(new Error('PIN cancelado pelo usuário.'));
      this.pinResolve = null;
      this.pinReject = null;
    }
  }

  renderAuthView(view) {
    document.getElementById('authCardLogin').style.display = view === 'login' ? 'block' : 'none';
    document.getElementById('authCardRegister').style.display = view === 'register' ? 'block' : 'none';
    document.getElementById('authCardForgot').style.display = view === 'forgot-password' ? 'block' : 'none';
  }

  async submitLogin(e) {
    e.preventDefault();
    const loginInput = document.getElementById('loginInput').value.trim();
    const passwordInput = document.getElementById('passwordInput').value;

    try {
      const authRes = await window.api.login(loginInput, passwordInput);
      window.api.setSession(authRes);
      this.showToast('Login efetuado com sucesso!', 'success');
      window.location.hash = '#dashboard';
    } catch (err) {
      this.showToast(err.message || 'Falha ao realizar login.', 'error');
    }
  }

  async submitRegister(e) {
    e.preventDefault();
    const fullName = document.getElementById('regFullName').value.trim();
    const username = document.getElementById('regUsername').value.trim().toLowerCase();
    const cpf = document.getElementById('regCpf').value.trim();
    const birthDate = document.getElementById('regBirthDate').value;
    const phone = document.getElementById('regPhone').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;
    const termsAccepted = document.getElementById('regTerms').checked;

    if (password !== confirmPassword) {
      this.showToast('As senhas não coincidem.', 'warning');
      return;
    }

    try {
      const authRes = await window.api.register({
        fullName, username, cpf, birthDate, phone, email, password, confirmPassword, termsAccepted
      });
      window.api.setSession(authRes);
      this.showToast('Conta criada com sucesso! Seja bem-vindo ao Banco SAF.', 'success');
      window.location.hash = '#dashboard';
    } catch (err) {
      this.showToast(err.message || 'Erro ao realizar cadastro.', 'error');
    }
  }

  logout() {
    window.api.clearSession();
    this.showToast('Sessão encerrada com sucesso.', 'info');
    window.location.hash = '#login';
  }

  togglePrivacy() {
    this.privacyMode = !this.privacyMode;
    const eyeIcon = document.getElementById('privacyEyeIcon');
    if (eyeIcon) {
      eyeIcon.innerHTML = this.privacyMode
        ? '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>'
        : '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
    }
    if (this.portfolioData) {
      this.updatePortfolioHero(this.portfolioData);
    }
  }

  toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('saf_theme', next);
  }

  togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
  }

  showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast-msg toast-${type}`;
    toast.innerHTML = `
      <div style="flex: 1; font-weight: 500;">${message}</div>
      <button style="background: transparent; border: none; color: var(--text-muted); cursor: pointer;" onclick="this.parentElement.remove()">✕</button>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 250);
    }, 4000);
  }
}

window.app = new App();
document.addEventListener('DOMContentLoaded', () => window.app.init());

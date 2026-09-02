# Banco SAP - Plataforma Bancária Demonstrativa & Segura

![Java](https://img.shields.io/badge/Java-17%2F21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Uma plataforma bancária corporativa completa, moderna, responsiva e segura construída em **Java (Spring Boot 3)** e **Web SPA Moderna**, projetada como um ambiente demonstrativo e pedagógico de alta fidelidade com dados e transações financeiras simuladas.

---

## 🏛️ Identidade Visual e Experiência do Usuário

* **Paleta de Cores Corporativa**: Azul-Marinho Profundo (`#0A192F` / `#0D2040`), Azul Médio (`#2563EB`), Branco Gelo e Verde Esmeralda (`#10B981`) para aprovações financeiras.
* **Modos Claro e Escuro**: Suporte dinâmico e instantâneo comutável via botão no cabeçalho.
* **100% Responsivo**: Otimizado com layouts fluidos e gaveta lateral para computadores, tablets e smartphones (com barra de navegação inferior estilo app nativo).
* **Gráficos Financeiros Interativos**: Gráficos de fluxo financeiro e distribuição de receitas/despesas com **Chart.js**.

---

## 🖥️ Módulos e Telas do Sistema (15 Telas)

1. **Página Inicial (Landing Page)**: Apresentação da instituição, diferenciais tecnológicos, segurança, simulador de rendimentos e acesso rápido.
2. **Cadastro de Conta**: Formulário com validação em tempo real de CPF fictício, força de senha, telefone e termos de uso.
3. **Autenticação & Recuperação de Senha**: Login seguro com tokens JWT (Access & Refresh), proteção anti-força bruta e redefinição de senha por código sem revelação de existência de e-mail.
4. **Dashboard Principal**: Saldo com botão de privacidade (ocultar/exibir), receitas e despesas do mês, limite disponível, gráfico financeiro e atalhos rápidos.
5. **Contas e Carteiras**: Visão unificada de Conta Corrente, Reserva Financeira e Carteira de Ativos, com dados de agência, conta e aplicação/resgate instantâneo.
6. **Transferências entre Usuários**: Busca de correntistas por e-mail, CPF ou conta, validação de saldo em tempo real, confirmação por PIN de 4 dígitos e emissão de comprovante oficial.
7. **Área de PIX Demonstrativo**: Gerenciamento de chaves (CPF, E-mail, Telefone, Aleatória), envio instantâneo e gerador/leitor de QR Code "Copia e Cola" com payload simulado.
8. **Pagamentos e Boletos**: Leitor/digitador de linha digitável com cálculo de vencimento/banco emissor, liquidação imediata e opção de recarga de saldo por depósito de teste.
9. **Cartões Virtuais Demonstrativos**: Visualização 3D de cartões físicos e virtuais, gerador de cartões temporários, controle deslizante de limite de gastos em tempo real, bloqueio/desbloqueio e simulação de compras online.
10. **Criptoativos Demonstrativos**: Carteira com BTC, ETH, SOL, USDT e ADA, cotações simuladas em tempo real com variação % 24h, compra/venda convertendo BRL <-> Cripto, transferência P2P entre correntistas e disclaimer legal visível.
11. **Extrato Bancário Completo**: Filtros multifacetados (data inicial/final, categoria, tipo de operação e valores), busca rápida e exportação para arquivo **CSV**.
12. **Perfil & Segurança**: Atualização de dados cadastrais, alteração de senha de acesso e configuração de PIN de segurança transacional.
13. **Central de Notificações**: Histórico de alertas de segurança, depósitos, transferências e avisos do sistema com marcação de leitura.
14. **Central de Ajuda e Suporte**: FAQ categorizado, simulador de **Assistente Virtual Inteligente (Chatbot)** e abertura de chamados/tickets com protocolo único.
15. **Painel Administrativo Protegido (ROLE_ADMIN)**: Indicadores globais (KPIs), gestão e busca de correntistas, bloqueio e desbloqueio de contas, gerenciamento de limites e visualização dos logs de auditoria imutáveis com exportação CSV.

---

## 🔒 Segurança Bancária Implementada

* **Senhas Criptografadas com BCrypt**: Nenhuma credencial é armazenada em texto plano.
* **Autenticação Stateless com JWT**: Tokens de acesso de curta duração (15 min) e Refresh Tokens assinados com algoritmo HMAC-SHA256.
* **Proteção Anti-Força Bruta**: Bloqueio temporário de 15 minutos após 5 tentativas consecutivas de login incorreto (por usuário e por IP).
* **Precisão Financeira Estrita**: Todos os valores monetários utilizam exclusivamente `BigDecimal` com arredondamento seguro (`HALF_EVEN` / `HALF_UP`), nunca `float` ou `double`.
* **Atomicidade Transacional e Bloqueio Otimista**: Transações isoladas com `@Transactional` e controle de concorrência com `@Version` em contas bancárias contra gastos duplos.
* **Controle de Acesso Baseado em Perfis (RBAC)**: Regras com `ROLE_CLIENTE`, `ROLE_ATENDENTE` e `ROLE_ADMIN` garantindo isolamento total de rotas.
* **Prevenção contra IDOR, XSS, CSRF e SQL Injection**: Consultas parametrizadas com Spring Data JPA e higienização de dados.
* **Logs de Auditoria Imutáveis**: Registro de quem realizou a ação, data/hora, IP de origem e recurso modificado, sem gravar senhas ou tokens.

---

## 🔑 Credenciais Demonstrativas Pré-Configuradas

| Perfil | E-mail | Senha | PIN | Saldo Inicial |
|---|---|---|---|---|
| **Cliente Principal** | `cliente@bancosap.com.br` | `BancoSap@2026` | `1234` | R$ 14.850,75 |
| **Cliente Secundária** | `maria.silva@bancosap.com.br` | `BancoSap@2026` | `1234` | R$ 8.320,50 |
| **Administrador** | `admin@bancosap.com.br` | `Admin@2026` | `1234` | Acesso Admin |
| **Atendente Operacional**| `operador@bancosap.com.br` | `BancoSap@2026` | `1234` | R$ 25.000,00 |

---

## 🚀 Como Executar o Projeto

### Opção 1: Execução Local Rápida (Zero-Config com H2)

Pré-requisitos: **Java 17 ou 21** e **Maven** (ou através da sua IDE favorita como IntelliJ IDEA / VS Code / Eclipse).

```bash
# Clone ou acesse o diretório do projeto
cd banco-java

# Executar a aplicação Spring Boot
mvn spring-boot:run
```

Acesse no navegador:
* **Aplicação Web Banco SAP**: [http://localhost:8080](http://localhost:8080)
* **Documentação Interativa OpenAPI / Swagger**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Console do Banco H2**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:bancosapdb`, Usuário: `sa`, Senha: em branco)

---

### Opção 2: Execução com Docker & Docker Compose (PostgreSQL + Backend)

Pré-requisitos: **Docker** e **Docker Compose** instalados.

```bash
# Iniciar todos os serviços (PostgreSQL + Backend Spring Boot + Migrations Flyway)
docker-compose up --build
```

A aplicação estará disponível em `http://localhost:8080` com banco de dados PostgreSQL persistente.

---

## 🧪 Execução de Testes Automatizados

O projeto conta com suíte completa de testes unitários desenvolvidos com **JUnit 5** e **Mockito**:

```bash
mvn test
```

Testes incluídos:
* `AuthServiceTest`: Validação de cadastro, login, renovação de tokens e bloqueio por força bruta.
* `TransferServiceTest`: Validação de atomicidade, débito/crédito consistente e rejeição por saldo insuficiente.
* `PixServiceTest`: Cadastro de chaves, transferências PIX e rejeição de chaves inválidas.
* `CardServiceTest`: Emissão de cartões virtuais, ajuste de limites e bloqueio instantâneo.
* `CryptoServiceTest`: Cotações de mercado, compra/venda convertendo BRL <-> Cripto e transferências P2P.
* `AdminServiceTest`: Cálculo de KPIs, bloqueio de contas e auditoria.

---

## 📜 Aviso Legal (Disclaimer)

> [!WARNING]
> O **Banco SAP** é um projeto demonstrativo e educacional. Todas as transações, saldos, chaves PIX, cartões virtuais e cotações de criptoativos operam em ambiente de simulação financeira. Nenhuma transação financeira real é processada.

# Eletronica VA - Sistema de Gestao para Assistencia Tecnica e Estoque

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue.svg)
![HikariCP](https://img.shields.io/badge/HikariCP-5.1.0-green.svg)
![BCrypt](https://img.shields.io/badge/Security-BCrypt-red.svg)
![JUnit5](https://img.shields.io/badge/Tests-JUnit%205%20(30%20passed)-success.svg)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen.svg)

Sistema desktop completo desenvolvido em **Java 17**, **JavaFX 17** e **PostgreSQL**, projetado para gestao operacional, triagem de equipamentos, controle de clientes e emissao de **Ordens de Servico (OS)** em assistencias tecnicas de eletronica.

---

## Interface Grafica, Dual Theme e Dashboard em 3 Colunas

A interface visual oferece alternancia instantanea entre **Tema Escuro (Dark Theme)** e **Tema Claro (Light Theme)**, layout ergonomico dividido em **3 colunas** e gerenciamento inteligente de janelas em tela cheia:

```
+------------------+------------------------------------+-------------------------+
|  SIDEBAR         |  PAINEL CENTRAL                    |  PREVIEW E ACOES (OS)   |
|  - Logotipo      |  - Saudacao, Busca e Alternar Tema |  - Card Destaque "OS"   |
|  - Painel Loja   |  - 4 Cards de Metricas:            |  - Resumo de Diagnostico|
|    (Capacidade)  |    * Em Espera                     |  - Orcamento e Status   |
|  - Menu Vertical |    * Em Andamento                  |  - Botoes Circulares:   |
|    (Icones SVG)  |    * Prontos                       |    * Detalhes (Consulta)|
|  - Botao Hero:   |    * Total Registrado              |    * Alterar Status     |
|    Nova OS (UC05)|  - Tabela de Atendimentos Recentes |    * Excluir OS         |
|  - Sessao e Sair |                                    |                         |
+------------------+------------------------------------+-------------------------+
```

### 1. Dual Theme System (Tema Escuro / Tema Claro)
* **Alternancia Dinamica em Tempo Real**: Atraves do botao de alternancia no topo da tela principal, o usuario pode trocar instantaneamente entre o Tema Escuro e o Tema Claro sem reiniciar a aplicacao.
* **Propagacao Global**: Gerenciado pela classe `ThemeManager`, que notifica ouvintes e reaplica as folhas de estilo (`style.css` e `style-light.css`) a todas as telas abertas e registradas.
* **Tema Escuro**: Baseado em tons Slate escuros (`#0b0f19`, `#111827`, `#1f2937`) e tipografia de alto contraste (`#f9fafb`).
* **Tema Claro**: Baseado em tons Slate claros e limpos (`#f1f5f9`, `#ffffff`, `#e2e8f0`) com tipografia escura (`#0f172a`).
* **Paleta de Acentos Funcionais Compartilhada**:
  * **Azul (`#3b82f6` / `#2563eb`)**: Acoes principais (Salvar, Pesquisar, Detalhes e botao `Nova Ordem de Servico`).
  * **Amarelo (`#fbbf24` / `#f59e0b`)**: Acoes de limpeza/alerta, ordens *Em Espera* e atalho de status.
  * **Vermelho (`#ef4444` / `#dc2626`)**: Acoes criticas (Excluir/Deletar), botao Sair e ordens *Prontas*.

### 2. Politica de Janela Unica e Tela Cheia (ScreenManager)
* **Fechamento Automatico de Janelas Anteriores**: Ao abrir qualquer tela secundaria a partir do menu principal ou de submenus, qualquer outra tela secundaria aberta anteriormente e fechada automaticamente. Isso impede acumulo de janelas sobrepostas e perda de foco pelo operador.
* **Maximizacao por Padrao**: Toda tela secundaria aberta passa a ocupar toda a area util da tela por padrao (`stage.setMaximized(true)`).
* **Retorno Fluido**: Ao fechar uma tela secundaria, o usuario retorna diretamente ao painel principal atualizado com os novos dados.

---

## Arquitetura do Software (MVC + DAO + Singleton)

O projeto segue rigorosamente os diagramas de caso de uso e de classe da aplicacao:

* **Model (`com.eletronica.model`)**: Entidades POJO de negocio com encapsulamento, relacionamentos e sobrescrita de `equals`, `hashCode` e `toString()`.
* **View (`src/main/resources/com/eletronica/view`)**: Interfaces graficas em XML (**JavaFX FXML**) estilizadas via `style.css`, `style-light.css` e icones vetoriais SVG (`SVGPath`).
* **Controller (`com.eletronica.controller`)**: Controladores que escutam eventos da UI, validam formularios e orquestram a navegacao.
* **DAO (`com.eletronica.dao`)**: Camada de persistencia isolando todo o acesso SQL/JDBC via `PreparedStatement` e `try-with-resources`.
* **Util / Infraestrutura (`com.eletronica.util`)**:
  * `ScreenManager`: Gerenciamento centralizado de janelas unicas e maximizadas.
  * `ThemeManager`: Motor centralizado de temas Dark e Light com suporte a observadores.
  * `Database`: Gerenciamento do pool de conexoes **HikariCP**.
  * `AppConfig`: Leitura centralizada de configuracoes com suporte a fallback.
  * `SenhaUtil`: Hashing e checagem de senhas via **BCrypt**.
  * `UsuarioSessao`: Singleton mantendo o estado da sessao do colaborador e permissoes.

```
EletronicaAPP/
├── pom.xml                                   # Configuracao Maven e dependencias
├── application-local.properties.example      # Modelo para credenciais locais
└── src/
    ├── main/
    │   ├── java/com/eletronica/
    │   │   ├── App.java                      # Ponto de entrada JavaFX e migracoes
    │   │   ├── model/                        # Entidades (Cliente, OS, Produto, Servico, etc.)
    │   │   ├── controller/                   # Controladores (Login, Menu, GerarOS, DetalhesOS, etc.)
    │   │   ├── dao/                          # Acesso a dados PostgreSQL (CRUDs)
    │   │   └── util/                         # ScreenManager, ThemeManager, HikariCP, BCrypt, Sessao
    │   └── resources/
    │       ├── application.properties        # Configuracoes padrao de conexao
    │       ├── simplelogger.properties       # Configuracao de logs SLF4J
    │       └── com/eletronica/view/          # Arquivos FXML, style.css e style-light.css
    └── test/java/com/eletronica/             # Suite de 30 testes unitarios e de integracao
```

---

## Funcionalidades e Modulos

### 1. Separacao de Casos de Uso de Ordem de Servico (Eliminacao de Redundancias)
* **Gerar Ordem de Servico (`UC05`) - `FrmGerarOS.fxml` e `GerarOSController.java`**:
  * Tela dedicada a criacao e triagem inicial de novos atendimentos.
  * Vinculacao rapida de Cliente, Equipamento/Produto, diagnostico, orcamento e usuario logado.
  * Atalhos para cadastro instantaneo de novo cliente ou novo produto sem perder o fluxo.
* **Consulta e Detalhes da OS - `FrmDetalhesOS.fxml` e `DetalhesOSController.java`**:
  * Acionada pelo botao circular "Detalhes" no painel de preview ou duplo clique na tabela.
  * Exibicao completa dos dados do cliente, aparelho, historico de diagnostico e orcamento.
  * Atualizacao de status e conclusao de reparos por usuarios autorizados.
* **Gerenciamento e Listagem de OS - `FrmOrdemServico.fxml` e `OrdemServicoController.java`**:
  * Central com tabela completa, filtros por status e administracao.

### 2. Autenticacao e Sessao Segura
* Login por e-mail e senha.
* Armazenamento seguro de senhas com hash **BCrypt**.
* **Migracao automatica e transparente**: senhas legadas sao convertidas para BCrypt na inicializacao (`App.migrarSenhasLegadas()`).
* Bloqueio de memoria: o hash da senha e descartado da entidade apos a validacao.

### 3. Controle de Acesso Baseado em Perfis (RBAC)
* Cadastro de grupos de usuarios (`Administrador`, `Tecnico`, `Atendente`).
* Privilegios granulares:
  * `permissao`: Acesso basico e geracao de Ordens de Servico (`UC05`).
  * `manter_usuario`: Acesso exclusivo para criar, editar ou excluir usuarios e perfis (`UC01`).
  * `manter_servico`: Permissao para modificar o catalogo de servicos (`UC02`).
* Protecao contra autoexclusao do usuario conectado.

### 4. Clientes (`UC03`)
* Gestao de pessoas fisicas e juridicas (Nome, E-mail, Telefone, CPF/CNPJ, RG e Inscricao Estadual).
* Busca em tempo real com filtro por nome e preenchimento automatico para edicao.

### 5. Equipamentos e Produtos (`UC04`)
* Cadastro de aparelhos recebidos na assistencia: Nome, Tipo, Marca, Modelo, Categoria e Defeito inicial relatado.

### 6. Catalogo de Servicos (`UC02`)
* Lista de servicos pre-tabelados para padronizacao de orcamentos e mao de obra.

---

## Seguranca e Banco de Dados

* **Pool de Conexoes**: **HikariCP 5.1.0**, garantindo reaproveitamento de conexoes, tolerancia a falhas e fechamento seguro em `App.stop()`.
* **Externalizacao de Credenciais**:
  As credenciais do banco nunca ficam gravadas no codigo-fonte. A aplicacao le as configuracoes na seguinte ordem de prioridade:
  1. Propriedades de sistema JVM (`-Ddb.url`, `-Ddb.user`, `-Ddb.password`).
  2. Variaveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
  3. Arquivo local `application-local.properties` (ignorado pelo `.gitignore`).
  4. Padroes de desenvolvimento em `src/main/resources/application.properties`.

---

## Como Executar o Projeto

### Pre-requisitos
* **Java JDK 17** ou superior instalado.
* **Apache Maven 3.8+**.
* **PostgreSQL 14+** instalado e em execucao.

---

### 1. Clonar o Repositorio
```bash
git clone https://github.com/Joaopf2/EletronicaVA_Java.git
cd EletronicaVA_Java
```

---

### 2. Configurar o Banco de Dados PostgreSQL
Acesse o console do PostgreSQL (`psql`) e crie o banco de dados:

```sql
CREATE DATABASE eletronicadb;
```

Conecte-se ao banco (`\c eletronicadb`) e execute o script de criacao das tabelas e carga inicial de perfis:

```sql
-- 1. Grupos e Perfis de Acesso
CREATE TABLE IF NOT EXISTS grupos_usuarios (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    grupo INT NOT NULL,
    permissao BOOLEAN NOT NULL DEFAULT TRUE,
    manter_usuario BOOLEAN NOT NULL DEFAULT FALSE,
    manter_servico BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. Usuarios / Colaboradores
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    id_grupo_usuario INT NOT NULL REFERENCES grupos_usuarios(id) ON DELETE RESTRICT
);

-- 3. Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telefone VARCHAR(40),
    cnpjcpf VARCHAR(30),
    rg VARCHAR(30),
    ie VARCHAR(30)
);

-- 4. Equipamentos / Produtos
CREATE TABLE IF NOT EXISTS produtos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(80),
    modelo VARCHAR(100),
    marca VARCHAR(80),
    categoria VARCHAR(80),
    defeito TEXT
);

-- 5. Catalogo de Servicos
CREATE TABLE IF NOT EXISTS servico (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
);

-- 6. Ordens de Servico
CREATE TABLE IF NOT EXISTS ordens_servico (
    id SERIAL PRIMARY KEY,
    orcamento VARCHAR(50),
    data DATE NOT NULL DEFAULT CURRENT_DATE,
    descricao TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ESPERA',
    id_cliente INT NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
    id_produto INT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT
);

-- Carga Inicial (Perfis e Usuario Admin)
INSERT INTO grupos_usuarios (descricao, grupo, permissao, manter_usuario, manter_servico)
VALUES
    ('Administrador', 1, TRUE, TRUE, TRUE),
    ('Tecnico', 2, TRUE, FALSE, TRUE),
    ('Recepcionista', 3, TRUE, FALSE, FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO usuarios (nome, email, senha, id_grupo_usuario)
VALUES ('Administrador', 'admin@eletronica.com', 'admin123', 1)
ON CONFLICT (email) DO NOTHING;
```

---

### 3. Configurar Credenciais Locais (Opcional)
Se o seu PostgreSQL utilizar senha diferente, crie o arquivo `EletronicaAPP/application-local.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/eletronicadb
db.user=postgres
db.password=sua_senha_aqui
```

---

### 4. Executar os Testes Automatizados
Entre na pasta do projeto Maven e execute a suite de testes:

```bash
cd EletronicaAPP
mvn clean test
```

> **Status:** 30 testes automatizados cobrindo autenticacao, migracao BCrypt, pool HikariCP, CRUDs, alternancia dinamica de temas (Dark/Light), gerenciamento de janelas e validacao de carregamento de todos os 10 arquivos FXML.

---

### 5. Iniciar a Aplicacao
Com o banco configurado e em execucao, inicie a aplicacao com:

```bash
mvn javafx:run
```

Ou gere o pacote `.jar` completo:
```bash
mvn clean package
```

---

### Credenciais Iniciais de Teste

| Campo | Valor |
|---|---|
| **E-mail** | `admin@eletronica.com` |
| **Senha** | `admin123` |
| **Perfil** | Administrador (Acesso completo a todos os modulos) |

---

### Tecnologias Utilizadas

* **Linguagem:** Java 17 (LTS)
* **GUI / Interface:** JavaFX 17 (FXML + CSS3 Customizado + Icones SVG + Dual Theme)
* **Banco de Dados:** PostgreSQL 16
* **Pool de Conexoes:** HikariCP 5.1.0
* **Criptografia:** jBCrypt 0.4
* **Testes Automatizados:** JUnit 5 (Jupiter 5.10.2)
* **Gerenciador de Dependencias:** Apache Maven
* **Logging:** SLF4J 1.7.36 com backend `slf4j-simple`

---

### Autores e Contribuidores
Projeto academico e profissional desenvolvido para gestao de assistencias tecnicas em eletronica. Contribuicoes, sugestoes e melhorias sao sempre bem-vindas!

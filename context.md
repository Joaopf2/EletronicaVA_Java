# Contexto e Plano de Melhoria do Projeto — Eletrônica VA (`EletronicaAPP`)

## 1. Visão Geral do Sistema
O **Eletrônica VA (`EletronicaAPP`)** é um sistema desktop CRUD desenvolvido em **Java 17**, **JavaFX 17 (FXML + CSS)** e **PostgreSQL (JDBC)** voltado para o gerenciamento operacional, atendimento e controle de equipamentos/estoque de uma **loja de reparo e assistência técnica de eletrônicos em geral**.

### Módulos do Sistema
1. **Autenticação e Sessão (`Login` / `UsuarioSessao`)**: Controle de acesso por e-mail e senha com carregamento de perfil e permissões em memória (Singleton).
2. **Grupos de Usuários (`GrupoUsuario`)**: Perfis de acesso parametrizáveis (`permissao` básica, `manter_usuario`, `manter_servico`) e níveis de acesso (`1 = Administrador`, `2 = Técnico`, `3 = Recepcionista/Atendente`).
3. **Usuários (`Usuario`)**: Cadastro, consulta, edição e exclusão de colaboradores vinculados a grupos de permissão.
4. **Clientes (`Cliente`)**: Gestão completa de clientes (Pessoa Física ou Jurídica: Nome, E-mail, Telefone, CPF/CNPJ, RG e Inscrição Estadual).
5. **Produtos / Equipamentos (`Produto`)**: Controle dos equipamentos eletrônicos e itens recebidos na loja (Nome, Tipo, Marca, Modelo, Categoria e Defeito/Observação).
6. **Catálogo de Serviços (`Servico`)**: Consulta e manutenção (para perfis autorizados com `manter_servico`) dos serviços técnicos prestados pela assistência.
7. **Ordens de Serviço (`OrdemServico`)**: Núcleo operacional que integra **Cliente + Produto/Equipamento + Orçamento + Status (`EM ESPERA`, `EM ANDAMENTO`, `PRONTO`) + Data**.

---

## 2. Arquitetura MVC + DAO do Projeto

O projeto segue o padrão arquitetural **MVC (Model-View-Controller)** combinado com **DAO (Data Access Object)** e **Singleton** para infraestrutura:

```
EletronicaAPP/
├── pom.xml
└── src/main/
    ├── java/com/eletronica/
    │   ├── App.java                              # Entrypoint JavaFX e ciclo de vida (start / stop)
    │   ├── model/                                # CAMADA MODEL (Entidades POJO + Regras de Domínio)
    │   │   ├── ClienteEntity.java
    │   │   ├── GrupoUsuarioEntity.java
    │   │   ├── OrdemServicoEntity.java
    │   │   ├── ProdutoEntity.java
    │   │   ├── ServicoEntity.java
    │   │   └── UsuarioEntity.java
    │   ├── controller/                           # CAMADA CONTROLLER (Mediação View ↔ Model/DAO)
    │   │   ├── LoginController.java
    │   │   ├── MenuPrincipalController.java
    │   │   ├── ClienteController.java
    │   │   ├── ProdutoController.java
    │   │   ├── ServicoController.java
    │   │   ├── OrdemServicoController.java
    │   │   ├── UsuarioController.java
    │   │   └── GrupoUsuarioController.java
    │   ├── dao/                                  # CAMADA DAO (Persistência JDBC PostgreSQL)
    │   │   ├── ClienteDAO.java
    │   │   ├── ProdutoDAO.java
    │   │   ├── ServicoDAO.java
    │   │   ├── OrdemServicoDAO.java
    │   │   ├── UsuarioDAO.java
    │   │   └── GrupoUsuarioDAO.java
    │   └── util/                                 # INFRAESTRUTURA & SESSÃO (Singletons)
    │       ├── Database.java
    │       ├── UsuarioSessao.java
    │       └── TesteConexao.java
    └── resources/com/eletronica/view/            # CAMADA VIEW (Interfaces FXML + Design System CSS)
        ├── style.css
        ├── FrmLogin.fxml
        ├── FrmMenuPrincipal.fxml
        ├── FrmCliente.fxml
        ├── FrmProduto.fxml
        ├── FrmServico.fxml
        ├── FrmOrdemServico.fxml
        ├── FrmUsuario.fxml
        └── FrmGrupoUsuario.fxml
```

### Responsabilidades no Padrão MVC
- **Model (`com.eletronica.model`)**: Representa as entidades de negócio (`*Entity`), encapsulando atributos, construtores, `getters/setters`, formatação de domínio (`TipoStatus` em `OrdemServicoEntity`) e contratos de igualdade (`equals`, `hashCode`, `toString`) essenciais para componentes visuais como `ComboBox` e `TableView`.
- **View (`src/main/resources/com/eletronica/view`)**: Define exclusivamente a estrutura declarativa da interface gráfica em arquivos `.fxml` e a estilização visual centralizada em `style.css`, sem lógica de negócio misturada.
- **Controller (`com.eletronica.controller`)**: Escuta eventos da **View** (`@FXML`), valida entradas do formulário, verifica permissões na sessão (`UsuarioSessao`), aciona a camada **DAO** para persistência dos objetos **Model** e atualiza as listas observáveis (`ObservableList`).
- **DAO (`com.eletronica.dao`)**: Isola todo o código SQL/JDBC (`PreparedStatement`, `ResultSet`, `JOIN`s) do restante da aplicação.

---

## 3. Diagnóstico da Análise Técnica (Problemas Identificados e Corrigidos)

Durante a auditoria do código-fonte original, foram identificados os seguintes problemas de Back-End e Front-End:

### 3.1. Problemas no Back-End (Controllers, Models e DAOs)
1. **Edição (UPDATE) ausente nos Controllers de Clientes, Produtos e Grupos**:
   - Embora `ClienteDAO.atualizar()`, `ProdutoDAO.atualizar()` e `GrupoUsuarioDAO.atualizar()` estivessem implementados no DAO e o clique na `TableView` carregasse os dados nos campos do formulário, o botão **Salvar** chamava sempre `inserir()`, duplicando registros no banco em vez de atualizá-los.
2. **CRUD de Usuários Incompleto na Tela (`UsuarioController`)**:
   - O `UsuarioDAO` possuía todos os métodos (`inserir`, `listarTodos`, `buscarPorNome`, `atualizar`, `deletar`), porém o `UsuarioController` só implementava `inserir()`. Além disso, o atributo `@FXML private TextArea txtResultado` não existia no `FrmUsuario.fxml`, causando `NullPointerException`.
3. **Catálogo de Serviços sem Operações de Escrita (`ServicoDAO` / `ServicoController`)**:
   - A entidade `GrupoUsuarioEntity` e `UsuarioSessao.podeManterServico()` previam a permissão `manter_servico`, mas `ServicoDAO` não tinha os métodos `inserir`, `atualizar` e `deletar`, e `ServicoController` não permitia cadastrar/editar serviços mesmo para administradores.
4. **Verificação de Permissão de Edição de OS (`OrdemServicoController`)**:
   - O método `UsuarioSessao.podeEditarOS()` existia, mas não era validado no momento de atualizar uma Ordem de Serviço existente.
5. **Risco de `NoSuchElementException` em Diálogos de Confirmação**:
   - Uso de `confirm.showAndWait().get() == ButtonType.OK` sem `orElse(ButtonType.CANCEL)` caso a janela modal fosse fechada pelo botão `X`.
6. **Fechamento da Conexão JDBC no Encerramento**:
   - A classe `App` não sobrescrevia o método de ciclo de vida `stop()` para garantir a chamada a `Database.closeConnection()`.

### 3.2. Problemas no Front-End (FXML e CSS)
1. **Erros de Sintaxe em Estilos Inline**:
   - Em `FrmCliente.fxml` e `FrmProduto.fxml` havia `-fx-font-size:: 20px;;` (dois-pontos e ponto-e-vírgula duplicados).
   - Em `FrmServico.fxml` havia `-fx-font-weight: bolf;` (erro de digitação em `bold`).
2. **Colunas e Componentes Ausentes nos FXMLs**:
   - `colId` estava declarado em `ClienteController` e `ProdutoController`, mas ausente nos respectivos `.fxml` (gerando logs `ERRO: colId é null!`).
   - `FrmOrdemServico.fxml` não exibia a coluna do equipamento/produto (`colProduto`), embora o `OrdemServicoDAO` já trouxesse `nome_produto` via `INNER JOIN`.
3. **Falta de Responsividade no Redimensionamento**:
   - As telas eram abertas em janelas grandes ou maximizadas pelo `MenuPrincipalController`, mas os containers internos (`VBox`) não possuíam âncoras (`AnchorPane.topAnchor`, `bottomAnchor`, `leftAnchor`, `rightAnchor`) nem `VBox.vgrow="ALWAYS"` ou `CONSTRAINED_RESIZE_POLICY` nas tabelas.
4. **Botões Ocultos Ocupando Espaço no Layout**:
   - Ao ocultar botões sem permissão com `setVisible(false)`, faltava definir `setManaged(false)` para que o container reorganizasse o espaço.

---

## 4. Melhorias Implementadas

### 4.1. Back-End (MVC & DAO)
- **Models (`ClienteEntity`, `ProdutoEntity`, `ServicoEntity`, `GrupoUsuarioEntity`, `UsuarioEntity`)**:
  - Implementação padronizada de `toString()`, `equals(Object)` e `hashCode()` baseados no `id` da entidade.
- **DAOs (`ServicoDAO`, `ClienteDAO`, `ProdutoDAO`)**:
  - Implementação de `inserir()`, `atualizar()` e `deletar()` em `ServicoDAO`.
  - Adição de `buscarPorId(int id)` em `ClienteDAO` e `ProdutoDAO`, uniformizando todos os DAOs do sistema.
  - Uso de `try-with-resources` também para objetos `ResultSet`.
- **Controllers**:
  - **`ClienteController`, `ProdutoController`, `GrupoUsuarioController`**: Fluxo unificado de **Salvar / Atualizar** (insere quando não há seleção na tabela; atualiza quando há um registro selecionado), busca ao pressionar `Enter` no campo de pesquisa e confirmação segura de exclusão.
  - **`UsuarioController`**: Evoluído para CRUD completo com `TableView<UsuarioEntity>`, busca por nome, edição ao selecionar na tabela, exclusão com proteção contra autoexclusão do usuário logado e feedback em `txtResultado`.
  - **`ServicoController`**: Habilita automaticamente inserção, edição e exclusão de serviços para usuários com `UsuarioSessao.podeManterServico()`, mantendo o modo somente-consulta para os demais perfis.
  - **`OrdemServicoController`**: Vinculação da coluna `colProduto` (`nomeProduto`), validação de `UsuarioSessao.podeEditarOS()` na edição e seleção automática do status padrão `"EM ESPERA"`.
  - **`App` / `LoginController` / `MenuPrincipalController`**: Fechamento seguro da conexão PostgreSQL (`Database.closeConnection()`) no encerramento da aplicação e ajuste de `setManaged(false)` para botões restritos.

### 4.2. Front-End (JavaFX FXML & CSS)
- **Design System Global (`style.css`)**:
  - Criação de folha de estilos centralizada em `src/main/resources/com/eletronica/view/style.css` com paleta corporativa moderna (tons slate/azul `#1e293b`, `#2563eb`, verde `#16a34a`, vermelho `#dc2626`), cartões com sombra suave (`.card-pane`), tabelas listradas com destaque de seleção e campos com realce de foco.
- **Layouts Responsivos em todas as 8 telas FXML**:
  - Aplicação de âncoras completas no `AnchorPane`, `GridPane` em duas colunas para melhor aproveitamento horizontal nos cadastros, `VBox.vgrow="ALWAYS"` e `CONSTRAINED_RESIZE_POLICY` em todas as `TableView`s.

---

## 5. Script SQL do Banco de Dados PostgreSQL (`eletronicadb`)

Caso precise recriar ou validar a estrutura do banco de dados `eletronicadb` no PostgreSQL:

```sql
-- Criação do banco: CREATE DATABASE eletronicadb;

CREATE TABLE IF NOT EXISTS grupos_usuarios (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    grupo INT NOT NULL,
    permissao BOOLEAN NOT NULL DEFAULT TRUE,
    manter_usuario BOOLEAN NOT NULL DEFAULT FALSE,
    manter_servico BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    id_grupo_usuario INT NOT NULL REFERENCES grupos_usuarios(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telefone VARCHAR(40),
    cnpjcpf VARCHAR(30),
    rg VARCHAR(30),
    ie VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS produtos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(80),
    modelo VARCHAR(100),
    marca VARCHAR(80),
    categoria VARCHAR(80),
    defeito TEXT
);

CREATE TABLE IF NOT EXISTS servico (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ordens_servico (
    id SERIAL PRIMARY KEY,
    orcamento VARCHAR(50),
    data DATE NOT NULL DEFAULT CURRENT_DATE,
    descricao TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ESPERA',
    id_cliente INT NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
    id_produto INT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT
);

-- Dados iniciais recomendados (Seed)
INSERT INTO grupos_usuarios (descricao, grupo, permissao, manter_usuario, manter_servico)
VALUES
    ('Administrador', 1, TRUE, TRUE, TRUE),
    ('Técnico', 2, TRUE, FALSE, TRUE),
    ('Recepcionista', 3, TRUE, FALSE, FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO usuarios (nome, email, senha, id_grupo_usuario)
VALUES ('Administrador', 'admin@eletronica.com', 'admin123', 1)
ON CONFLICT (email) DO NOTHING;
```

---

## 6. Roadmap de Evolução Futura (Próximos Passos Recomendados)

1. **Controle Quantitativo de Estoque de Peças e Insumos**:
   - Adicionar colunas `quantidade INT DEFAULT 0`, `estoque_minimo INT DEFAULT 1` e `preco_unitario NUMERIC(10,2)` na tabela `produtos` (ou criar tabela dedicada `pecas_estoque`) com alertas visuais quando um componente atingir o estoque mínimo.
2. **Itens da Ordem de Serviço (Peças + Serviços realizados)**:
   - Criar tabela associativa `os_itens` vinculando uma Ordem de Serviço a múltiplos `servico` e `produtos/pecas`, calculando o orçamento automaticamente e dando baixa automática no estoque ao mudar o status da OS para `PRONTO`.
3. **Segurança e Configuração (Concluído ✅)**:
   - Substituição de armazenamento de senha em texto puro por hash **BCrypt** (`SenhaUtil.java`).
   - Migração automática e idempotente de senhas legadas na inicialização (`UsuarioDAO.migrarSenhasTextoPuro()`).
   - Externalização de credenciais do `Database.java` para `AppConfig.java`, lendo de `application.properties`, arquivo local `application-local.properties` (gitignored), variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`) ou propriedades JVM.
   - Adoção de pool de conexões de alta performance **HikariCP** thread-safe com liberação imediata via `try-with-resources`.
   - Adição de suíte de testes automatizados com **JUnit 5** (`SenhaUtilTest`, `AppConfigTest`, `DatabaseTest`, `UsuarioDAOTest`, `OutrosDAOsTest`).
4. **Limpeza de Diretórios**:
   - Remover a pasta legada `mavenproject1/` na raiz do repositório, mantendo apenas `EletronicaAPP/` como projeto oficial.


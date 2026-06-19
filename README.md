# Sistema de Gestao Eletronica

Sistema desktop para gerenciamento de ordens de servico em assistencia tecnica de eletronicos.

Caso de Uso:

<img width="898" height="447" alt="image" src="https://github.com/user-attachments/assets/0b139e82-8323-4848-84cd-7a5ff13b1085" />



Diagrama de Classes:

<img width="828" height="715" alt="image" src="https://github.com/user-attachments/assets/7f3bda8a-21e4-4b61-96a3-91ce0c7e79bc" />


Descrição:

O Sistema de Gestao Eletronica e uma aplicacao desktop desenvolvida em JavaFX para gerenciar:
- Cadastro de clientes
- Cadastro de produtos (equipamentos eletronicos)
- Cadastro de servicos
- Controle de usuarios e perfis de acesso
- Ordens de servico (OS)
- Controle de permissoes por grupo

 Tecnologias Utilizadas

| Tecnologia | Versao | Descricao |
|------------|--------|-----------|
| Java | 17+ | Linguagem de programacao |
| JavaFX | 17+ | Framework para interface grafica |
| PostgreSQL | 14+ | Banco de dados relacional |
| Maven | 3.8+ | Gerenciador de dependencias |
| Scene Builder | - | Ferramenta para design das telas |

Estrutura do Banco de Dados

Tabelas principais:
- `cliente` - Dados dos clientes
- `produtos` - Equipamentos eletronicos
- `servico` - Tipos de servicos disponiveis
- `ordens_servico` - Ordens de servico
- `usuarios` - Usuarios do sistema
- `grupos_usuarios` - Perfis e permissoes

 Funcionalidades

Clientes
- Cadastro completo (nome, email, telefone, CPF/CNPJ, RG, IE)
- Busca por nome
- Edicao e exclusao

Produtos
- Cadastro de equipamentos (nome, tipo, modelo, marca, categoria, defeito)
- Busca por nome

Ordens de Servico
- Criar nova OS vinculando cliente e produto
- Status: EM ESPERA, EM ANDAMENTO, PRONTO
- Buscar por descricao
- Editar e deletar (com permissoes)

Usuarios e Grupos
- Cadastro de usuarios com vinculo a grupos
- Grupos com permissoes granulares:
  - `manterUsuario` - Gerenciar usuarios
  - `manterServico` - Gerenciar servicos
  - `permissao` - Acesso basico ao sistema

 Controle de Acesso
- Login com email e senha
- Permissoes diferentes por grupo:
  - Administrador: Acesso total
  - Tecnico: Criar/editar OS, nao deleta
  - Recepcionista: Apenas consulta
  - Visitante: Sem acesso
## Estrutura do Projeto


``` 
EletronicaAPP/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/eletronica/
│   │   │       ├── App.java
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── model/
│   │   │       └── util/
│   │   └── resources/
│   │       └── com/eletronica/view/
│   └── test/
├── pom.xml
└── README.md
```

- JDK 17 ou superior
- PostgreSQL 14 ou superior
- Maven 3.8 ou superior
- JavaFX (gerenciado pelo Maven)

 Instalacao e Execucao

 1. Clone o repositorio

```bash
git clone https://github.com/seu-usuario/eletronica-app.git
cd eletronica-app
-- Criar o banco de dados
CREATE DATABASE eletronicadb;

-- Conectar ao banco
\c eletronicadb;

-- Criar as tabelas
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    cnpjcpf VARCHAR(20) NOT NULL,
    rg VARCHAR(20) NOT NULL,
    ie VARCHAR(20)
);

CREATE TABLE produtos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(50),
    modelo VARCHAR(50),
    marca VARCHAR(50),
    categoria VARCHAR(50),
    defeito TEXT NOT NULL
);

CREATE TABLE servico (
    id SERIAL PRIMARY KEY,
    descricao TEXT NOT NULL
);

CREATE TABLE grupos_usuarios (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(50) NOT NULL,
    grupo INTEGER NOT NULL,
    permissao BOOLEAN DEFAULT TRUE,
    manter_usuario BOOLEAN DEFAULT FALSE,
    manter_servico BOOLEAN DEFAULT FALSE
);

CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(100) NOT NULL,
    id_grupo_usuario INTEGER REFERENCES grupos_usuarios(id)
);

CREATE TABLE ordens_servico (
    id SERIAL PRIMARY KEY,
    orcamento VARCHAR(20) NOT NULL,
    data DATE DEFAULT CURRENT_DATE,
    descricao TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'EM_ESPERA',
    id_cliente INTEGER REFERENCES clientes(id),
    id_produto INTEGER REFERENCES produtos(id)
);

-- Inserir dados padrao
INSERT INTO grupos_usuarios (descricao, grupo, permissao, manter_usuario, manter_servico) VALUES
('Administrador', 1, true, true, true),
('Tecnico', 2, true, false, true),
('Recepcionista', 3, true, false, false),
('Visitante', 4, false, false, false);

INSERT INTO usuarios (nome, email, senha, id_grupo_usuario) VALUES
('Administrador', 'admin@eletronica.com', 'admin123', 1);

Edite o arquivo src/main/java/com/eletronica/util/Database.java:
connection = DriverManager.getConnection(
    "jdbc:postgresql://localhost:5432/SEU_BANCO",
    "SEU_USUARIO",
    "SUA_SENHA"
);

Execute o projeto
mvn clean javafx:run

-- =============================================================================
-- PROJETO: Eletrônica VA (EletronicaAPP)
-- SCRIPT DE DEFINIÇÃO E CARGA INICIAL DO BANCO DE DADOS (PostgreSQL 14+)
-- =============================================================================

-- Instrução para criação do banco de dados (execute como superusuário postgres):
-- CREATE DATABASE eletronicadb WITH OWNER = postgres ENCODING = 'UTF8';
-- \c eletronicadb;

-- -----------------------------------------------------------------------------
-- 1. TABELA: grupos_usuarios (Perfis de Acesso e Permissões RBAC)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS grupos_usuarios (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    grupo INT NOT NULL,
    permissao BOOLEAN NOT NULL DEFAULT TRUE,
    manter_usuario BOOLEAN NOT NULL DEFAULT FALSE,
    manter_servico BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE grupos_usuarios IS 'Perfis de usuários e matriz de permissões do sistema';
COMMENT ON COLUMN grupos_usuarios.grupo IS 'Nível numérico do perfil (1: Administrador, 2: Técnico, 3: Recepcionista)';
COMMENT ON COLUMN grupos_usuarios.permissao IS 'Permissão de acesso básico e emissão de OS';
COMMENT ON COLUMN grupos_usuarios.manter_usuario IS 'Permissão para criar, editar e excluir colaboradores e perfis';
COMMENT ON COLUMN grupos_usuarios.manter_servico IS 'Permissão para gerenciar o catálogo de serviços';

-- -----------------------------------------------------------------------------
-- 2. TABELA: usuarios (Colaboradores do Sistema)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    id_grupo_usuario INT NOT NULL REFERENCES grupos_usuarios(id) ON DELETE RESTRICT
);

COMMENT ON TABLE usuarios IS 'Colaboradores autorizados a operar o sistema';
COMMENT ON COLUMN usuarios.senha IS 'Hash seguro BCrypt (senhas legadas são migradas automaticamente na inicialização)';

-- -----------------------------------------------------------------------------
-- 3. TABELA: cliente (Clientes Pessoa Física e Jurídica)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telefone VARCHAR(40),
    cnpjcpf VARCHAR(30),
    rg VARCHAR(30),
    ie VARCHAR(30)
);

COMMENT ON TABLE cliente IS 'Cadastro de clientes da assistência técnica';

-- -----------------------------------------------------------------------------
-- 4. TABELA: produtos (Equipamentos Eletrônicos e Itens em Reparo)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produtos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    tipo VARCHAR(80),
    modelo VARCHAR(100),
    marca VARCHAR(80),
    categoria VARCHAR(80),
    defeito TEXT
);

COMMENT ON TABLE produtos IS 'Aparelhos e eletrônicos recebidos para manutenção';

-- -----------------------------------------------------------------------------
-- 5. TABELA: servico (Catálogo Tabelado de Serviços Prestados)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS servico (
    id SERIAL PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL
);

COMMENT ON TABLE servico IS 'Catálogo de serviços técnicos tabelados para orçamentos';

-- -----------------------------------------------------------------------------
-- 6. TABELA: ordens_servico (Ordens de Serviço - Núcleo Operacional)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ordens_servico (
    id SERIAL PRIMARY KEY,
    orcamento VARCHAR(50),
    data DATE NOT NULL DEFAULT CURRENT_DATE,
    descricao TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_ESPERA',
    id_cliente INT NOT NULL REFERENCES cliente(id) ON DELETE RESTRICT,
    id_produto INT NOT NULL REFERENCES produtos(id) ON DELETE RESTRICT
);

COMMENT ON TABLE ordens_servico IS 'Ordens de serviço emitidas integrando cliente, equipamento e status de reparo';
COMMENT ON COLUMN ordens_servico.status IS 'Status da OS: EM_ESPERA, EM_ANDAMENTO, PRONTO';

-- -----------------------------------------------------------------------------
-- ÍNDICES DE PERFORMANCE
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_cliente_nome ON cliente(nome);
CREATE INDEX IF NOT EXISTS idx_produtos_nome ON produtos(nome);
CREATE INDEX IF NOT EXISTS idx_ordens_servico_cliente ON ordens_servico(id_cliente);
CREATE INDEX IF NOT EXISTS idx_ordens_servico_produto ON ordens_servico(id_produto);
CREATE INDEX IF NOT EXISTS idx_ordens_servico_status ON ordens_servico(status);
CREATE INDEX IF NOT EXISTS idx_ordens_servico_data ON ordens_servico(data DESC);

-- =============================================================================
-- CARGA INICIAL (SEEDS ESSENCIAIS E DADOS PADRÃO)
-- =============================================================================

-- Perfis de Acesso
INSERT INTO grupos_usuarios (descricao, grupo, permissao, manter_usuario, manter_servico)
VALUES
    ('Administrador', 1, TRUE, TRUE, TRUE),
    ('Técnico', 2, TRUE, FALSE, TRUE),
    ('Recepcionista', 3, TRUE, FALSE, FALSE)
ON CONFLICT DO NOTHING;

-- Usuário Administrador Padrão (senha inicial: admin123, hash migrado automaticamente pelo App)
INSERT INTO usuarios (nome, email, senha, id_grupo_usuario)
VALUES ('Administrador', 'admin@eletronica.com', 'admin123', 1)
ON CONFLICT (email) DO NOTHING;

-- Catálogo Básico de Serviços
INSERT INTO servico (descricao) VALUES
    ('Troca de Tela / Display'),
    ('Troca de Conector de Carga'),
    ('Substituição de Bateria'),
    ('Reparo de Placa-Mãe / Microssolda'),
    ('Banho Químico e Desoxidação'),
    ('Formatação e Recuperação de Sistema'),
    ('Limpeza Interna e Troca de Pasta Térmica'),
    ('Atualização de BIOS / Firmware')
ON CONFLICT DO NOTHING;

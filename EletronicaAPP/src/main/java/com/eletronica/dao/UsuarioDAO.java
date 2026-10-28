/**
 * CLASSE DE ACESSO A DADOS (DAO) PARA USUÁRIO
 * 
 * Esta classe é responsável por todas as operações de banco de dados
 * relacionadas à entidade Usuário.
 * 
 * Funcionalidades:
 * - Inserir novo usuário (CREATE)
 * - Listar todos os usuários (READ)
 * - Buscar usuário por nome (READ com filtro)
 * - Buscar usuário por ID (READ)
 * - Buscar usuário por email (READ - para login)
 * - Buscar usuário com grupo completo (READ - para login)
 * - Atualizar dados do usuário (UPDATE)
 * - Deletar usuário (DELETE)
 * - Validar login (autenticação com BCrypt)
 * - Migrar senhas legadas em texto puro para BCrypt
 * 
 * Segurança:
 * - As senhas são gravadas SOMENTE como hash BCrypt (ver SenhaUtil)
 * - As consultas de listagem não carregam o hash da senha
 * 
 * Relacionamentos:
 * - Um usuário pertence a UM grupo (FK: id_grupo_usuario)
 * - JOIN com tabela grupos_usuarios para trazer dados do grupo
 * 
 * @author joao
 */
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.GrupoUsuarioEntity; // Modelo de Grupo de Usuário
import com.eletronica.model.UsuarioEntity;      // Modelo/entidade Usuário
import com.eletronica.util.Database;            // Classe de conexão com o banco
import com.eletronica.util.SenhaUtil;           // Hash/verificação de senhas (BCrypt)

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class UsuarioDAO {

    // ==================== MÉTODO INSERIR (CREATE) ====================
    
    /**
     * INSERE UM NOVO USUÁRIO NO BANCO DE DADOS
     * @param usuario Objeto UsuarioEntity com os dados a serem inseridos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(UsuarioEntity usuario) throws SQLException {
        // SQL de inserção
        String sql = "INSERT INTO usuarios (nome, email, senha, id_grupo_usuario) VALUES (?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS: solicita que o banco retorne o ID gerado
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());        // Nome do usuário
            stmt.setString(2, usuario.getEmail());       // Email (usado para login)
            stmt.setString(3, SenhaUtil.hash(usuario.getSenha())); // Senha (hash BCrypt)
            stmt.setInt(4, usuario.getIdGrupoUsuario()); // ID do grupo (FK)

            // EXECUÇÃO DO SQL
            stmt.executeUpdate();

            //RECUPERAÇÃO DO ID GERADO
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto usuário
            }
        }
    }

    // MÉTODO LISTAR TODOS (READ)
    /**
     * LISTA TODOS OS USUÁRIOS DO BANCO DE DADOS
     * 
     * Realiza JOIN com a tabela grupos_usuarios para trazer
     * a descrição do grupo (nome_grupo).
     * 
     * @return Lista de UsuarioEntity com todos os usuários
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<UsuarioEntity> listarTodos() throws SQLException {
        List<UsuarioEntity> usuarios = new ArrayList<>();
        
        // SQL com JOIN para buscar o nome do grupo
        // u.* = todas as colunas da tabela usuarios
        // g.descricao = nome do grupo (apelido: nome_grupo)
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u "
                + "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id "
                + "ORDER BY u.nome";  // Ordenado por nome (A-Z)

        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setIdGrupoUsuario(rs.getInt("id_grupo_usuario"));
                u.setNomeGrupo(rs.getString("nome_grupo")); // Vem do JOIN
                usuarios.add(u);
            }
        }
        return usuarios;
    }

    //  MÉTODO BUSCAR POR NOME (READ COM FILTRO)
    
    /**
     * BUSCA USUÁRIOS CUJO NOME CONTENHA O TEXTO FORNECIDO
     * 
     * Utiliza ILIKE no PostgreSQL (busca case-insensitive).
     * 
     * @param nome Parte do nome do usuário a ser buscada
     * @return Lista de usuários que atendem ao critério de busca
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<UsuarioEntity> buscarPorNome(String nome) throws SQLException {
        List<UsuarioEntity> usuarios = new ArrayList<>();
        
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u "
                + "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id "
                + "WHERE u.nome ILIKE ? ORDER BY u.nome";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");  // % = qualquer caractere antes/depois
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setIdGrupoUsuario(rs.getInt("id_grupo_usuario"));
                u.setNomeGrupo(rs.getString("nome_grupo"));
                usuarios.add(u);
            }
        }
        return usuarios;
    }

    // ==================== MÉTODO BUSCAR POR ID (READ) ====================
    
    /**
     * BUSCA UM USUÁRIO PELO SEU ID
     * 
     * @param id Identificador único do usuário
     * @return UsuarioEntity encontrado, ou null se não existir
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public UsuarioEntity buscarPorId(int id) throws SQLException {
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u "
                + "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id "
                + "WHERE u.id = ?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setIdGrupoUsuario(rs.getInt("id_grupo_usuario"));
                u.setNomeGrupo(rs.getString("nome_grupo"));
                return u;
            }
        }
        return null;
    }

    // ==================== MÉTODO BUSCAR POR EMAIL (READ) ====================
    
    /**
     * BUSCA UM USUÁRIO PELO SEU EMAIL
     * 
     * O email é único no sistema e é usado para login.
     * 
     * @param email Email do usuário
     * @return UsuarioEntity encontrado, ou null se não existir
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public UsuarioEntity buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u "
                + "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id "
                + "WHERE u.email = ?";  // Busca exata (não LIKE)

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setIdGrupoUsuario(rs.getInt("id_grupo_usuario"));
                u.setNomeGrupo(rs.getString("nome_grupo"));
                return u;
            }
        }
        return null;
    }

    // ==================== MÉTODO ATUALIZAR (UPDATE) ====================
    
    /**
     * ATUALIZA OS DADOS DE UM USUÁRIO EXISTENTE
     * 
     * Se usuario.getSenha() estiver vazio/nulo, a senha atual é MANTIDA.
     * Caso contrário, a nova senha (texto puro) é convertida em hash BCrypt.
     * 
     * @param usuario Objeto UsuarioEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(UsuarioEntity usuario) throws SQLException {
        boolean alterarSenha = usuario.getSenha() != null && !usuario.getSenha().isEmpty();
        String sql = alterarSenha
                ? "UPDATE usuarios SET nome=?, email=?, id_grupo_usuario=?, senha=? WHERE id=?"
                : "UPDATE usuarios SET nome=?, email=?, id_grupo_usuario=? WHERE id=?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            int i = 1;
            stmt.setString(i++, usuario.getNome());
            stmt.setString(i++, usuario.getEmail());
            stmt.setInt(i++, usuario.getIdGrupoUsuario());
            if (alterarSenha) {
                stmt.setString(i++, SenhaUtil.hash(usuario.getSenha()));
            }
            stmt.setInt(i, usuario.getId());  // WHERE id = ?
            stmt.executeUpdate();
        }
    }

    // ==================== MÉTODO DELETAR (DELETE) ====================
    
    /**
     * DELETA UM USUÁRIO DO BANCO DE DADOS
     * 
     * @param id Identificador único do usuário a ser deletado
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ==================== MÉTODO VALIDAR LOGIN (AUTH) ====================
    
    /**
     * VALIDA AS CREDENCIAIS DE LOGIN DO USUÁRIO
     * 
     * Verifica se existe um usuário com o email fornecido e se a senha
     * confere com o hash BCrypt armazenado.
     * 
     * @param email Email do usuário
     * @param senha Senha do usuário (texto puro digitado)
     * @return true se as credenciais são válidas, false caso contrário
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public boolean validarLogin(String email, String senha) throws SQLException {
        return autenticar(email, senha) != null;
    }

    // ==================== MÉTODO AUTENTICAR (LOGIN SEGURO) ====================

    /**
     * AUTENTICA O USUÁRIO E RETORNA SEUS DADOS COM O GRUPO/PERMISSÕES
     * 
     * - Compara a senha digitada com o hash BCrypt armazenado
     * - Se a senha armazenada ainda estiver em texto puro (legado) e conferir,
     *   ela é convertida para BCrypt automaticamente
     * - O hash NÃO é mantido no objeto retornado (não fica na sessão)
     * 
     * @param email Email do usuário
     * @param senha Senha digitada (texto puro)
     * @return UsuarioEntity com grupo preenchido, ou null se email/senha inválidos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public UsuarioEntity autenticar(String email, String senha) throws SQLException {
        UsuarioEntity usuario = buscarPorEmailComGrupo(email);
        if (usuario == null || !SenhaUtil.verificar(senha, usuario.getSenha())) {
            return null;
        }
        if (SenhaUtil.precisaRehash(usuario.getSenha())) {
            atualizarHashSenha(usuario.getId(), SenhaUtil.hash(senha));
        }
        usuario.setSenha(null);  // Não manter o hash em memória
        return usuario;
    }

    /**
     * GRAVA DIRETAMENTE UM HASH DE SENHA JÁ CALCULADO
     */
    private void atualizarHashSenha(int id, String hash) throws SQLException {
        String sql = "UPDATE usuarios SET senha=? WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hash);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    // ==================== MIGRAÇÃO DE SENHAS LEGADAS ====================

    /**
     * CONVERTE PARA BCRYPT TODAS AS SENHAS AINDA ARMAZENADAS EM TEXTO PURO
     * 
     * Operação idempotente: senhas que já são hash BCrypt são ignoradas.
     * Executada automaticamente na inicialização do sistema (App.start).
     * 
     * @return Quantidade de senhas convertidas
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public int migrarSenhasTextoPuro() throws SQLException {
        String select = "SELECT id, senha FROM usuarios";
        String update = "UPDATE usuarios SET senha=? WHERE id=? AND senha=?";
        int convertidas = 0;

        try (Connection conn = Database.getConnection()) {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (Statement stmtSel = conn.createStatement();
                 ResultSet rs = stmtSel.executeQuery(select);
                 PreparedStatement stmtUpd = conn.prepareStatement(update)) {

                while (rs.next()) {
                    String atual = rs.getString("senha");
                    if (SenhaUtil.precisaRehash(atual)) {
                        stmtUpd.setString(1, SenhaUtil.hash(atual));
                        stmtUpd.setInt(2, rs.getInt("id"));
                        stmtUpd.setString(3, atual);  // evita sobrescrever alteração concorrente
                        stmtUpd.addBatch();
                        convertidas++;
                    }
                }
                if (convertidas > 0) {
                    stmtUpd.executeBatch();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }
        return convertidas;
    }

    // ==================== MÉTODO BUSCAR POR EMAIL COM GRUPO COMPLETO (LOGIN) ====================
    
    /**
     * BUSCA UM USUÁRIO PELO EMAIL E CARREGA TODOS OS DADOS DO GRUPO
     * 
     * Este método é usado especificamente no login para carregar
     * todas as permissões do usuário de uma só vez.
     * 
     * Retorna um UsuarioEntity que contém um objeto GrupoUsuarioEntity
     * com todas as permissões (manterUsuario, manterServico, etc.)
     * 
     * @param email Email do usuário
     * @return UsuarioEntity com grupo preenchido, ou null se não encontrado
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public UsuarioEntity buscarPorEmailComGrupo(String email) throws SQLException {
        // SQL busca dados do usuário E todas as permissões do grupo
        String sql = "SELECT u.*, g.descricao as nome_grupo, g.grupo as codigo_grupo, "
                + "g.permissao, g.manter_usuario, g.manter_servico "
                + "FROM usuarios u "
                + "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id "
                + "WHERE u.email = ?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // ==================== CRIA O OBJETO USUÁRIO ====================
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
                u.setIdGrupoUsuario(rs.getInt("id_grupo_usuario"));
                u.setNomeGrupo(rs.getString("nome_grupo"));

                // ==================== CRIA O OBJETO GRUPO (COM PERMISSÕES) ====================
                GrupoUsuarioEntity g = new GrupoUsuarioEntity();
                g.setId(rs.getInt("id_grupo_usuario"));
                g.setDescricao(rs.getString("nome_grupo"));
                g.setGrupo(rs.getInt("codigo_grupo"));
                g.setPermissao(rs.getBoolean("permissao"));
                g.setManterUsuario(rs.getBoolean("manter_usuario"));
                g.setManterServico(rs.getBoolean("manter_servico"));

                // ==================== VINCULA O GRUPO AO USUÁRIO ====================
                u.setGrupo(g);

                return u;
            }
        }
        return null;
    }
}
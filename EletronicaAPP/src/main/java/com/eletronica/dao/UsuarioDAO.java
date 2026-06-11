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
 * - Validar login (autenticação)
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

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class UsuarioDAO {

    // ==================== MÉTODO INSERIR (CREATE) ====================
    
    /**
     * INSERE UM NOVO USUÁRIO NO BANCO DE DADOS
     * 
     * Utiliza PreparedStatement para evitar SQL Injection.
     * O ID do usuário é gerado automaticamente pelo banco (SERIAL).
     * 
     * @param usuario Objeto UsuarioEntity com os dados a serem inseridos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(UsuarioEntity usuario) throws SQLException {
        // SQL de inserção
        String sql = "INSERT INTO usuarios (nome, email, senha, id_grupo_usuario) VALUES (?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS: solicita que o banco retorne o ID gerado
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ==================== PREENCHIMENTO DOS PARÂMETROS ====================
            stmt.setString(1, usuario.getNome());        // Nome do usuário
            stmt.setString(2, usuario.getEmail());       // Email (usado para login)
            stmt.setString(3, usuario.getSenha());       // Senha (texto puro - para desenvolvimento)
            stmt.setInt(4, usuario.getIdGrupoUsuario()); // ID do grupo (FK)

            // ==================== EXECUÇÃO DO SQL ====================
            stmt.executeUpdate();

            // ==================== RECUPERAÇÃO DO ID GERADO ====================
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto usuário
            }
        }
    }

    // ==================== MÉTODO LISTAR TODOS (READ) ====================
    
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

        try (Statement stmt = Database.getConnection().createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
                u.setIdGrupoUsuario(rs.getInt("id_grupo_usuario"));
                u.setNomeGrupo(rs.getString("nome_grupo")); // Vem do JOIN
                usuarios.add(u);
            }
        }
        return usuarios;
    }

    // ==================== MÉTODO BUSCAR POR NOME (READ COM FILTRO) ====================
    
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

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");  // % = qualquer caractere antes/depois
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
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

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
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

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setId(rs.getInt("id"));
                u.setNome(rs.getString("nome"));
                u.setEmail(rs.getString("email"));
                u.setSenha(rs.getString("senha"));
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
     * @param usuario Objeto UsuarioEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(UsuarioEntity usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome=?, email=?, senha=?, id_grupo_usuario=? WHERE id=?";

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setInt(4, usuario.getIdGrupoUsuario());
            stmt.setInt(5, usuario.getId());  // WHERE id = ?
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

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ==================== MÉTODO VALIDAR LOGIN (AUTH) ====================
    
    /**
     * VALIDA AS CREDENCIAIS DE LOGIN DO USUÁRIO
     * 
     * Verifica se existe um usuário com o email e senha fornecidos.
     * 
     * @param email Email do usuário
     * @param senha Senha do usuário
     * @return true se as credenciais são válidas, false caso contrário
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public boolean validarLogin(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // Retorna true se encontrou algum registro
        }
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

        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
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
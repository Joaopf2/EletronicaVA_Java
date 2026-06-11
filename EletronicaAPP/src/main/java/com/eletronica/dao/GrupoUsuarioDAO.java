/**
 * CLASSE DE ACESSO A DADOS (DAO) PARA GRUPO DE USUÁRIOS
 * 
 * Esta classe é responsável por todas as operações de banco de dados
 * relacionadas à entidade GrupoUsuario.
 * 
 * Funcionalidades:
 * - Inserir novo grupo (CREATE)
 * - Listar todos os grupos (READ)
 * - Buscar grupo por ID (READ)
 * - Buscar grupo por descrição (READ com filtro)
 * - Atualizar dados do grupo (UPDATE)
 * - Deletar grupo (DELETE)
 * 
 * Os grupos definem as permissões que os usuários terão no sistema:
 * - permissao: acesso básico ao sistema
 * - manter_usuario: pode gerenciar usuários e grupos
 * - manter_servico: pode gerenciar serviços
 * 
 * @author joao
 */
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.GrupoUsuarioEntity; // Modelo/entidade Grupo de Usuário
import com.eletronica.util.Database;            // Classe de conexão com o banco

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class GrupoUsuarioDAO {
    
    // ==================== MÉTODO INSERIR (CREATE) ====================
    
    /**
     * INSERE UM NOVO GRUPO DE USUÁRIO NO BANCO DE DADOS
     * 
     * Utiliza PreparedStatement para evitar SQL Injection.
     * O ID do grupo é gerado automaticamente pelo banco (SERIAL) e
     * é atribuído ao objeto grupo passado como parâmetro.
     * 
     * @param grupo Objeto GrupoUsuarioEntity com os dados a serem inseridos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(GrupoUsuarioEntity grupo) throws SQLException {
        // SQL de inserção (os '?' são placeholders para os valores)
        String sql = "INSERT INTO grupos_usuarios (descricao, grupo, permissao, manter_usuario, manter_servico) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        // try-with-resources: garante que o PreparedStatement será fechado automaticamente
        // Statement.RETURN_GENERATED_KEYS: solicita que o banco retorne o ID gerado
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ==================== PREENCHIMENTO DOS PARÂMETROS ====================
            // Os números correspondem à posição dos '?' na SQL
            stmt.setString(1, grupo.getDescricao());      // Descrição do grupo (ex: "Administrador")
            stmt.setInt(2, grupo.getGrupo());             // Código numérico do grupo (1, 2, 3...)
            stmt.setBoolean(3, grupo.isPermissao());      // Permissão básica (true/false)
            stmt.setBoolean(4, grupo.isManterUsuario());  // Pode gerenciar usuários?
            stmt.setBoolean(5, grupo.isManterServico());  // Pode gerenciar serviços?
            
            // ==================== EXECUÇÃO DO SQL ====================
            stmt.executeUpdate();
            
            // ==================== RECUPERAÇÃO DO ID GERADO ====================
            // O banco PostgreSQL gera o ID automaticamente (SERIAL)
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                grupo.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto grupo
            }
        }
    }
    
    // ==================== MÉTODO LISTAR TODOS (READ) ====================
    
    /**
     * LISTA TODOS OS GRUPOS DE USUÁRIOS DO BANCO DE DADOS
     * 
     * Retorna uma lista com todos os grupos ordenados pelo código do grupo.
     * 
     * @return Lista de GrupoUsuarioEntity com todos os grupos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<GrupoUsuarioEntity> listarTodos() throws SQLException {
        // Lista vazia para armazenar os resultados
        List<GrupoUsuarioEntity> grupos = new ArrayList<>();
        
        // SQL: Seleciona todos os grupos, ordenados por código (grupo)
        String sql = "SELECT * FROM grupos_usuarios ORDER BY grupo";
        
        // try-with-resources: Statement e ResultSet são fechados automaticamente
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // ==================== PERCORRE O RESULTADO ====================
            // rs.next() move o cursor para o próximo registro
            // Retorna false quando não há mais registros
            while (rs.next()) {
                // Cria um novo objeto GrupoUsuarioEntity
                GrupoUsuarioEntity g = new GrupoUsuarioEntity();
                
                // Extrai os dados do ResultSet (mapeamento coluna → atributo)
                g.setId(rs.getInt("id"));                       // ID (int)
                g.setDescricao(rs.getString("descricao"));      // Descrição (String)
                g.setGrupo(rs.getInt("grupo"));                 // Código do grupo (int)
                g.setPermissao(rs.getBoolean("permissao"));     // Permissão (boolean)
                g.setManterUsuario(rs.getBoolean("manter_usuario")); // Manter usuário (boolean)
                g.setManterServico(rs.getBoolean("manter_servico")); // Manter serviço (boolean)
                
                // Adiciona o grupo à lista
                grupos.add(g);
            }
        }
        
        return grupos;  // Retorna a lista (pode ser vazia)
    }
    
    // ==================== MÉTODO BUSCAR POR ID (READ) ====================
    
    /**
     * BUSCA UM GRUPO DE USUÁRIO PELO SEU ID
     * 
     * Retorna um único grupo ou null se não encontrado.
     * 
     * @param id Identificador único do grupo
     * @return GrupoUsuarioEntity encontrado, ou null se não existir
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public GrupoUsuarioEntity buscarPorId(int id) throws SQLException {
        // SQL: Seleciona grupo pelo ID
        String sql = "SELECT * FROM grupos_usuarios WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);  // WHERE id = ?
            ResultSet rs = stmt.executeQuery();
            
            // Se encontrou um registro (rs.next() retorna true)
            if (rs.next()) {
                GrupoUsuarioEntity g = new GrupoUsuarioEntity();
                g.setId(rs.getInt("id"));
                g.setDescricao(rs.getString("descricao"));
                g.setGrupo(rs.getInt("grupo"));
                g.setPermissao(rs.getBoolean("permissao"));
                g.setManterUsuario(rs.getBoolean("manter_usuario"));
                g.setManterServico(rs.getBoolean("manter_servico"));
                return g;  // Retorna o grupo encontrado
            }
        }
        
        return null;  // Nenhum grupo encontrado com este ID
    }
    
    // ==================== MÉTODO BUSCAR POR DESCRIÇÃO (READ COM FILTRO) ====================
    
    /**
     * BUSCA GRUPOS CUJA DESCRIÇÃO CONTENHA O TEXTO FORNECIDO
     * 
     * Utiliza ILIKE no PostgreSQL (busca case-insensitive, ignora maiúsculas/minúsculas).
     * O % no início e fim permite buscar textos que contenham o termo em qualquer posição.
     * 
     * Exemplo: buscarPorDescricao("admin") encontra "Administrador", "Admin", "Super Admin"
     * 
     * @param descricao Parte da descrição a ser buscada
     * @return Lista de grupos que atendem ao critério de busca
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<GrupoUsuarioEntity> buscarPorDescricao(String descricao) throws SQLException {
        List<GrupoUsuarioEntity> grupos = new ArrayList<>();
        
        // SQL: ILIKE = case-insensitive (não diferencia maiúsculas de minúsculas)
        String sql = "SELECT * FROM grupos_usuarios WHERE descricao ILIKE ? ORDER BY grupo";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            // O % no início e fim torna a busca "contém"
            // Ex: descricao = "tec" → SQL fica "... WHERE descricao ILIKE '%tec%'"
            stmt.setString(1, "%" + descricao + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GrupoUsuarioEntity g = new GrupoUsuarioEntity();
                g.setId(rs.getInt("id"));
                g.setDescricao(rs.getString("descricao"));
                g.setGrupo(rs.getInt("grupo"));
                g.setPermissao(rs.getBoolean("permissao"));
                g.setManterUsuario(rs.getBoolean("manter_usuario"));
                g.setManterServico(rs.getBoolean("manter_servico"));
                grupos.add(g);
            }
        }
        
        return grupos;
    }
    
    // ==================== MÉTODO ATUALIZAR (UPDATE) ====================
    
    /**
     * ATUALIZA OS DADOS DE UM GRUPO DE USUÁRIO EXISTENTE
     * 
     * O grupo é identificado pelo seu ID (chave primária).
     * Todos os campos são atualizados com os valores do objeto fornecido.
     * 
     * @param grupo Objeto GrupoUsuarioEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(GrupoUsuarioEntity grupo) throws SQLException {
        // SQL de atualização: SET define os novos valores, WHERE identifica o registro
        String sql = "UPDATE grupos_usuarios SET descricao=?, grupo=?, permissao=?, " +
                     "manter_usuario=?, manter_servico=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            // Preenche os parâmetros (na ordem do SET)
            stmt.setString(1, grupo.getDescricao());
            stmt.setInt(2, grupo.getGrupo());
            stmt.setBoolean(3, grupo.isPermissao());
            stmt.setBoolean(4, grupo.isManterUsuario());
            stmt.setBoolean(5, grupo.isManterServico());
            stmt.setInt(6, grupo.getId());  // WHERE id = ?
            
            stmt.executeUpdate();  // Executa a atualização
        }
    }
    
    // ==================== MÉTODO DELETAR (DELETE) ====================
    
    /**
     * DELETA UM GRUPO DE USUÁRIO DO BANCO DE DADOS
     * 
     * O grupo é identificado pelo seu ID.
     * 
     * ATENÇÃO: Se houver usuários vinculados a este grupo,
     * a exclusão pode falhar devido à chave estrangeira (ON DELETE RESTRICT).
     * Isso impede que grupos com usuários ativos sejam deletados.
     * 
     * @param id Identificador único do grupo a ser deletado
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void deletar(int id) throws SQLException {
        // SQL de deleção
        String sql = "DELETE FROM grupos_usuarios WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);  // WHERE id = ?
            stmt.executeUpdate(); // Executa a deleção
        }
    }
}
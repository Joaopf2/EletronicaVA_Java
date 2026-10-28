
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.ServicoEntity; // Modelo/entidade Serviço
import com.eletronica.util.Database;       // Classe de conexão com o banco

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class ServicoDAO {
    
    
    public List<ServicoEntity> buscarPorDescricao(String descricao) throws SQLException {
        // Lista vazia para armazenar os resultados
        List<ServicoEntity> servicos = new ArrayList<>();
        
        // SQL: ILIKE = case-insensitive (não diferencia maiúsculas de minúsculas)
        // ORDER BY descricao ordena os resultados em ordem alfabética
        String sql = "SELECT * FROM servico WHERE descricao ILIKE ? ORDER BY descricao";
        
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // O % no início e fim torna a busca "contém"
            // Ex: descricao = "tela" → SQL fica "... WHERE descricao ILIKE '%tela%'"
            stmt.setString(1, "%" + descricao + "%");
            ResultSet rs = stmt.executeQuery();
            
            // ==================== PERCORRE O RESULTADO ====================
            while (rs.next()) {
                // Cria um novo objeto ServicoEntity
                ServicoEntity s = new ServicoEntity();
                
                // Extrai os dados do ResultSet (mapeamento coluna → atributo)
                s.setId(rs.getInt("id"));                 // ID (int)
                s.setDescricao(rs.getString("descricao")); // Descrição (String)
                
                // Adiciona o serviço à lista
                servicos.add(s);
            }
        }
        
        return servicos;  // Retorna a lista (pode ser vazia)
    }
    
        public List<ServicoEntity> listarTodos() throws SQLException {
        List<ServicoEntity> servicos = new ArrayList<>();
        
        // SQL: Seleciona todos os serviços, ordenados por descrição (alfabética)
        String sql = "SELECT * FROM servico ORDER BY descricao";
        
        // try-with-resources: Statement e ResultSet são fechados automaticamente
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ServicoEntity s = new ServicoEntity();
                s.setId(rs.getInt("id"));
                s.setDescricao(rs.getString("descricao"));
                servicos.add(s);
            }
        }
        
        return servicos;  // Retorna a lista (pode ser vazia)
    }
    
    
    public ServicoEntity buscarPorId(int id) throws SQLException {
        // SQL: Seleciona serviço pelo ID
        String sql = "SELECT * FROM servico WHERE id = ?";
        
        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);  // WHERE id = ?
            try (ResultSet rs = stmt.executeQuery()) {
                // Se encontrou um registro (rs.next() retorna true)
                if (rs.next()) {
                    ServicoEntity s = new ServicoEntity();
                    s.setId(rs.getInt("id"));
                    s.setDescricao(rs.getString("descricao"));
                    return s;  // Retorna o serviço encontrado
                }
            }
        }
        
        return null;  // Nenhum serviço encontrado com este ID
    }

    // ==================== MÉTODO INSERIR (CREATE) ====================

    /**
     * INSERE UM NOVO SERVIÇO NO BANCO DE DADOS
     * 
     * @param servico Objeto ServicoEntity com a descrição a ser inserida
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(ServicoEntity servico) throws SQLException {
        String sql = "INSERT INTO servico (descricao) VALUES (?)";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, servico.getDescricao());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    servico.setId(rs.getInt(1));
                }
            }
        }
    }

    // ==================== MÉTODO ATUALIZAR (UPDATE) ====================

    /**
     * ATUALIZA OS DADOS DE UM SERVIÇO EXISTENTE
     * 
     * @param servico Objeto ServicoEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(ServicoEntity servico) throws SQLException {
        String sql = "UPDATE servico SET descricao = ? WHERE id = ?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getDescricao());
            stmt.setInt(2, servico.getId());
            stmt.executeUpdate();
        }
    }

    // ==================== MÉTODO DELETAR (DELETE) ====================

    /**
     * DELETA UM SERVIÇO DO BANCO DE DADOS PELO ID
     * 
     * @param id Identificador único do serviço a ser deletado
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM servico WHERE id = ?";

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
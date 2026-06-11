/**
 * CLASSE DE ACESSO A DADOS (DAO) PARA SERVIÇO
 * 
 * Esta classe é responsável por todas as operações de banco de dados
 * relacionadas à entidade Serviço.
 * 
 * Diferente de outras DAOs (Cliente, Produto), esta classe é APENAS PARA CONSULTA.
 * Os serviços são pré-cadastrados e não podem ser alterados via interface do usuário.
 * 
 * Funcionalidades:
 * - Listar todos os serviços (READ)
 * - Buscar serviço por descrição (READ com filtro)
 * - Buscar serviço por ID (READ)
 * 
 * NOTA: Não há métodos de inserir, atualizar ou deletar porque esta tela
 * é apenas de consulta. Os serviços são gerenciados diretamente no banco.
 * 
 * @author joao
 */
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.ServicoEntity; // Modelo/entidade Serviço
import com.eletronica.util.Database;       // Classe de conexão com o banco

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class ServicoDAO {
    
    // ==================== MÉTODO BUSCAR POR DESCRIÇÃO (READ COM FILTRO) ====================
    
    /**
     * BUSCA SERVIÇOS CUJA DESCRIÇÃO CONTENHA O TEXTO FORNECIDO
     * 
     * Utiliza ILIKE no PostgreSQL (busca case-insensitive, ignora maiúsculas/minúsculas).
     * O % no início e fim permite buscar textos que contenham o termo em qualquer posição.
     * 
     * Exemplo: buscarPorDescricao("tela") encontra "Troca de tela", "Limpeza de tela"
     * 
     * @param descricao Parte da descrição do serviço a ser buscada
     * @return Lista de serviços que atendem ao critério de busca
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<ServicoEntity> buscarPorDescricao(String descricao) throws SQLException {
        // Lista vazia para armazenar os resultados
        List<ServicoEntity> servicos = new ArrayList<>();
        
        // SQL: ILIKE = case-insensitive (não diferencia maiúsculas de minúsculas)
        // ORDER BY descricao ordena os resultados em ordem alfabética
        String sql = "SELECT * FROM servico WHERE descricao ILIKE ? ORDER BY descricao";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
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
    
    // ==================== MÉTODO LISTAR TODOS (READ) ====================
    
    /**
     * LISTA TODOS OS SERVIÇOS DO BANCO DE DADOS
     * 
     * Retorna uma lista com todos os serviços ordenados por descrição (A-Z).
     * 
     * @return Lista de ServicoEntity com todos os serviços
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<ServicoEntity> listarTodos() throws SQLException {
        List<ServicoEntity> servicos = new ArrayList<>();
        
        // SQL: Seleciona todos os serviços, ordenados por descrição (alfabética)
        String sql = "SELECT * FROM servico ORDER BY descricao";
        
        // try-with-resources: Statement e ResultSet são fechados automaticamente
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // ==================== PERCORRE O RESULTADO ====================
            while (rs.next()) {
                ServicoEntity s = new ServicoEntity();
                s.setId(rs.getInt("id"));
                s.setDescricao(rs.getString("descricao"));
                servicos.add(s);
            }
        }
        
        return servicos;  // Retorna a lista (pode ser vazia)
    }
    
    // ==================== MÉTODO BUSCAR POR ID (READ) ====================
    
    /**
     * BUSCA UM SERVIÇO PELO SEU ID
     * 
     * Retorna um único serviço ou null se não encontrado.
     * 
     * @param id Identificador único do serviço
     * @return ServicoEntity encontrado, ou null se não existir
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public ServicoEntity buscarPorId(int id) throws SQLException {
        // SQL: Seleciona serviço pelo ID
        String sql = "SELECT * FROM servico WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);  // WHERE id = ?
            ResultSet rs = stmt.executeQuery();
            
            // Se encontrou um registro (rs.next() retorna true)
            if (rs.next()) {
                ServicoEntity s = new ServicoEntity();
                s.setId(rs.getInt("id"));
                s.setDescricao(rs.getString("descricao"));
                return s;  // Retorna o serviço encontrado
            }
        }
        
        return null;  // Nenhum serviço encontrado com este ID
    }
}
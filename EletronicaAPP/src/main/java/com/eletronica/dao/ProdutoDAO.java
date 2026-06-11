/**
 * CLASSE DE ACESSO A DADOS (DAO) PARA PRODUTO
 * 
 * Esta classe é responsável por todas as operações de banco de dados
 * relacionadas à entidade Produto.
 * 
 * Produtos são os equipamentos eletrônicos que chegam para conserto/manutenção.
 * 
 * Funcionalidades:
 * - Inserir novo produto (CREATE)
 * - Listar todos os produtos (READ)
 * - Buscar produto por nome (READ com filtro)
 * - Atualizar dados do produto (UPDATE)
 * - Deletar produto (DELETE)
 * 
 * @author joao
 */
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.ProdutoEntity; // Modelo/entidade Produto
import com.eletronica.util.Database;       // Classe de conexão com o banco

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class ProdutoDAO {
    
    // ==================== MÉTODO INSERIR (CREATE) ====================
    
    /**
     * INSERE UM NOVO PRODUTO NO BANCO DE DADOS
     * 
     * Utiliza PreparedStatement para evitar SQL Injection.
     * O ID do produto é gerado automaticamente pelo banco (SERIAL) e
     * é atribuído ao objeto produto passado como parâmetro.
     * 
     * @param produto Objeto ProdutoEntity com os dados a serem inseridos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(ProdutoEntity produto) throws SQLException {
        // SQL de inserção (os '?' são placeholders para os valores)
        String sql = "INSERT INTO produtos (nome, tipo, modelo, marca, categoria, defeito) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        // try-with-resources: garante que o PreparedStatement será fechado automaticamente
        // Statement.RETURN_GENERATED_KEYS: solicita que o banco retorne o ID gerado
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ==================== PREENCHIMENTO DOS PARÂMETROS ====================
            // Os números correspondem à posição dos '?' na SQL
            stmt.setString(1, produto.getNome());       // Nome do produto (obrigatório)
            stmt.setString(2, produto.getTipo());       // Tipo (Smartphone, Notebook, etc.)
            stmt.setString(3, produto.getModelo());     // Modelo (iPhone 12, Galaxy S21, etc.)
            stmt.setString(4, produto.getMarca());      // Marca/Fabricante (Apple, Samsung, LG)
            stmt.setString(5, produto.getCategoria());  // Categoria (Celular, Computador, Áudio)
            stmt.setString(6, produto.getDefeito());    // Defeito apresentado (obrigatório)
            
            // ==================== EXECUÇÃO DO SQL ====================
            stmt.executeUpdate();
            
            // ==================== RECUPERAÇÃO DO ID GERADO ====================
            // O banco PostgreSQL gera o ID automaticamente (SERIAL)
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                produto.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto produto
            }
        }
    }
    
    // ==================== MÉTODO LISTAR TODOS (READ) ====================
    
    /**
     * LISTA TODOS OS PRODUTOS DO BANCO DE DADOS
     * 
     * Retorna uma lista com todos os produtos ordenados por nome (A-Z).
     * 
     * @return Lista de ProdutoEntity com todos os produtos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<ProdutoEntity> listarTodos() throws SQLException {
        // Lista vazia para armazenar os resultados
        List<ProdutoEntity> produtos = new ArrayList<>();
        
        // SQL: Seleciona todos os produtos, ordenados por nome (alfabética)
        String sql = "SELECT * FROM produtos ORDER BY nome";
        
        // try-with-resources: Statement e ResultSet são fechados automaticamente
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // ==================== PERCORRE O RESULTADO ====================
            // rs.next() move o cursor para o próximo registro
            // Retorna false quando não há mais registros
            while (rs.next()) {
                // Cria um novo objeto ProdutoEntity
                ProdutoEntity p = new ProdutoEntity();
                
                // Extrai os dados do ResultSet (mapeamento coluna → atributo)
                p.setId(rs.getInt("id"));                 // ID (int)
                p.setNome(rs.getString("nome"));          // Nome (String)
                p.setTipo(rs.getString("tipo"));          // Tipo (String)
                p.setModelo(rs.getString("modelo"));      // Modelo (String)
                p.setMarca(rs.getString("marca"));        // Marca (String)
                p.setCategoria(rs.getString("categoria")); // Categoria (String)
                p.setDefeito(rs.getString("defeito"));    // Defeito (String)
                
                // Adiciona o produto à lista
                produtos.add(p);
            }
        }
        
        return produtos;  // Retorna a lista (pode ser vazia)
    }
    
    // ==================== MÉTODO BUSCAR POR NOME (READ COM FILTRO) ====================
    
    /**
     * BUSCA PRODUTOS CUJO NOME CONTENHA O TEXTO FORNECIDO
     * 
     * Utiliza ILIKE no PostgreSQL (busca case-insensitive, ignora maiúsculas/minúsculas).
     * O % no início e fim permite buscar textos que contenham o termo em qualquer posição.
     * 
     * Exemplo: buscarPorNome("iphone") encontra "iPhone 12", "iPhone 13", "iPhone X"
     * 
     * @param nome Parte do nome do produto a ser buscada
     * @return Lista de produtos que atendem ao critério de busca
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<ProdutoEntity> buscarPorNome(String nome) throws SQLException {
        List<ProdutoEntity> produtos = new ArrayList<>();
        
        // SQL: ILIKE = case-insensitive (não diferencia maiúsculas de minúsculas)
        String sql = "SELECT * FROM produtos WHERE nome ILIKE ? ORDER BY nome";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            // O % no início e fim torna a busca "contém"
            // Ex: nome = "sansung" → SQL fica "... WHERE nome ILIKE '%sansung%'"
            stmt.setString(1, "%" + nome + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ProdutoEntity p = new ProdutoEntity();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setTipo(rs.getString("tipo"));
                p.setModelo(rs.getString("modelo"));
                p.setMarca(rs.getString("marca"));
                p.setCategoria(rs.getString("categoria"));
                p.setDefeito(rs.getString("defeito"));
                produtos.add(p);
            }
        }
        
        return produtos;
    }
    
    // ==================== MÉTODO ATUALIZAR (UPDATE) ====================
    
    /**
     * ATUALIZA OS DADOS DE UM PRODUTO EXISTENTE
     * 
     * O produto é identificado pelo seu ID (chave primária).
     * Todos os campos são atualizados com os valores do objeto fornecido.
     * 
     * @param produto Objeto ProdutoEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(ProdutoEntity produto) throws SQLException {
        // SQL de atualização: SET define os novos valores, WHERE identifica o registro
        String sql = "UPDATE produtos SET nome=?, tipo=?, modelo=?, marca=?, categoria=?, defeito=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            // Preenche os parâmetros (na ordem do SET)
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getTipo());
            stmt.setString(3, produto.getModelo());
            stmt.setString(4, produto.getMarca());
            stmt.setString(5, produto.getCategoria());
            stmt.setString(6, produto.getDefeito());
            stmt.setInt(7, produto.getId());  // WHERE id = ?
            
            stmt.executeUpdate();  // Executa a atualização
        }
    }
    
    // ==================== MÉTODO DELETAR (DELETE) ====================
    
    /**
     * DELETA UM PRODUTO DO BANCO DE DADOS
     * 
     * O produto é identificado pelo seu ID.
     * 
     * ATENÇÃO: Se houver Ordens de Serviço vinculadas a este produto,
     * a exclusão pode falhar devido à chave estrangeira.
     * 
     * @param id Identificador único do produto a ser deletado
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void deletar(int id) throws SQLException {
        // SQL de deleção
        String sql = "DELETE FROM produtos WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);  // WHERE id = ?
            stmt.executeUpdate(); // Executa a deleção
        }
    }
}
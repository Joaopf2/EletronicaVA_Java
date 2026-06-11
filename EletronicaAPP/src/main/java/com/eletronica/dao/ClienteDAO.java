/**
 * CLASSE DE ACESSO A DADOS (DAO) PARA CLIENTE
 * 
 * Esta classe é responsável por todas as operações de banco de dados
 * relacionadas à entidade Cliente.
 * 
 * Funcionalidades:
 * - Inserir novo cliente (CREATE)
 * - Listar todos os clientes (READ)
 * - Buscar cliente por nome (READ com filtro)
 * - Atualizar dados do cliente (UPDATE)
 * - Deletar cliente (DELETE)
 * 
 * Padrão DAO (Data Access Object):
 * - Encapsula a lógica de acesso ao banco de dados
 * - Isola as operações SQL do resto da aplicação
 * - Facilita manutenção e testes
 * 
 * @author joao
 */
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.ClienteEntity; // Modelo/entidade Cliente
import com.eletronica.util.Database;       // Classe de conexão com o banco

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL (Connection, PreparedStatement, ResultSet)
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class ClienteDAO {
    
    // ==================== MÉTODO INSERIR (CREATE) ====================
    
    /**
     * INSERE UM NOVO CLIENTE NO BANCO DE DADOS
     * 
     * Utiliza PreparedStatement para evitar SQL Injection.
     * O ID do cliente é gerado automaticamente pelo banco (SERIAL) e
     * é atribuído ao objeto cliente passado como parâmetro.
     * 
     * @param cliente Objeto ClienteEntity com os dados a serem inseridos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(ClienteEntity cliente) throws SQLException {
        // Logs para debug (mostram o que está sendo inserido)
        System.out.println("=== INSERINDO NO BANCO ===");
        System.out.println("Nome: " + cliente.getNome());
        System.out.println("Email: " + cliente.getEmail());
        
        // SQL de inserção (os '?' são placeholders para os valores)
        // IMPORTANTE: O nome da tabela é "cliente" (singular) conforme seu banco
        String sql = "INSERT INTO cliente (nome, email, telefone, cnpjcpf, rg, ie) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("SQL: " + sql);
        
        // try-with-resources: garante que o PreparedStatement será fechado automaticamente
        // Statement.RETURN_GENERATED_KEYS: solicita que o banco retorne o ID gerado
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ==================== PREENCHIMENTO DOS PARÂMETROS ====================
            // Os números correspondem à posição dos '?' na SQL (1 = primeiro '?')
            stmt.setString(1, cliente.getNome());       // Nome
            stmt.setString(2, cliente.getEmail());      // Email
            stmt.setString(3, cliente.getTelefone());   // Telefone
            stmt.setString(4, cliente.getCnpjCpf());    // CPF ou CNPJ
            stmt.setString(5, cliente.getRg());         // RG
            stmt.setString(6, cliente.getIe());         // Inscrição Estadual (pode ser null)
            
            // ==================== EXECUÇÃO DO SQL ====================
            System.out.println("Executando update...");
            int linhas = stmt.executeUpdate();  // Retorna número de linhas afetadas
            System.out.println("Linhas afetadas: " + linhas);
            
            // ==================== RECUPERAÇÃO DO ID GERADO ====================
            // O banco PostgreSQL gera o ID automaticamente (SERIAL)
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                cliente.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto cliente
                System.out.println("ID gerado: " + cliente.getId());
            } else {
                System.out.println("Nenhum ID gerado!");
            }
            
            System.out.println("Inserção concluída!");
            
        } catch (SQLException e) {
            System.out.println("ERRO NA INSERÇÃO: " + e.getMessage());
            throw e;  // Repassa a exceção para o Controller tratar
        }
    }
    
    // ==================== MÉTODO LISTAR TODOS (READ) ====================
    
    /**
     * LISTA TODOS OS CLIENTES DO BANCO DE DADOS
     * 
     * Retorna uma lista com todos os clientes ordenados por nome.
     * 
     * @return Lista de ClienteEntity com todos os clientes
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<ClienteEntity> listarTodos() throws SQLException {
        // Lista vazia para armazenar os resultados
        List<ClienteEntity> clientes = new ArrayList<>();
        
        // SQL: Seleciona todos os clientes, ordenados por nome (A-Z)
        String sql = "SELECT * FROM cliente ORDER BY nome";
        
        // try-with-resources: Statement e ResultSet são fechados automaticamente
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // ==================== PERCORRE O RESULTADO ====================
            // rs.next() move o cursor para o próximo registro
            // Retorna false quando não há mais registros
            while (rs.next()) {
                // Cria um novo objeto ClienteEntity
                ClienteEntity c = new ClienteEntity();
                
                // Extrai os dados do ResultSet (mapeamento coluna → atributo)
                c.setId(rs.getInt("id"));                 // ID (int)
                c.setNome(rs.getString("nome"));          // Nome (String)
                c.setEmail(rs.getString("email"));        // Email (String)
                c.setTelefone(rs.getString("telefone"));  // Telefone (String)
                c.setCnpjCpf(rs.getString("cnpjcpf"));    // CPF/CNPJ (String)
                c.setRg(rs.getString("rg"));              // RG (String)
                c.setIe(rs.getString("ie"));              // IE (String, pode ser null)
                
                // Adiciona o cliente à lista
                clientes.add(c);
            }
        }
        
        return clientes;  // Retorna a lista (pode ser vazia)
    }
    
    // ==================== MÉTODO BUSCAR POR NOME (READ COM FILTRO) ====================
    
    /**
     * BUSCA CLIENTES CUJO NOME CONTENHA O TEXTO FORNECIDO
     * 
     * Utiliza ILIKE no PostgreSQL (busca case-insensitive, ignora maiúsculas/minúsculas).
     * O % no início e fim permite buscar textos que contenham o termo em qualquer posição.
     * 
     * Exemplo: buscarPorNome("silva") encontra "João Silva", "Silva Santos", "Ana Silvana"
     * 
     * @param nome Parte do nome a ser buscada
     * @return Lista de clientes que atendem ao critério de busca
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<ClienteEntity> buscarPorNome(String nome) throws SQLException {
        List<ClienteEntity> clientes = new ArrayList<>();
        
        // SQL: ILIKE = case-insensitive (não diferencia maiúsculas de minúsculas)
        String sql = "SELECT * FROM cliente WHERE nome ILIKE ? ORDER BY nome";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            // O % no início e fim torna a busca "contém"
            // Ex: nome = "silva" → SQL fica "... WHERE nome ILIKE '%silva%'"
            stmt.setString(1, "%" + nome + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ClienteEntity c = new ClienteEntity();
                c.setId(rs.getInt("id"));
                c.setNome(rs.getString("nome"));
                c.setEmail(rs.getString("email"));
                c.setTelefone(rs.getString("telefone"));
                c.setCnpjCpf(rs.getString("cnpjcpf"));
                c.setRg(rs.getString("rg"));
                c.setIe(rs.getString("ie"));
                clientes.add(c);
            }
        }
        
        return clientes;
    }
    
    // ==================== MÉTODO ATUALIZAR (UPDATE) ====================
    
    /**
     * ATUALIZA OS DADOS DE UM CLIENTE EXISTENTE
     * 
     * O cliente é identificado pelo seu ID (chave primária).
     * Todos os campos são atualizados com os valores do objeto fornecido.
     * 
     * @param cliente Objeto ClienteEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(ClienteEntity cliente) throws SQLException {
        // SQL de atualização: SET define os novos valores, WHERE identifica o registro
        String sql = "UPDATE cliente SET nome=?, email=?, telefone=?, cnpjcpf=?, rg=?, ie=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            // Preenche os parâmetros (na ordem do SET)
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getEmail());
            stmt.setString(3, cliente.getTelefone());
            stmt.setString(4, cliente.getCnpjCpf());
            stmt.setString(5, cliente.getRg());
            stmt.setString(6, cliente.getIe());
            stmt.setInt(7, cliente.getId());  // WHERE id = ?
            
            // Executa a atualização
            stmt.executeUpdate();
        }
    }
    
    // ==================== MÉTODO DELETAR (DELETE) ====================
    
    /**
     * DELETA UM CLIENTE DO BANCO DE DADOS
     * 
     * O cliente é identificado pelo seu ID.
     * 
     * ATENÇÃO: Se houver Ordens de Serviço vinculadas a este cliente,
     * a exclusão pode falhar devido à chave estrangeira (ON DELETE CASCADE).
     * 
     * @param id Identificador único do cliente a ser deletado
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void deletar(int id) throws SQLException {
        // SQL de deleção
        String sql = "DELETE FROM cliente WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);  // WHERE id = ?
            stmt.executeUpdate(); // Executa a deleção
        }
    }
}
/**
 * CLASSE DE ACESSO A DADOS (DAO) PARA ORDEM DE SERVIÇO (OS)
 * 
 * Esta classe é responsável por todas as operações de banco de dados
 * relacionadas à entidade OrdemServico.
 * 
 * Funcionalidades:
 * - Inserir nova OS (CREATE)
 * - Listar todas as OS (READ)
 * - Buscar OS por descrição (READ com filtro)
 * - Buscar OS por ID (READ)
 * - Atualizar OS completa (UPDATE)
 * - Atualizar apenas o status (UPDATE parcial)
 * - Deletar OS (DELETE)
 * 
 * Relacionamentos:
 * - Uma OS pertence a UM cliente (JOIN com tabela cliente)
 * - Uma OS pertence a UM produto (JOIN com tabela produtos)
 * 
 * @author joao
 */
package com.eletronica.dao;

// Imports da aplicação
import com.eletronica.model.OrdemServicoEntity; // Modelo/entidade Ordem de Serviço
import com.eletronica.util.Database;            // Classe de conexão com o banco

// Imports Java padrão
import java.sql.*;          // Classes para operações SQL
import java.time.LocalDate; // Para manipulação de datas
import java.util.ArrayList; // Lista para armazenar resultados
import java.util.List;      // Interface List para retornar coleções

public class OrdemServicoDAO {
    
    // ==================== MÉTODO INSERIR (CREATE) ====================
    
    /**
     * INSERE UMA NOVA ORDEM DE SERVIÇO NO BANCO DE DADOS
     * 
     * A data da OS é definida como a data atual do sistema.
     * O ID é gerado automaticamente pelo banco (SERIAL).
     * 
     * @param os Objeto OrdemServicoEntity com os dados a serem inseridos
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void inserir(OrdemServicoEntity os) throws SQLException {
        // SQL de inserção
        // data: a data atual é definida no momento da criação
        String sql = "INSERT INTO ordens_servico (orcamento, data, descricao, status, id_cliente, id_produto) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ==================== PREENCHIMENTO DOS PARÂMETROS ====================
            stmt.setString(1, os.getOrcamento());                    // Orçamento (String)
            stmt.setDate(2, Date.valueOf(os.getData()));             // Data (converte LocalDate → SQL Date)
            stmt.setString(3, os.getDescricao());                    // Descrição do serviço
            stmt.setString(4, os.getStatus().name());                // Status (EM_ESPERA, EM_ANDAMENTO, PRONTO)
            stmt.setInt(5, os.getIdCliente());                       // ID do cliente (FK)
            stmt.setInt(6, os.getIdProduto());                       // ID do produto (FK)
            
            // ==================== EXECUÇÃO DO SQL ====================
            stmt.executeUpdate();
            
            // ==================== RECUPERAÇÃO DO ID GERADO ====================
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                os.setId(rs.getInt(1));  // Atribui o ID gerado ao objeto OS
            }
        }
    }
    
    // ==================== MÉTODO LISTAR TODAS (READ) ====================
    
    /**
     * LISTA TODAS AS ORDENS DE SERVIÇO DO BANCO DE DADOS
     * 
     * Realiza JOIN com as tabelas cliente e produtos para trazer
     * os nomes do cliente e produto (evita consultas adicionais).
     * 
     * @return Lista de OrdemServicoEntity com todas as OS
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<OrdemServicoEntity> listarTodas() throws SQLException {
        List<OrdemServicoEntity> ordens = new ArrayList<>();
        
        // SQL com JOINs para buscar dados relacionados
        // os.* = todas as colunas da tabela ordens_servico
        // c.nome = nome do cliente (apelido: nome_cliente)
        // p.nome = nome do produto (apelido: nome_produto)
        String sql = "SELECT os.*, c.nome as nome_cliente, p.nome as nome_produto " +
                     "FROM ordens_servico os " +
                     "INNER JOIN cliente c ON os.id_cliente = c.id " +
                     "INNER JOIN produtos p ON os.id_produto = p.id " +
                     "ORDER BY os.data DESC";  // Mais recentes primeiro
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ordens.add(criarOrdemServico(rs));  // Método auxiliar para criar objeto
            }
        }
        return ordens;
    }
    
    // ==================== MÉTODO BUSCAR POR DESCRIÇÃO (READ COM FILTRO) ====================
    
    /**
     * BUSCA ORDENS DE SERVIÇO CUJA DESCRIÇÃO CONTENHA O TEXTO FORNECIDO
     * 
     * Utiliza ILIKE no PostgreSQL (busca case-insensitive).
     * Retorna as OS ordenadas por data (mais recentes primeiro).
     * 
     * @param descricao Parte da descrição a ser buscada
     * @return Lista de OS que atendem ao critério de busca
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public List<OrdemServicoEntity> buscarPorDescricao(String descricao) throws SQLException {
        List<OrdemServicoEntity> ordens = new ArrayList<>();
        
        String sql = "SELECT os.*, c.nome as nome_cliente, p.nome as nome_produto " +
                     "FROM ordens_servico os " +
                     "INNER JOIN cliente c ON os.id_cliente = c.id " +
                     "INNER JOIN produtos p ON os.id_produto = p.id " +
                     "WHERE os.descricao ILIKE ? " +  // Busca case-insensitive
                     "ORDER BY os.data DESC";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + descricao + "%");  // % = qualquer caractere antes/depois
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ordens.add(criarOrdemServico(rs));
            }
        }
        return ordens;
    }
    
    // ==================== MÉTODO BUSCAR POR ID (READ) ====================
    
    /**
     * BUSCA UMA ORDEM DE SERVIÇO PELO SEU ID
     * 
     * Retorna uma única OS ou null se não encontrada.
     * 
     * @param id Identificador único da OS
     * @return OrdemServicoEntity encontrada, ou null se não existir
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public OrdemServicoEntity buscarPorId(int id) throws SQLException {
        String sql = "SELECT os.*, c.nome as nome_cliente, p.nome as nome_produto " +
                     "FROM ordens_servico os " +
                     "INNER JOIN cliente c ON os.id_cliente = c.id " +
                     "INNER JOIN produtos p ON os.id_produto = p.id " +
                     "WHERE os.id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return criarOrdemServico(rs);
            }
        }
        return null;  // Nenhuma OS encontrada com este ID
    }
    
    // ==================== MÉTODO ATUALIZAR (UPDATE COMPLETO) ====================
    
    /**
     * ATUALIZA TODOS OS DADOS DE UMA ORDEM DE SERVIÇO
     * 
     * NOTA: A data NÃO é atualizada neste método (a data original é mantida).
     * Para mudar o status, considere usar atualizarStatus().
     * 
     * @param os Objeto OrdemServicoEntity com os dados atualizados
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizar(OrdemServicoEntity os) throws SQLException {
        // SQL de atualização (data NÃO é atualizada)
        String sql = "UPDATE ordens_servico SET orcamento=?, descricao=?, status=?, id_cliente=?, id_produto=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, os.getOrcamento());
            stmt.setString(2, os.getDescricao());
            stmt.setString(3, os.getStatus().name());  // Converte Enum para String
            stmt.setInt(4, os.getIdCliente());
            stmt.setInt(5, os.getIdProduto());
            stmt.setInt(6, os.getId());  // WHERE id = ?
            
            stmt.executeUpdate();
        }
    }
    
    // ==================== MÉTODO ATUALIZAR APENAS STATUS (UPDATE PARCIAL) ====================
    
    /**
     * ATUALIZA APENAS O STATUS DE UMA ORDEM DE SERVIÇO
     * 
     * Este método é útil quando apenas o status precisa ser alterado
     * (ex: técnico mudando de "EM ANDAMENTO" para "PRONTO").
     * 
     * @param id Identificador da OS
     * @param status Novo status (EM_ESPERA, EM_ANDAMENTO, PRONTO)
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void atualizarStatus(int id, OrdemServicoEntity.TipoStatus status) throws SQLException {
        String sql = "UPDATE ordens_servico SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());  // Converte Enum para String
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
    
    // ==================== MÉTODO DELETAR (DELETE) ====================
    
    /**
     * DELETA UMA ORDEM DE SERVIÇO DO BANCO DE DADOS
     * 
     * @param id Identificador único da OS a ser deletada
     * @throws SQLException Se ocorrer erro durante a operação no banco
     */
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM ordens_servico WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // ==================== MÉTODO AUXILIAR ====================
    
    /**
     * MÉTODO AUXILIAR PARA CRIAR UM OBJETO ORDEMSERVICO A PARTIR DO RESULTSET
     * 
     * Centraliza a criação do objeto, evitando código duplicado.
     * 
     * @param rs ResultSet da consulta SQL
     * @return Objeto OrdemServicoEntity preenchido
     * @throws SQLException Se ocorrer erro ao acessar os dados do ResultSet
     */
    private OrdemServicoEntity criarOrdemServico(ResultSet rs) throws SQLException {
        OrdemServicoEntity os = new OrdemServicoEntity();
        
        // ==================== CAMPOS BÁSICOS ====================
        os.setId(rs.getInt("id"));
        os.setOrcamento(rs.getString("orcamento"));
        os.setData(rs.getDate("data").toLocalDate());  // SQL Date → LocalDate
        os.setDescricao(rs.getString("descricao"));
        
        // ==================== CONVERSÃO DO STATUS (String → Enum) ====================
        String statusStr = rs.getString("status");
        switch (statusStr) {
            case "EM_ESPERA":
                os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
                break;
            case "EM_ANDAMENTO":
                os.setStatus(OrdemServicoEntity.TipoStatus.EM_ANDAMENTO);
                break;
            case "PRONTO":
                os.setStatus(OrdemServicoEntity.TipoStatus.PRONTO);
                break;
            default:
                os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);  // Valor padrão
        }
        
        // ==================== DADOS DOS RELACIONAMENTOS (JOIN) ====================
        os.setIdCliente(rs.getInt("id_cliente"));
        os.setNomeCliente(rs.getString("nome_cliente"));  // Vem do JOIN com cliente
        os.setIdProduto(rs.getInt("id_produto"));
        os.setNomeProduto(rs.getString("nome_produto"));  // Vem do JOIN com produtos
        
        return os;
    }
}
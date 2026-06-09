/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.dao;

/**
 *
 * @author joao
 */

import com.eletronica.model.OrdemServicoEntity;
import com.eletronica.util.Database;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdemServicoDAO {
    
    // Inserir nova OS
    public void inserir(OrdemServicoEntity os) throws SQLException {
        String sql = "INSERT INTO ordens_servico (orcamento, data, descricao, status, id_cliente, id_produto) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, os.getOrcamento());
            stmt.setDate(2, Date.valueOf(os.getData()));
            stmt.setString(3, os.getDescricao());
            stmt.setString(4, os.getStatus().name());
            stmt.setInt(5, os.getIdCliente());
            stmt.setInt(6, os.getIdProduto());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                os.setId(rs.getInt(1));
            }
        }
    }
    
    // Listar todas as OS
    public List<OrdemServicoEntity> listarTodas() throws SQLException {
        List<OrdemServicoEntity> ordens = new ArrayList<>();
        String sql = "SELECT os.*, c.nome as nome_cliente, p.nome as nome_produto " +
                     "FROM ordens_servico os " +
                     "INNER JOIN cliente c ON os.id_cliente = c.id " +
                     "INNER JOIN produtos p ON os.id_produto = p.id " +
                     "ORDER BY os.data DESC";
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ordens.add(criarOrdemServico(rs));
            }
        }
        return ordens;
    }
    
    // Buscar OS por descrição
    public List<OrdemServicoEntity> buscarPorDescricao(String descricao) throws SQLException {
        List<OrdemServicoEntity> ordens = new ArrayList<>();
        String sql = "SELECT os.*, c.nome as nome_cliente, p.nome as nome_produto " +
                     "FROM ordens_servico os " +
                     "INNER JOIN cliente c ON os.id_cliente = c.id " +
                     "INNER JOIN produtos p ON os.id_produto = p.id " +
                     "WHERE os.descricao ILIKE ? " +
                     "ORDER BY os.data DESC";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + descricao + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ordens.add(criarOrdemServico(rs));
            }
        }
        return ordens;
    }
    
    // Buscar OS por ID
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
        return null;
    }
    
    // Atualizar OS completa
    public void atualizar(OrdemServicoEntity os) throws SQLException {
        String sql = "UPDATE ordens_servico SET orcamento=?, descricao=?, status=?, id_cliente=?, id_produto=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, os.getOrcamento());
            stmt.setString(2, os.getDescricao());
            stmt.setString(3, os.getStatus().name());
            stmt.setInt(4, os.getIdCliente());
            stmt.setInt(5, os.getIdProduto());
            stmt.setInt(6, os.getId());
            
            stmt.executeUpdate();
        }
    }
    
    // Atualizar apenas o status
    public void atualizarStatus(int id, OrdemServicoEntity.TipoStatus status) throws SQLException {
        String sql = "UPDATE ordens_servico SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
    
    // Deletar OS
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM ordens_servico WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // Método auxiliar para criar objeto a partir do ResultSet
    private OrdemServicoEntity criarOrdemServico(ResultSet rs) throws SQLException {
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setId(rs.getInt("id"));
        os.setOrcamento(rs.getString("orcamento"));
        os.setData(rs.getDate("data").toLocalDate());
        os.setDescricao(rs.getString("descricao"));
        
        // Converter String para Enum
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
                os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
        }
        
        os.setIdCliente(rs.getInt("id_cliente"));
        os.setNomeCliente(rs.getString("nome_cliente"));
        os.setIdProduto(rs.getInt("id_produto"));
        os.setNomeProduto(rs.getString("nome_produto"));
        
        return os;
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.dao;

/**
 *
 * @author joao
 */


import com.eletronica.model.ServicoEntity;
import com.eletronica.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {
    
    // Buscar serviços por descrição (contém)
    public List<ServicoEntity> buscarPorDescricao(String descricao) throws SQLException {
        List<ServicoEntity> servicos = new ArrayList<>();
        String sql = "SELECT * FROM servico WHERE descricao ILIKE ? ORDER BY descricao";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + descricao + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ServicoEntity s = new ServicoEntity();
                s.setId(rs.getInt("id"));
                s.setDescricao(rs.getString("descricao"));
                servicos.add(s);
            }
        }
        return servicos;
    }
    
    // Listar todos os serviços
    public List<ServicoEntity> listarTodos() throws SQLException {
        List<ServicoEntity> servicos = new ArrayList<>();
        String sql = "SELECT * FROM servico ORDER BY descricao";
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ServicoEntity s = new ServicoEntity();
                s.setId(rs.getInt("id"));
                s.setDescricao(rs.getString("descricao"));
                servicos.add(s);
            }
        }
        return servicos;
    }
    
    // Buscar serviço por ID
    public ServicoEntity buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM servico WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                ServicoEntity s = new ServicoEntity();
                s.setId(rs.getInt("id"));
                s.setDescricao(rs.getString("descricao"));
                return s;
            }
        }
        return null;
    }
}
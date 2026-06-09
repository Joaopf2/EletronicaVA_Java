/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.dao;

/**
 *
 * @author joao
 */

import com.eletronica.model.GrupoUsuarioEntity;
import com.eletronica.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrupoUsuarioDAO {
    
    // Inserir
    public void inserir(GrupoUsuarioEntity grupo) throws SQLException {
        String sql = "INSERT INTO grupos_usuarios (descricao, grupo, permissao, manter_usuario, manter_servico) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, grupo.getDescricao());
            stmt.setInt(2, grupo.getGrupo());
            stmt.setBoolean(3, grupo.isPermissao());
            stmt.setBoolean(4, grupo.isManterUsuario());
            stmt.setBoolean(5, grupo.isManterServico());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                grupo.setId(rs.getInt(1));
            }
        }
    }
    
    // Listar todos
    public List<GrupoUsuarioEntity> listarTodos() throws SQLException {
        List<GrupoUsuarioEntity> grupos = new ArrayList<>();
        String sql = "SELECT * FROM grupos_usuarios ORDER BY grupo";
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
    
    // Buscar por ID
    public GrupoUsuarioEntity buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM grupos_usuarios WHERE id = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                GrupoUsuarioEntity g = new GrupoUsuarioEntity();
                g.setId(rs.getInt("id"));
                g.setDescricao(rs.getString("descricao"));
                g.setGrupo(rs.getInt("grupo"));
                g.setPermissao(rs.getBoolean("permissao"));
                g.setManterUsuario(rs.getBoolean("manter_usuario"));
                g.setManterServico(rs.getBoolean("manter_servico"));
                return g;
            }
        }
        return null;
    }
    
    // Buscar por descrição
    public List<GrupoUsuarioEntity> buscarPorDescricao(String descricao) throws SQLException {
        List<GrupoUsuarioEntity> grupos = new ArrayList<>();
        String sql = "SELECT * FROM grupos_usuarios WHERE descricao ILIKE ? ORDER BY grupo";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
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
    
    // Atualizar
    public void atualizar(GrupoUsuarioEntity grupo) throws SQLException {
        String sql = "UPDATE grupos_usuarios SET descricao=?, grupo=?, permissao=?, " +
                     "manter_usuario=?, manter_servico=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, grupo.getDescricao());
            stmt.setInt(2, grupo.getGrupo());
            stmt.setBoolean(3, grupo.isPermissao());
            stmt.setBoolean(4, grupo.isManterUsuario());
            stmt.setBoolean(5, grupo.isManterServico());
            stmt.setInt(6, grupo.getId());
            stmt.executeUpdate();
        }
    }
    
    // Deletar
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM grupos_usuarios WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
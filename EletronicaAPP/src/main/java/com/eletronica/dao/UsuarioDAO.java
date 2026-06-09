/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.dao;

/**
 *
 * @author joao
 */

import com.eletronica.model.UsuarioEntity;
import com.eletronica.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    
    // Inserir
    public void inserir(UsuarioEntity usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, email, senha, id_grupo_usuario) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setInt(4, usuario.getIdGrupoUsuario());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getInt(1));
            }
        }
    }
    
    // Listar todos com JOIN para pegar o nome do grupo
    public List<UsuarioEntity> listarTodos() throws SQLException {
        List<UsuarioEntity> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u " +
                     "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id " +
                     "ORDER BY u.nome";
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
    
    // Buscar por nome
    public List<UsuarioEntity> buscarPorNome(String nome) throws SQLException {
        List<UsuarioEntity> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u " +
                     "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id " +
                     "WHERE u.nome ILIKE ? ORDER BY u.nome";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%");
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
    
    // Buscar por ID
    public UsuarioEntity buscarPorId(int id) throws SQLException {
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u " +
                     "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id " +
                     "WHERE u.id = ?";
        
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
    
    // Buscar por email (para login)
    public UsuarioEntity buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT u.*, g.descricao as nome_grupo FROM usuarios u " +
                     "INNER JOIN grupos_usuarios g ON u.id_grupo_usuario = g.id " +
                     "WHERE u.email = ?";
        
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
    
    // Atualizar
    public void atualizar(UsuarioEntity usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome=?, email=?, senha=?, id_grupo_usuario=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setInt(4, usuario.getIdGrupoUsuario());
            stmt.setInt(5, usuario.getId());
            stmt.executeUpdate();
        }
    }
    
    // Deletar
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // Validar login
    public boolean validarLogin(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.dao;

/**
 *
 * @author joao
 */

import com.eletronica.model.ProdutoEntity;
import com.eletronica.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    
    public void inserir(ProdutoEntity produto) throws SQLException {
        String sql = "INSERT INTO produtos (nome, tipo, modelo, marca, categoria, defeito) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getTipo());
            stmt.setString(3, produto.getModelo());
            stmt.setString(4, produto.getMarca());
            stmt.setString(5, produto.getCategoria());
            stmt.setString(6, produto.getDefeito());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                produto.setId(rs.getInt(1));
            }
        }
    }
    
    public List<ProdutoEntity> listarTodos() throws SQLException {
        List<ProdutoEntity> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos ORDER BY nome";
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
    
    public List<ProdutoEntity> buscarPorNome(String nome) throws SQLException {
        List<ProdutoEntity> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE nome ILIKE ? ORDER BY nome";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
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
    
    public void atualizar(ProdutoEntity produto) throws SQLException {
        String sql = "UPDATE produtos SET nome=?, tipo=?, modelo=?, marca=?, categoria=?, defeito=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getTipo());
            stmt.setString(3, produto.getModelo());
            stmt.setString(4, produto.getMarca());
            stmt.setString(5, produto.getCategoria());
            stmt.setString(6, produto.getDefeito());
            stmt.setInt(7, produto.getId());
            stmt.executeUpdate();
        }
    }
    
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
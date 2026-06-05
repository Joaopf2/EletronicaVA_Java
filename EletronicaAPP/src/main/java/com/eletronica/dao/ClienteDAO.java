/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.dao;

/**
 *
 * @author joao
 */


import com.eletronica.model.ClienteEntity;
import com.eletronica.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    
    public void inserir(ClienteEntity cliente) throws SQLException {
    System.out.println("=== INSERINDO NO BANCO ===");
    System.out.println("Nome: " + cliente.getNome());
    System.out.println("Email: " + cliente.getEmail());
    
    String sql = "INSERT INTO cliente (nome, email, telefone, cnpjcpf, rg, ie) VALUES (?, ?, ?, ?, ?, ?)";
    System.out.println("SQL: " + sql);
    
    try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, cliente.getNome());
        stmt.setString(2, cliente.getEmail());
        stmt.setString(3, cliente.getTelefone());
        stmt.setString(4, cliente.getCnpjCpf());
        stmt.setString(5, cliente.getRg());
        stmt.setString(6, cliente.getIe());
        
        System.out.println("Executando update...");
        int linhas = stmt.executeUpdate();
        System.out.println("Linhas afetadas: " + linhas);
        
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            cliente.setId(rs.getInt(1));
            System.out.println("ID gerado: " + cliente.getId());
        } else {
            System.out.println("Nenhum ID gerado!");
        }
        
        System.out.println("Inserção concluída!");
    } catch (SQLException e) {
        System.out.println("ERRO NA INSERÇÃO: " + e.getMessage());
        throw e;
    }
}
    
    public List<ClienteEntity> listarTodos() throws SQLException {
        List<ClienteEntity> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nome";
        
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
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
    
    public List<ClienteEntity> buscarPorNome(String nome) throws SQLException {
        List<ClienteEntity> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente WHERE nome ILIKE ? ORDER BY nome";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
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
    
    public void atualizar(ClienteEntity cliente) throws SQLException {
        String sql = "UPDATE cliente SET nome=?, email=?, telefone=?, cnpjcpf=?, rg=?, ie=? WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getEmail());
            stmt.setString(3, cliente.getTelefone());
            stmt.setString(4, cliente.getCnpjCpf());
            stmt.setString(5, cliente.getRg());
            stmt.setString(6, cliente.getIe());
            stmt.setInt(7, cliente.getId());
            
            stmt.executeUpdate();
        }
    }
    
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id=?";
        
        try (PreparedStatement stmt = Database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.model;

/**
 *
 * @author joao
 */

public class UsuarioEntity {
    private int id;
    private String nome;
    private String email;
    private String senha;
    private int idGrupoUsuario;
    private GrupoUsuarioEntity grupoUsuario;
    
    // Construtores
    public UsuarioEntity() {}
    
    public UsuarioEntity(String nome, String email, String senha, int idGrupoUsuario) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.idGrupoUsuario = idGrupoUsuario;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public int getIdGrupoUsuario() { return idGrupoUsuario; }
    public void setIdGrupoUsuario(int idGrupoUsuario) { this.idGrupoUsuario = idGrupoUsuario; }
    
    public GrupoUsuarioEntity getGrupoUsuario() { return grupoUsuario; }
    public void setGrupoUsuario(GrupoUsuarioEntity grupoUsuario) { this.grupoUsuario = grupoUsuario; }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.model;

/**
 *
 * @author joao
 */

public class ClienteEntity {
    private int id;
    private String nome;
    private String email;
    private String telefone;
    private String cnpjCpf;
    private String rg;
    private String ie; 
    
    // Construtores
    public ClienteEntity() {}
    
    public ClienteEntity(String nome, String email, String telefone, 
                         String cnpjCpf, String rg, String ie) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cnpjCpf = cnpjCpf;
        this.rg = rg;
        this.ie = ie;
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    
    public String getCnpjCpf() { return cnpjCpf; }
    public void setCnpjCpf(String cnpjCpf) { this.cnpjCpf = cnpjCpf; }
    
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    
    public String getIe() { return ie; }
    public void setIe(String ie) { this.ie = ie; }
}
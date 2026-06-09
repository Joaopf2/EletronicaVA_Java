/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.model;

/**
 *
 * @author joao
 */

public class GrupoUsuarioEntity {
    private int id;
    private String descricao;
    private int grupo;
    private boolean permissao;
    private boolean manterUsuario;
    private boolean manterServico;
    
    public GrupoUsuarioEntity() {}
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public int getGrupo() { return grupo; }
    public void setGrupo(int grupo) { this.grupo = grupo; }
    
    public boolean isPermissao() { return permissao; }
    public void setPermissao(boolean permissao) { this.permissao = permissao; }
    
    public boolean isManterUsuario() { return manterUsuario; }
    public void setManterUsuario(boolean manterUsuario) { this.manterUsuario = manterUsuario; }
    
    public boolean isManterServico() { return manterServico; }
    public void setManterServico(boolean manterServico) { this.manterServico = manterServico; }
    
    @Override
    public String toString() {
        return descricao; // Para mostrar no ComboBox
    }
}
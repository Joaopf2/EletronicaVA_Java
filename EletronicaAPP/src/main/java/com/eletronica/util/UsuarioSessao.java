/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.util;

/**
 *
 * @author joao
 */

import com.eletronica.model.GrupoUsuarioEntity;
import com.eletronica.model.UsuarioEntity;

public class UsuarioSessao {
    private static UsuarioEntity usuarioLogado;
    private static GrupoUsuarioEntity grupoLogado;
    
    public static void login(UsuarioEntity usuario, GrupoUsuarioEntity grupo) {
        usuarioLogado = usuario;
        grupoLogado = grupo;
    }
    
    public static void logout() {
        usuarioLogado = null;
        grupoLogado = null;
    }
    
    public static UsuarioEntity getUsuarioLogado() {
        return usuarioLogado;
    }
    
    public static GrupoUsuarioEntity getGrupoLogado() {
        return grupoLogado;
    }
    
    public static boolean isLogado() {
        return usuarioLogado != null;
    }
    
    // Métodos de verificação de permissão
    public static boolean podeManterUsuario() {
        return grupoLogado != null && grupoLogado.isManterUsuario();
    }
    
    public static boolean podeManterServico() {
        return grupoLogado != null && grupoLogado.isManterServico();
    }
    
    public static boolean podeDeletarOS() {
        // Apenas Administrador (grupo 1) pode deletar OS
        return grupoLogado != null && grupoLogado.getGrupo() == 1;
    }
    
    public static boolean podeEditarOS() {
        // Administrador (1) e Técnico (2) podem editar
        return grupoLogado != null && (grupoLogado.getGrupo() == 1 || grupoLogado.getGrupo() == 2);
    }
    
    public static boolean podeCriarOS() {
        // Todos com permissao true podem criar OS
        return grupoLogado != null && grupoLogado.isPermissao();
    }
    
    public static boolean podeVerBotaoDeletar() {
        return podeDeletarOS();
    }
    
    public static boolean podeVerBotaoEditar() {
        return podeEditarOS();
    }
}
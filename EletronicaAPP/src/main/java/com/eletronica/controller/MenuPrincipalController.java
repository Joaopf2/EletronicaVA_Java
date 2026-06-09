/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuPrincipalController {
    
    @FXML private Button btnClientes;
    @FXML private Button btnProdutos;
    @FXML private Button btnServicos;
    @FXML private Button btnOrdens;
    //@FXML private Button btnUsuarios;
    @FXML private Button btnGrupoUsuario;
    
    @FXML
    public void initialize() {
         System.out.println("=== INICIALIZANDO MENU ===");
    
    // Verificar se os botões foram carregados
    System.out.println("btnClientes: " + (btnClientes == null ? "NULL" : "OK"));
    System.out.println("btnProdutos: " + (btnProdutos == null ? "NULL" : "OK"));
    System.out.println("btnServicos: " + (btnServicos == null ? "NULL" : "OK"));
    System.out.println("btnOrdens: " + (btnOrdens == null ? "NULL" : "OK"));
    System.out.println("btnGrupoUsuario: " + (btnGrupoUsuario == null ? "NULL" : "OK"));
    
    if (btnGrupoUsuario == null) {
        System.out.println("ERRO: btnGrupoUsuario está NULL! Verifique o fx:id no FXML.");
        return;
    }
    
    btnClientes.setOnAction(e -> abrirTelaClientes());
    btnProdutos.setOnAction(e -> abrirTelaProdutos());
    btnServicos.setOnAction(e -> abrirTelaServicos());
    btnOrdens.setOnAction(e -> abrirTelaOrdens());
    btnGrupoUsuario.setOnAction(e -> abrirTelaGrupoUsuarios());
    
    System.out.println("Menu inicializado com sucesso!");
    }
    
    private void abrirTelaClientes() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmCliente.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Clientes");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void abrirTelaProdutos() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmProduto.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Produtos");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void abrirTelaServicos() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmServico.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Serviços");
            stage.setScene(new Scene(root, 800, 400));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void abrirTelaOrdens() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmOrdemServico.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Ordens de Serviço");
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   /* private void abrirTelaUsuarios() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmUsuario.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Usuários");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
   private void abrirTelaGrupoUsuarios() {
    System.out.println("=== ABRINDO TELA GRUPO USUARIO ===");
    try {
        String caminho = "/com/eletronica/view/FrmGrupoUsuario.fxml";
        System.out.println("Procurando arquivo: " + caminho);
        
        java.net.URL url = getClass().getResource(caminho);
        if (url == null) {
            System.out.println("ERRO: Arquivo não encontrado!");
            System.out.println("Caminhos disponíveis:");
            java.net.URL root = getClass().getResource("/");
            System.out.println("Root: " + root);
            return;
        }
        
        System.out.println("Arquivo encontrado em: " + url.getPath());
        Parent root = FXMLLoader.load(url);
        Stage stage = new Stage();
        stage.setTitle("Cadastro de Grupos de Usuários");
        stage.setScene(new Scene(root, 1000, 900));
        stage.show();
        System.out.println("Tela aberta com sucesso!");
        
    } catch (Exception e) {
        System.out.println("ERRO DETALHADO:");
        System.out.println("Mensagem: " + e.getMessage());
        System.out.println("Causa: " + e.getCause());
        e.printStackTrace();
    }
}
}
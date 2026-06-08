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
    //@FXML private Button btnOrdens;
    //@FXML private Button btnUsuarios;
    
    @FXML
    public void initialize() {
        btnClientes.setOnAction(e -> abrirTelaClientes());
        btnProdutos.setOnAction(e -> abrirTelaProdutos());
        btnServicos.setOnAction(e -> abrirTelaServicos());
       // btnOrdens.setOnAction(e -> abrirTelaOrdens());
       // btnUsuarios.setOnAction(e -> abrirTelaUsuarios());
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
    /*
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
    
    private void abrirTelaUsuarios() {
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
}
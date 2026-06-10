/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */


import com.eletronica.dao.UsuarioDAO;
import com.eletronica.model.GrupoUsuarioEntity;
import com.eletronica.model.UsuarioEntity;
import com.eletronica.util.UsuarioSessao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnEntrar;
    @FXML private Button btnCancelar;
    @FXML private Label lblMensagem;
    
    private UsuarioDAO usuarioDAO;
    
    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        
        btnEntrar.setOnAction(e -> fazerLogin());
        btnCancelar.setOnAction(e -> System.exit(0));
        txtSenha.setOnAction(e -> fazerLogin());
    }
    
    private void fazerLogin() {
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText();
        
        if (email.isEmpty()) {
            lblMensagem.setText("Digite o email!");
            return;
        }
        
        if (senha.isEmpty()) {
            lblMensagem.setText("Digite a senha!");
            return;
        }
        
        try {
            UsuarioEntity usuario = usuarioDAO.buscarPorEmailComGrupo(email);
            
            if (usuario == null) {
                lblMensagem.setText("Usuário não encontrado!");
                return;
            }
            
            if (!usuario.getSenha().equals(senha)) {
                lblMensagem.setText("Senha incorreta!");
                return;
            }
            
            // Login bem-sucedido
            UsuarioSessao.login(usuario, usuario.getGrupo());
            
            // Abrir menu principal
            abrirMenuPrincipal();
            
            // Fechar login
            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            lblMensagem.setText("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void abrirMenuPrincipal() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eletronica/view/FrmMenuPrincipal.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Eletrônica - Sistema de Gestão");
        stage.setScene(new Scene(root, 1200, 800));
        stage.setMaximized(true);
        stage.show();
    }
}
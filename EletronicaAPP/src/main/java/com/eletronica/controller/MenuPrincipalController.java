/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */

import com.eletronica.util.UsuarioSessao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MenuPrincipalController {

    @FXML private Button btnClientes;
    @FXML private Button btnProdutos;
    @FXML private Button btnServicos;
    @FXML private Button btnOrdens;
    @FXML private Button btnUsuarios;
    @FXML private Button btnGrupoUsuario;
    @FXML private Label lblUsuarioLogado;
    @FXML private Label lblPerfil;

    @FXML
    public void initialize() {
        System.out.println("=== INICIALIZANDO MENU ===");

        // Verificar se o usuário está logado
        if (!UsuarioSessao.isLogado()) {
            System.out.println("ERRO: Nenhum usuário logado!");
            return;
        }

        // Exibir informações do usuário logado
        if (lblUsuarioLogado != null) {
            lblUsuarioLogado.setText("Usuário: " + UsuarioSessao.getUsuarioLogado().getNome());
        }
        if (lblPerfil != null) {
            lblPerfil.setText("Perfil: " + UsuarioSessao.getGrupoLogado().getDescricao());
        }

        // CONTROLE DE PERMISSÕES - Esconder botões que o usuário não pode acessar
        if (!UsuarioSessao.podeManterUsuario()) {
            if (btnUsuarios != null) btnUsuarios.setVisible(false);
            if (btnGrupoUsuario != null) btnGrupoUsuario.setVisible(false);
            System.out.println("Botões de usuário/grupo ocultos (sem permissão)");
        }

        // Verificar se os botões foram carregados
        System.out.println("btnClientes: " + (btnClientes == null ? "NULL" : "OK"));
        System.out.println("btnProdutos: " + (btnProdutos == null ? "NULL" : "OK"));
        System.out.println("btnServicos: " + (btnServicos == null ? "NULL" : "OK"));
        System.out.println("btnOrdens: " + (btnOrdens == null ? "NULL" : "OK"));
        System.out.println("btnUsuarios: " + (btnUsuarios == null ? "NULL" : "OK"));
        System.out.println("btnGrupoUsuario: " + (btnGrupoUsuario == null ? "NULL" : "OK"));

        // Configurar ações dos botões
        if (btnClientes != null) btnClientes.setOnAction(e -> abrirTelaClientes());
        if (btnProdutos != null) btnProdutos.setOnAction(e -> abrirTelaProdutos());
        if (btnServicos != null) btnServicos.setOnAction(e -> abrirTelaServicos());
        if (btnOrdens != null) btnOrdens.setOnAction(e -> abrirTelaOrdens());
        if (btnUsuarios != null) btnUsuarios.setOnAction(e -> abrirTelaUsuarios());
        if (btnGrupoUsuario != null) btnGrupoUsuario.setOnAction(e -> abrirTelaGrupoUsuarios());

        System.out.println("Menu inicializado com sucesso!");
        System.out.println("Usuário: " + UsuarioSessao.getUsuarioLogado().getNome());
        System.out.println("Perfil: " + UsuarioSessao.getGrupoLogado().getDescricao());
    }

    // ==================== MÉTODO CENTRALIZADO ====================
    private void abrirTela(String fxmlPath, String titulo, int width, int height) {
        try {
            System.out.println("Abrindo: " + fxmlPath);
            java.net.URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.out.println("ERRO: Arquivo não encontrado - " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root, width, height));
            stage.setMinWidth(width * 0.6);
            stage.setMinHeight(height * 0.6);
            stage.show();
        } catch (Exception e) {
            System.out.println("Erro ao abrir " + titulo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== MÉTODOS DAS TELAS ====================
    private void abrirTelaClientes() {
        abrirTela("/com/eletronica/view/FrmCliente.fxml", "Cadastro de Clientes", 1000, 700);
    }

    private void abrirTelaProdutos() {
        abrirTela("/com/eletronica/view/FrmProduto.fxml", "Cadastro de Produtos", 900, 600);
    }

    private void abrirTelaServicos() {
        abrirTela("/com/eletronica/view/FrmServico.fxml", "Consulta de Serviços", 800, 600);
    }

    private void abrirTelaOrdens() {
        abrirTela("/com/eletronica/view/FrmOrdemServico.fxml", "Ordens de Serviço", 1200, 800);
    }

    private void abrirTelaUsuarios() {
        if (!UsuarioSessao.podeManterUsuario()) {
            System.out.println("Acesso negado: usuário não tem permissão!");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso Negado");
            alert.setHeaderText(null);
            alert.setContentText("Você não tem permissão para acessar esta funcionalidade!");
            alert.showAndWait();
            return;
        }
        abrirTela("/com/eletronica/view/FrmUsuario.fxml", "Cadastro de Usuários", 900, 600);
    }

    private void abrirTelaGrupoUsuarios() {
        if (!UsuarioSessao.podeManterUsuario()) {
            System.out.println("Acesso negado: usuário não tem permissão!");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso Negado");
            alert.setHeaderText(null);
            alert.setContentText("Você não tem permissão para acessar esta funcionalidade!");
            alert.showAndWait();
            return;
        }
        abrirTela("/com/eletronica/view/FrmGrupoUsuario.fxml", "Cadastro de Grupos de Usuários", 1000, 700);
    }

    @FXML
    private void sair() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sair");
        confirm.setHeaderText("Deseja realmente sair?");
        confirm.setContentText("Você será desconectado do sistema.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            UsuarioSessao.logout();
            System.exit(0);
        }
    }
}
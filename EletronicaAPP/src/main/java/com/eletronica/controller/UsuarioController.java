/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */
import com.eletronica.dao.GrupoUsuarioDAO;
import com.eletronica.dao.UsuarioDAO;
import com.eletronica.model.GrupoUsuarioEntity;
import com.eletronica.model.UsuarioEntity;
import com.eletronica.util.UsuarioSessao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsuarioController {

    @FXML
    private TextField txtNome;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtSenha;
    @FXML
    private PasswordField txtConfirmarSenha;
    @FXML
    private ComboBox<GrupoUsuarioEntity> cbGrupo;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnLimpar;
    @FXML
    private TextArea txtResultado;

    private UsuarioDAO usuarioDAO;
    private GrupoUsuarioDAO grupoDAO;
    private ObservableList<GrupoUsuarioEntity> listaGrupos;

    @FXML
    public void initialize() {
        usuarioDAO = new UsuarioDAO();
        grupoDAO = new GrupoUsuarioDAO();
        listaGrupos = FXCollections.observableArrayList();

        carregarGrupos();

        btnSalvar.setOnAction(e -> salvar());
        btnLimpar.setOnAction(e -> limpar());

        // Controle de permissão - só quem tem manterUsuario pode salvar
        if (!UsuarioSessao.podeManterUsuario()) {
            btnSalvar.setDisable(true);
            txtResultado.setText("Você não tem permissão para cadastrar usuários!");
        }
    }

    private void carregarGrupos() {
        try {
            listaGrupos.clear();
            listaGrupos.addAll(grupoDAO.listarTodos());
            cbGrupo.setItems(listaGrupos);
            cbGrupo.setPromptText("Selecione um grupo");
        } catch (Exception e) {
            txtResultado.setText("Erro ao carregar grupos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void salvar() {
        if (txtNome.getText().isEmpty()) {
            txtResultado.setText("Nome é obrigatório!");
            txtNome.requestFocus();
            return;
        }

        if (txtEmail.getText().isEmpty()) {
            txtResultado.setText("Email é obrigatório!");
            txtEmail.requestFocus();
            return;
        }

        if (txtSenha.getText().isEmpty()) {
            txtResultado.setText("Senha é obrigatória!");
            txtSenha.requestFocus();
            return;
        }

        if (!txtSenha.getText().equals(txtConfirmarSenha.getText())) {
            txtResultado.setText("Senhas não conferem!");
            txtSenha.clear();
            txtConfirmarSenha.clear();
            txtSenha.requestFocus();
            return;
        }

        if (cbGrupo.getValue() == null) {
            txtResultado.setText("Selecione um grupo!");
            return;
        }

        try {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setNome(txtNome.getText());
            usuario.setEmail(txtEmail.getText());
            usuario.setSenha(txtSenha.getText());
            usuario.setIdGrupoUsuario(cbGrupo.getValue().getId());

            usuarioDAO.inserir(usuario);
            txtResultado.setText("Usuário salvo com sucesso! ID: " + usuario.getId());
            limpar();
        } catch (Exception e) {
            txtResultado.setText("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpar() {
        txtNome.clear();
        txtEmail.clear();
        txtSenha.clear();
        txtConfirmarSenha.clear();
        cbGrupo.setValue(null);
    }
}

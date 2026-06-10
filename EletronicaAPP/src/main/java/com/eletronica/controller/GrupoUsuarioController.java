/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;
import com.eletronica.util.UsuarioSessao;


/**
 *
 * @author joao
 */

import com.eletronica.dao.GrupoUsuarioDAO;
import com.eletronica.model.GrupoUsuarioEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class GrupoUsuarioController {
    
    @FXML private TextField txtDescricao;
    @FXML private TextField txtGrupo;
    @FXML private CheckBox chkPermissao;
    @FXML private CheckBox chkManterUsuario;
    @FXML private CheckBox chkManterServico;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private Button btnDeletar;
    @FXML private Button btnBuscar;
    @FXML private TextField txtBusca;
    @FXML private TableView<GrupoUsuarioEntity> tblGrupos;
    @FXML private TableColumn<GrupoUsuarioEntity, Integer> colId;
    @FXML private TableColumn<GrupoUsuarioEntity, String> colDescricao;
    @FXML private TableColumn<GrupoUsuarioEntity, Integer> colGrupo;
    @FXML private TableColumn<GrupoUsuarioEntity, Boolean> colPermissao;
    @FXML private TableColumn<GrupoUsuarioEntity, Boolean> colManterUsuario;
    @FXML private TableColumn<GrupoUsuarioEntity, Boolean> colManterServico;
    @FXML private TextArea txtResultado;
    
    private GrupoUsuarioDAO grupoDAO;
    private ObservableList<GrupoUsuarioEntity> listaGrupos;
    
    @FXML
    public void initialize() {
        grupoDAO = new GrupoUsuarioDAO();
        listaGrupos = FXCollections.observableArrayList();
        
        // Configurar colunas da tabela
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colPermissao.setCellValueFactory(new PropertyValueFactory<>("permissao"));
        colManterUsuario.setCellValueFactory(new PropertyValueFactory<>("manterUsuario"));
        colManterServico.setCellValueFactory(new PropertyValueFactory<>("manterServico"));
        
        // Carregar dados
        carregarTabela();
        
        // Eventos
        btnSalvar.setOnAction(e -> salvar());
        btnLimpar.setOnAction(e -> limpar());
        btnDeletar.setOnAction(e -> deletar());
        btnBuscar.setOnAction(e -> buscar());
        
        // Selecionar linha da tabela
        tblGrupos.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, novo) -> {
                if (novo != null) carregarCampos(novo);
            });
    
        if (!UsuarioSessao.podeManterUsuario()) 
        {
            btnSalvar.setDisable(true);
            btnDeletar.setDisable(true);
            txtResultado.setText("Você não tem permissão para gerenciar grupos de usuários!");
        }
    
    }
    
    private void carregarTabela() {
        try {
            listaGrupos.clear();
            listaGrupos.addAll(grupoDAO.listarTodos());
            tblGrupos.setItems(listaGrupos);
            txtResultado.setText("Total: " + listaGrupos.size() + " grupos");
        } catch (Exception e) {
            txtResultado.setText("Erro ao carregar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void salvar() {
        // Validações
        if (txtDescricao.getText().isEmpty()) {
            txtResultado.setText("Descrição é obrigatória!");
            return;
        }
        
        if (txtGrupo.getText().isEmpty()) {
            txtResultado.setText("Código do grupo é obrigatório!");
            return;
        }
        
        try {
            GrupoUsuarioEntity grupo = new GrupoUsuarioEntity();
            grupo.setDescricao(txtDescricao.getText());
            grupo.setGrupo(Integer.parseInt(txtGrupo.getText()));
            grupo.setPermissao(chkPermissao.isSelected());
            grupo.setManterUsuario(chkManterUsuario.isSelected());
            grupo.setManterServico(chkManterServico.isSelected());
            
            grupoDAO.inserir(grupo);
            txtResultado.setText("Grupo salvo com sucesso! ID: " + grupo.getId());
            limpar();
            carregarTabela();
        } catch (NumberFormatException e) {
            txtResultado.setText("Código do grupo deve ser um número!");
        } catch (Exception e) {
            txtResultado.setText("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void buscar() {
        String busca = txtBusca.getText().trim();
        
        try {
            listaGrupos.clear();
            if (busca.isEmpty()) {
                listaGrupos.addAll(grupoDAO.listarTodos());
            } else {
                listaGrupos.addAll(grupoDAO.buscarPorDescricao(busca));
            }
            tblGrupos.setItems(listaGrupos);
            txtResultado.setText("Encontrados: " + listaGrupos.size());
        } catch (Exception e) {
            txtResultado.setText("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deletar() {
        GrupoUsuarioEntity selecionado = tblGrupos.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            txtResultado.setText("Selecione um grupo para deletar!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Deletar grupo: " + selecionado.getDescricao());
        confirm.setContentText("Tem certeza?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                grupoDAO.deletar(selecionado.getId());
                txtResultado.setText("Grupo deletado com sucesso!");
                limpar();
                carregarTabela();
            } catch (Exception e) {
                txtResultado.setText("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void carregarCampos(GrupoUsuarioEntity grupo) {
        txtDescricao.setText(grupo.getDescricao());
        txtGrupo.setText(String.valueOf(grupo.getGrupo()));
        chkPermissao.setSelected(grupo.isPermissao());
        chkManterUsuario.setSelected(grupo.isManterUsuario());
        chkManterServico.setSelected(grupo.isManterServico());
    }
    
    private void limpar() {
        txtDescricao.clear();
        txtGrupo.clear();
        chkPermissao.setSelected(false);
        chkManterUsuario.setSelected(false);
        chkManterServico.setSelected(false);
        txtBusca.clear();
        tblGrupos.getSelectionModel().clearSelection();
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */

import com.eletronica.dao.ServicoDAO;
import com.eletronica.model.ServicoEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServicoController {
    
    @FXML private TextField txtBusca;
    @FXML private Button btnPesquisar;
    @FXML private Button btnLimpar;
    @FXML private TextField txtId;
    @FXML private TextField txtDescricao;
    @FXML private TextArea txtResultado;
    @FXML private TableView<ServicoEntity> tblServicos;
    @FXML private TableColumn<ServicoEntity, Integer> colId;
    @FXML private TableColumn<ServicoEntity, String> colDescricao;
    
    private ServicoDAO servicoDAO;
    private ObservableList<ServicoEntity> listaServicos;
    
    @FXML
    public void initialize() {
        servicoDAO = new ServicoDAO();
        listaServicos = FXCollections.observableArrayList();
        
        // Configurar colunas da tabela
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colDescricao != null) colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        
        // Carregar todos os serviços ao iniciar
        carregarTodos();
        
        // Eventos dos botões
        if (btnPesquisar != null) btnPesquisar.setOnAction(e -> pesquisar());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparTudo());
        
        // Ao clicar na tabela, carregar detalhes
        if (tblServicos != null) {
            tblServicos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) {
                        exibirDetalhes(novo);
                    }
                });
        }
    }
    
    private void carregarTodos() {
        try {
            listaServicos.clear();
            listaServicos.addAll(servicoDAO.listarTodos());
            tblServicos.setItems(listaServicos);
            txtResultado.setText("Total de serviços: " + listaServicos.size());
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar serviços: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void pesquisar() {
        String busca = txtBusca.getText().trim();
        
        if (busca.isEmpty()) {
            carregarTodos();
            return;
        }
        
        try {
            listaServicos.clear();
            listaServicos.addAll(servicoDAO.buscarPorDescricao(busca));
            tblServicos.setItems(listaServicos);
            
            if (listaServicos.isEmpty()) {
                txtResultado.setText("Nenhum serviço encontrado com: \"" + busca + "\"");
            } else {
                txtResultado.setText("Encontrados " + listaServicos.size() + " serviço(s) com: \"" + busca + "\"");
            }
        } catch (Exception e) {
            mostrarAlerta("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void exibirDetalhes(ServicoEntity servico) {
        if (txtId != null) txtId.setText(String.valueOf(servico.getId()));
        if (txtDescricao != null) txtDescricao.setText(servico.getDescricao());
    }
    
    private void limparTudo() {
        txtBusca.clear();
        carregarTodos();
        limparDetalhes();
    }
    
    private void limparDetalhes() {
        if (txtId != null) txtId.clear();
        if (txtDescricao != null) txtDescricao.clear();
        tblServicos.getSelectionModel().clearSelection();
    }
    
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
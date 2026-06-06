/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */

import com.eletronica.dao.ProdutoDAO;
import com.eletronica.model.ProdutoEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;



public class ProdutoController {
   
    /*private int id;
    private String nome;
    private String tipo;
    private String modelo;
    private String marca;
    private String categoria;
    private String defeito;
    */
    
    // Componentes do FXML
    @FXML private TextField txtNome;
    @FXML private TextField txtTipo;
    @FXML private TextField txtModelo;
    @FXML private TextField txtMarca;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtDefeito;
    @FXML private TextField txtBusca;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private Button btnBuscar;
    @FXML private Button btnDeletar;
    @FXML private TableView<ProdutoEntity> tblProdutos;
    @FXML private TableColumn<ProdutoEntity, Integer> colId;
    @FXML private TableColumn<ProdutoEntity, String> colNome;
    @FXML private TableColumn<ProdutoEntity, String> colTipo;
    @FXML private TableColumn<ProdutoEntity, String> colModelo;
    @FXML private TableColumn<ProdutoEntity, String> colMarca;
    @FXML private TableColumn<ProdutoEntity, String> colCategoria;
    @FXML private TableColumn<ProdutoEntity, String> colDefeito;
    
    private ProdutoDAO produtoDAO;
    private ObservableList<ProdutoEntity> listaProdutos;
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando ClienteController...");
        
        produtoDAO = new ProdutoDAO();
        listaProdutos = FXCollections.observableArrayList();
        
        // Verifica se os componentes foram carregados
        if (colId == null) System.out.println("ERRO: colId é null!");
        if (colNome == null) System.out.println("ERRO: colNome é null!");
        if (tblProdutos == null) System.out.println("ERRO: tblProdutos é null!");
        
        // Configurar colunas da tabela (se existirem)
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colTipo != null) colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        if (colModelo != null) colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        if (colMarca != null) colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        if (colDefeito != null) colDefeito.setCellValueFactory(new PropertyValueFactory<>("defeito"));

        
        // Carregar dados
        carregarTabela();
        
        // Configurar eventos
        if (btnSalvar != null) btnSalvar.setOnAction(e -> salvarProdutos());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparCampos());
        if (btnBuscar != null) btnBuscar.setOnAction(e -> buscarProdutos());
        if (btnDeletar != null) btnDeletar.setOnAction(e -> deletarProdutos());
        
        // Seleção da tabela
        if (tblProdutos != null) {
            tblProdutos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) carregarCampos(novo);
                });
        }
    }
    
    private void carregarTabela() {
        try {
            listaProdutos.clear();
            listaProdutos.addAll(produtoDAO.listarTodos());
            if (tblProdutos != null) tblProdutos.setItems(listaProdutos);
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void salvarProdutos() {
    System.out.println("=== SALVAR PRODUTO ===");
    
    // Verificar se os campos existem
    if (txtNome == null) {
        System.out.println("ERRO: txtNome é NULL!");
        mostrarAlerta("Erro interno: campo Nome não encontrado!");
        return;
    }
    
    String nome = txtNome.getText();
    System.out.println("Nome digitado: '" + nome + "'");
    
    if (nome.isEmpty()) {
        mostrarAlerta("Nome é obrigatório!");
        return;
    }
    
    // Coletar dados
    String tipo = txtTipo != null ? txtTipo.getText() : "";
    String modelo = txtModelo != null ? txtModelo.getText() : "";
    String marca = txtMarca != null ? txtMarca.getText() : "";
    String categoria = txtCategoria != null ? txtCategoria.getText() : "";
    String defeito = txtDefeito != null ? txtDefeito.getText() : "";
    
    System.out.println("Dados coletados:");
    System.out.println("  Nome: " + nome);
    System.out.println("  Tipo: " + tipo);
    System.out.println("  Modelo: " + modelo);
    System.out.println("  Marca: " + marca);
    System.out.println("  CATEGORIA: " + categoria);
    System.out.println("  DEFEITO: " + defeito);
    
    ProdutoEntity produto = new ProdutoEntity();
    produto.setNome(nome);
    produto.setTipo(tipo);
    produto.setModelo(modelo);
    produto.setMarca(marca);
    produto.setCategoria(categoria);
    produto.setDefeito(defeito);
    
    try {
        System.out.println("Chamando clienteDAO.inserir()...");
        produtoDAO.inserir(produto);
        System.out.println("INSERIDO COM SUCESSO! ID gerado: " + produto.getId());
        mostrarAlerta("Cliente salvo com sucesso! ID: " + produto.getId());
        limparCampos();
        carregarTabela();
    } catch (SQLException e) {
        System.out.println("=== ERRO SQL ===");
        System.out.println("Mensagem: " + e.getMessage());
        System.out.println("SQL State: " + e.getSQLState());
        System.out.println("Error Code: " + e.getErrorCode());
        e.printStackTrace();
        mostrarAlerta("Erro no banco: " + e.getMessage());
    } catch (Exception e) {
        System.out.println("=== ERRO GERAL ===");
        System.out.println("Mensagem: " + e.getMessage());
        e.printStackTrace();
        mostrarAlerta("Erro: " + e.getMessage());
    }
}
    
    private void buscarProdutos() {
        try {
            String nomeBusca = txtBusca != null ? txtBusca.getText() : "";
            listaProdutos.clear();
            
            if (nomeBusca.isEmpty()) {
                listaProdutos.addAll(produtoDAO.listarTodos());
            } else {
                listaProdutos.addAll(produtoDAO.buscarPorNome(nomeBusca));
            }
            if (tblProdutos != null) tblProdutos.setItems(listaProdutos);
        } catch (Exception e) {
            mostrarAlerta("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deletarProdutos() {
        if (tblProdutos == null) return;
        
        ProdutoEntity selecionado = tblProdutos.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            mostrarAlerta("Selecione um produto para deletar!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza?");
        confirm.setContentText("Produto: " + selecionado.getNome());
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                produtoDAO.deletar(selecionado.getId());
                mostrarAlerta("Produto deletado!");
                limparCampos();
                carregarTabela();
            } catch (Exception e) {
                mostrarAlerta("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void carregarCampos(ProdutoEntity produto) {
        if (txtNome != null) txtNome.setText(produto.getNome());
        if (txtTipo != null) txtTipo.setText(produto.getTipo());
        if (txtModelo != null) txtModelo.setText(produto.getModelo());
        if (txtMarca != null) txtMarca.setText(produto.getMarca());
        if (txtCategoria != null) txtCategoria.setText(produto.getCategoria());
        if (txtDefeito != null) txtDefeito.setText(produto.getDefeito() != null ? produto.getDefeito() : "");
    }
    
    private void limparCampos() {
        if (txtNome != null) txtNome.clear();
        if (txtTipo != null) txtTipo.clear();
        if (txtModelo != null) txtModelo.clear();
        if (txtMarca != null) txtMarca.clear();
        if (txtCategoria != null) txtCategoria.clear();
        if (txtDefeito != null) txtDefeito.clear();
        if (txtBusca != null) txtBusca.clear();
        if (tblProdutos != null) tblProdutos.getSelectionModel().clearSelection();
    }
    
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */


import com.eletronica.dao.ClienteDAO;
import com.eletronica.model.ClienteEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class ClienteController {
    
    // Componentes do FXML
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtCpfCnpj;
    @FXML private TextField txtRg;
    @FXML private TextField txtIe;
    @FXML private TextField txtBusca;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private Button btnBuscar;
    @FXML private Button btnDeletar;
    @FXML private TableView<ClienteEntity> tblClientes;
    @FXML private TableColumn<ClienteEntity, Integer> colId;
    @FXML private TableColumn<ClienteEntity, String> colNome;
    @FXML private TableColumn<ClienteEntity, String> colEmail;
    @FXML private TableColumn<ClienteEntity, String> colTelefone;
    @FXML private TableColumn<ClienteEntity, String> colCpfCnpj;
    @FXML private TableColumn<ClienteEntity, String> colRg;
    
    private ClienteDAO clienteDAO;
    private ObservableList<ClienteEntity> listaClientes;
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando ClienteController...");
        
        clienteDAO = new ClienteDAO();
        listaClientes = FXCollections.observableArrayList();
        
        // Verifica se os componentes foram carregados
        if (colId == null) System.out.println("ERRO: colId é null!");
        if (colNome == null) System.out.println("ERRO: colNome é null!");
        if (tblClientes == null) System.out.println("ERRO: tblClientes é null!");
        
        // Configurar colunas da tabela (se existirem)
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colTelefone != null) colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        if (colCpfCnpj != null) colCpfCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpjCpf"));
        if (colRg != null) colRg.setCellValueFactory(new PropertyValueFactory<>("rg"));
        
        // Carregar dados
        carregarTabela();
        
        // Configurar eventos
        if (btnSalvar != null) btnSalvar.setOnAction(e -> salvarCliente());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparCampos());
        if (btnBuscar != null) btnBuscar.setOnAction(e -> buscarCliente());
        if (btnDeletar != null) btnDeletar.setOnAction(e -> deletarCliente());
        
        // Seleção da tabela
        if (tblClientes != null) {
            tblClientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) carregarCampos(novo);
                });
        }
    }
    
    private void carregarTabela() {
        try {
            listaClientes.clear();
            listaClientes.addAll(clienteDAO.listarTodos());
            if (tblClientes != null) tblClientes.setItems(listaClientes);
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void salvarCliente() {
    System.out.println("=== SALVAR CLIENTE ===");
    
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
    String email = txtEmail != null ? txtEmail.getText() : "";
    String telefone = txtTelefone != null ? txtTelefone.getText() : "";
    String cpfCnpj = txtCpfCnpj != null ? txtCpfCnpj.getText() : "";
    String rg = txtRg != null ? txtRg.getText() : "";
    String ie = txtIe != null ? txtIe.getText() : "";
    
    System.out.println("Dados coletados:");
    System.out.println("  Nome: " + nome);
    System.out.println("  Email: " + email);
    System.out.println("  Telefone: " + telefone);
    System.out.println("  CPF/CNPJ: " + cpfCnpj);
    System.out.println("  RG: " + rg);
    System.out.println("  IE: " + ie);
    
    ClienteEntity cliente = new ClienteEntity();
    cliente.setNome(nome);
    cliente.setEmail(email);
    cliente.setTelefone(telefone);
    cliente.setCnpjCpf(cpfCnpj);
    cliente.setRg(rg);
    cliente.setIe(ie);
    
    try {
        System.out.println("Chamando clienteDAO.inserir()...");
        clienteDAO.inserir(cliente);
        System.out.println("INSERIDO COM SUCESSO! ID gerado: " + cliente.getId());
        mostrarAlerta("Cliente salvo com sucesso! ID: " + cliente.getId());
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
    
    private void buscarCliente() {
        try {
            String nomeBusca = txtBusca != null ? txtBusca.getText() : "";
            listaClientes.clear();
            
            if (nomeBusca.isEmpty()) {
                listaClientes.addAll(clienteDAO.listarTodos());
            } else {
                listaClientes.addAll(clienteDAO.buscarPorNome(nomeBusca));
            }
            if (tblClientes != null) tblClientes.setItems(listaClientes);
        } catch (Exception e) {
            mostrarAlerta("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void deletarCliente() {
        if (tblClientes == null) return;
        
        ClienteEntity selecionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            mostrarAlerta("Selecione um cliente para deletar!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza?");
        confirm.setContentText("Cliente: " + selecionado.getNome());
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                clienteDAO.deletar(selecionado.getId());
                mostrarAlerta("Cliente deletado!");
                limparCampos();
                carregarTabela();
            } catch (Exception e) {
                mostrarAlerta("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void carregarCampos(ClienteEntity cliente) {
        if (txtNome != null) txtNome.setText(cliente.getNome());
        if (txtEmail != null) txtEmail.setText(cliente.getEmail());
        if (txtTelefone != null) txtTelefone.setText(cliente.getTelefone());
        if (txtCpfCnpj != null) txtCpfCnpj.setText(cliente.getCnpjCpf());
        if (txtRg != null) txtRg.setText(cliente.getRg());
        if (txtIe != null) txtIe.setText(cliente.getIe() != null ? cliente.getIe() : "");
    }
    
    private void limparCampos() {
        if (txtNome != null) txtNome.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtTelefone != null) txtTelefone.clear();
        if (txtCpfCnpj != null) txtCpfCnpj.clear();
        if (txtRg != null) txtRg.clear();
        if (txtIe != null) txtIe.clear();
        if (txtBusca != null) txtBusca.clear();
        if (tblClientes != null) tblClientes.getSelectionModel().clearSelection();
    }
    
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.service;

/**
 *
 * @author joao
 */


import com.eletronica.dao.ClienteDAO;
import com.eletronica.model.ClienteEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ClienteService {
    private ClienteDAO clienteDAO;
    private ObservableList<ClienteEntity> listaClientes;
    
    public ClienteService() {
        this.clienteDAO = new ClienteDAO();
        this.listaClientes = FXCollections.observableArrayList();
    }
    
    // Salvar cliente
    public void salvarCliente(TextField txtNome, TextField txtEmail, TextField txtTelefone,
                              TextField txtCnpjCpf, TextField txtRg, TextField txtIe,
                              TableView<ClienteEntity> tableView) {
        
        // Validação
        if (txtNome.getText().isEmpty()) {
            mostrarAlerta("Erro", "O campo Nome é obrigatório!");
            return;
        }
        
        // Criar cliente
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome(txtNome.getText());
        cliente.setEmail(txtEmail.getText());
        cliente.setTelefone(txtTelefone.getText());
        cliente.setCnpjCpf(txtCnpjCpf.getText());
        cliente.setRg(txtRg.getText());
        cliente.setIe(txtIe.getText());
        
        try {
            clienteDAO.inserir(cliente);
            mostrarAlerta("Sucesso", "Cliente cadastrado com sucesso!");
            limparCampos(txtNome, txtEmail, txtTelefone, txtCnpjCpf, txtRg, txtIe);
            atualizarTabela(tableView);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao salvar: " + e.getMessage());
        }
    }
    
    // Atualizar tabela
    public void atualizarTabela(TableView<ClienteEntity> tableView) {
        try {
            listaClientes.clear();
            listaClientes.addAll(clienteDAO.listarTodos());
            tableView.setItems(listaClientes);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao carregar dados: " + e.getMessage());
        }
    }
    
    // Buscar cliente
    public void buscarCliente(TextField txtBusca, TableView<ClienteEntity> tableView) {
        try {
            String nomeBusca = txtBusca.getText();
            listaClientes.clear();
            
            if (nomeBusca.isEmpty()) {
                listaClientes.addAll(clienteDAO.listarTodos());
            } else {
                listaClientes.addAll(clienteDAO.buscarPorNome(nomeBusca));
            }
            tableView.setItems(listaClientes);
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro na busca: " + e.getMessage());
        }
    }
    
    // Limpar campos
    private void limparCampos(TextField... campos) {
        for (TextField campo : campos) {
            campo.clear();
        }
    }
    
    // Mostrar alerta
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
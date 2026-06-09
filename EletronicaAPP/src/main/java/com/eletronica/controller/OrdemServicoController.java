/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.controller;

/**
 *
 * @author joao
 */



import com.eletronica.dao.*;
import com.eletronica.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdemServicoController {
    
    // Componentes do FXML
    @FXML private TextField txtBusca;
    @FXML private Button btnPesquisar;
    @FXML private Button btnNovo;
    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private Button btnDeletar;
    
    @FXML private TextField txtId;
    @FXML private TextArea txtDescricao;
    @FXML private TextField txtOrcamento;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<ClienteEntity> cbCliente;
    @FXML private ComboBox<ProdutoEntity> cbProduto;
    
    @FXML private TableView<OrdemServicoEntity> tblOrdens;
    @FXML private TableColumn<OrdemServicoEntity, Integer> colId;
    @FXML private TableColumn<OrdemServicoEntity, String> colDescricao;
    @FXML private TableColumn<OrdemServicoEntity, String> colCliente;
    @FXML private TableColumn<OrdemServicoEntity, String> colStatus;
    @FXML private TableColumn<OrdemServicoEntity, String> colOrcamento;
    @FXML private TableColumn<OrdemServicoEntity, String> colData;  // Mudou para String
    
    @FXML private TextArea txtResultado;
    
    private OrdemServicoDAO osDAO;
    private ClienteDAO clienteDAO;
    private ProdutoDAO produtoDAO;
    private ObservableList<OrdemServicoEntity> listaOrdens;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @FXML
    public void initialize() {
        osDAO = new OrdemServicoDAO();
        clienteDAO = new ClienteDAO();
        produtoDAO = new ProdutoDAO();
        listaOrdens = FXCollections.observableArrayList();
        
        // Configurar colunas da tabela
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        colOrcamento.setCellValueFactory(new PropertyValueFactory<>("orcamento"));
        
        // CORREÇÃO: Configurar coluna data como String
        colData.setCellValueFactory(cellData -> {
            LocalDate data = cellData.getValue().getData();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> data != null ? data.format(formatter) : ""
            );
        });
        
        // Configurar ComboBox de status (conforme enum)
        cbStatus.getItems().addAll("EM ESPERA", "EM ANDAMENTO", "PRONTO");
        
        // Carregar comboboxes
        carregarClientes();
        carregarProdutos();
        
        // Carregar tabela
        carregarTodas();
        
        // Eventos
        btnPesquisar.setOnAction(e -> pesquisar());
        btnNovo.setOnAction(e -> novo());
        btnSalvar.setOnAction(e -> salvar());
        btnLimpar.setOnAction(e -> limparTudo());
        btnDeletar.setOnAction(e -> deletar());
        
        // Seleção da tabela
        tblOrdens.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, novo) -> {
                if (novo != null) {
                    carregarDetalhes(novo);
                }
            });
    }
    
    private void carregarClientes() {
        try {
            List<ClienteEntity> clientes = clienteDAO.listarTodos();
            cbCliente.getItems().clear();
            cbCliente.getItems().addAll(clientes);
            
            // Mostrar nome no ComboBox
            cbCliente.setCellFactory(l -> new ListCell<ClienteEntity>() {
                @Override
                protected void updateItem(ClienteEntity item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNome());
                }
            });
            cbCliente.setButtonCell(new ListCell<ClienteEntity>() {
                @Override
                protected void updateItem(ClienteEntity item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNome());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void carregarProdutos() {
        try {
            List<ProdutoEntity> produtos = produtoDAO.listarTodos();
            cbProduto.getItems().clear();
            cbProduto.getItems().addAll(produtos);
            
            cbProduto.setCellFactory(l -> new ListCell<ProdutoEntity>() {
                @Override
                protected void updateItem(ProdutoEntity item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNome());
                }
            });
            cbProduto.setButtonCell(new ListCell<ProdutoEntity>() {
                @Override
                protected void updateItem(ProdutoEntity item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNome());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void carregarTodas() {
        try {
            listaOrdens.clear();
            listaOrdens.addAll(osDAO.listarTodas());
            tblOrdens.setItems(listaOrdens);
            txtResultado.setText("Total de OS: " + listaOrdens.size());
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void pesquisar() {
        String busca = txtBusca.getText().trim();
        if (busca.isEmpty()) {
            carregarTodas();
            return;
        }
        
        try {
            listaOrdens.clear();
            listaOrdens.addAll(osDAO.buscarPorDescricao(busca));
            tblOrdens.setItems(listaOrdens);
            txtResultado.setText("Encontradas " + listaOrdens.size() + " OS(es)");
        } catch (Exception e) {
            mostrarAlerta("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void novo() {
        limparTudo();
        txtDescricao.requestFocus();
    }
    
    private void salvar() {
        // Validações
        if (txtDescricao.getText().isEmpty()) {
            mostrarAlerta("Descrição é obrigatória!");
            return;
        }
        if (cbCliente.getValue() == null) {
            mostrarAlerta("Selecione um cliente!");
            return;
        }
        if (cbProduto.getValue() == null) {
            mostrarAlerta("Selecione um produto!");
            return;
        }
        
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setDescricao(txtDescricao.getText());
        os.setOrcamento(txtOrcamento.getText().isEmpty() ? "0" : txtOrcamento.getText());
        os.setIdCliente(cbCliente.getValue().getId());
        os.setIdProduto(cbProduto.getValue().getId());
        
        // Converter status do ComboBox para Enum
        String statusSelecionado = cbStatus.getValue();
        if (statusSelecionado != null) {
            switch (statusSelecionado) {
                case "EM ESPERA":
                    os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
                    break;
                case "EM ANDAMENTO":
                    os.setStatus(OrdemServicoEntity.TipoStatus.EM_ANDAMENTO);
                    break;
                case "PRONTO":
                    os.setStatus(OrdemServicoEntity.TipoStatus.PRONTO);
                    break;
                default:
                    os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
            }
        } else {
            os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
        }
        
        try {
            // Verificar se é edição ou novo
            if (txtId.getText().isEmpty()) {
                osDAO.inserir(os);
                mostrarAlerta("OS salva com sucesso! ID: " + os.getId());
            } else {
                os.setId(Integer.parseInt(txtId.getText()));
                osDAO.atualizar(os);
                mostrarAlerta("OS atualizada com sucesso!");
            }
            limparTudo();
            carregarTodas();
        } catch (Exception e) {
            mostrarAlerta("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void carregarDetalhes(OrdemServicoEntity os) {
        txtId.setText(String.valueOf(os.getId()));
        txtDescricao.setText(os.getDescricao());
        txtOrcamento.setText(os.getOrcamento());
        
        // Selecionar status no ComboBox
        String statusStr = os.getStatusString();
        cbStatus.setValue(statusStr);
        
        // Selecionar cliente no ComboBox
        for (ClienteEntity c : cbCliente.getItems()) {
            if (c.getId() == os.getIdCliente()) {
                cbCliente.setValue(c);
                break;
            }
        }
        
        // Selecionar produto no ComboBox
        for (ProdutoEntity p : cbProduto.getItems()) {
            if (p.getId() == os.getIdProduto()) {
                cbProduto.setValue(p);
                break;
            }
        }
    }
    
    private void deletar() {
        OrdemServicoEntity selecionado = tblOrdens.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            mostrarAlerta("Selecione uma OS para deletar!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza?");
        confirm.setContentText("OS: " + selecionado.getDescricao());
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                osDAO.deletar(selecionado.getId());
                mostrarAlerta("OS deletada com sucesso!");
                limparTudo();
                carregarTodas();
            } catch (Exception e) {
                mostrarAlerta("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void limparTudo() {
        txtId.clear();
        txtDescricao.clear();
        txtOrcamento.clear();
        txtBusca.clear();
        cbStatus.setValue(null);
        cbCliente.setValue(null);
        cbProduto.setValue(null);
        tblOrdens.getSelectionModel().clearSelection();
        txtResultado.clear();
    }
    
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
/**
 * CONTROLADOR DA TELA DE ORDEM DE SERVIÇO (OS)
 * 
 * Esta classe é responsável por gerenciar as Ordens de Serviço do sistema.
 * É a tela mais complexa do sistema, pois integra Clientes, Produtos e Serviços.
 * 
 * Funcionalidades:
 * - Criar nova OS (vincular cliente, produto, status, orçamento)
 * - Editar OS existente
 * - Buscar OS por descrição
 * - Deletar OS (apenas usuários com permissão)
 * - Listar todas as OS em uma tabela
 * - Controlar permissões baseadas no usuário logado
 * 
 * @author joao
 */
package com.eletronica.controller;

// Import da aplicação
import com.eletronica.util.UsuarioSessao;   // Classe que gerencia a sessão do usuário

// Imports de DAOs e Models
import com.eletronica.dao.*;
import com.eletronica.model.*;

// Imports do JavaFX
import javafx.collections.FXCollections;   // Utilitários para listas observáveis
import javafx.collections.ObservableList;  // Lista que notifica mudanças na UI
import javafx.fxml.FXML;                   // Anotação para vincular componentes do FXML
import javafx.scene.control.*;             // Componentes da UI
import javafx.scene.control.cell.PropertyValueFactory; // Mapeia atributos para colunas

// Imports Java padrão
import java.time.LocalDate;                // Para manipulação de datas
import java.time.format.DateTimeFormatter; // Para formatar datas
import java.util.List;

public class OrdemServicoController {
    
    // ==================== COMPONENTES DO FXML ====================
    // Estes atributos são injetados automaticamente pelo JavaFX
    
    // Componentes de busca e ações
    @FXML private TextField txtBusca;       // Campo para buscar OS por descrição
    @FXML private Button btnPesquisar;      // Botão para executar a busca
    @FXML private Button btnNovo;           // Botão para criar nova OS (limpa formulário)
    @FXML private Button btnSalvar;         // Botão para salvar/editar OS
    @FXML private Button btnLimpar;         // Botão para limpar formulário
    @FXML private Button btnDeletar;        // Botão para deletar OS
    
    // Campos do formulário de OS
    @FXML private TextField txtId;          // ID da OS (gerado automaticamente, readonly)
    @FXML private TextArea txtDescricao;    // Descrição detalhada do serviço a ser realizado
    @FXML private TextField txtOrcamento;   // Valor orçado para o serviço
    @FXML private ComboBox<String> cbStatus; // Status da OS (EM ESPERA, EM ANDAMENTO, PRONTO)
    @FXML private ComboBox<ClienteEntity> cbCliente;  // ComboBox com lista de clientes
    @FXML private ComboBox<ProdutoEntity> cbProduto;  // ComboBox com lista de produtos
    
    // Tabela de OS
    @FXML private TableView<OrdemServicoEntity> tblOrdens;  // Tabela principal
    @FXML private TableColumn<OrdemServicoEntity, Integer> colId;         // Coluna ID
    @FXML private TableColumn<OrdemServicoEntity, String> colDescricao;   // Coluna Descrição
    @FXML private TableColumn<OrdemServicoEntity, String> colCliente;     // Coluna Cliente
    @FXML private TableColumn<OrdemServicoEntity, String> colStatus;      // Coluna Status
    @FXML private TableColumn<OrdemServicoEntity, String> colOrcamento;   // Coluna Orçamento
    @FXML private TableColumn<OrdemServicoEntity, String> colData;        // Coluna Data
    
    // Área de texto para exibir resultados/mensagens
    @FXML private TextArea txtResultado;
    
    // ==================== ATRIBUTOS PRIVADOS ====================
    
    private OrdemServicoDAO osDAO;                     // DAO para operações de OS
    private ClienteDAO clienteDAO;                     // DAO para buscar clientes
    private ProdutoDAO produtoDAO;                     // DAO para buscar produtos
    private ObservableList<OrdemServicoEntity> listaOrdens; // Lista que alimenta a tabela
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Formato de data BR
    
    // ==================== MÉTODO INICIALIZADOR ====================
    
    /**
     * Método chamado AUTOMATICAMENTE após o carregamento do FXML.
     * Responsável por:
     * - Inicializar os objetos DAO
     * - Configurar as colunas da tabela
     * - Carregar Comboboxes de Clientes e Produtos
     * - Carregar dados iniciais
     * - Configurar eventos dos botões
     * - Configurar listener para seleção de linha na tabela
     * - Controlar permissões do usuário logado
     */
    @FXML
    public void initialize() {
        // Inicializa DAOs
        osDAO = new OrdemServicoDAO();
        clienteDAO = new ClienteDAO();
        produtoDAO = new ProdutoDAO();
        listaOrdens = FXCollections.observableArrayList();
        
        // ==================== CONFIGURAÇÃO DAS COLUNAS DA TABELA ====================
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        colOrcamento.setCellValueFactory(new PropertyValueFactory<>("orcamento"));
        
        // CONFIGURAÇÃO ESPECIAL PARA DATA (converte LocalDate para String formatada)
        colData.setCellValueFactory(cellData -> {
            LocalDate data = cellData.getValue().getData();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> data != null ? data.format(formatter) : ""
            );
        });
        
        // ==================== CONFIGURAÇÃO DO COMBOBOX DE STATUS ====================
        // Adiciona os possíveis status da OS (conforme enum TipoStatus)
        cbStatus.getItems().addAll("EM ESPERA", "EM ANDAMENTO", "PRONTO");
        
        // ==================== CARREGAMENTO DE DADOS ====================
        carregarClientes();   // Carrega lista de clientes no ComboBox
        carregarProdutos();   // Carrega lista de produtos no ComboBox
        carregarTodas();      // Carrega todas as OS na tabela
        
        // ==================== EVENTOS DOS BOTÕES ====================
        btnPesquisar.setOnAction(e -> pesquisar());
        btnNovo.setOnAction(e -> novo());
        btnSalvar.setOnAction(e -> salvar());
        btnLimpar.setOnAction(e -> limparTudo());
        btnDeletar.setOnAction(e -> deletar());
        
        // ==================== SELETOR DA TABELA ====================
        // Quando o usuário clica em uma OS na tabela, carrega os dados nos campos
        tblOrdens.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, novo) -> {
                if (novo != null) {
                    carregarDetalhes(novo);
                }
            });
        
        // ==================== CONTROLE DE PERMISSÕES ====================
        // Verifica se o usuário pode criar OS (baseado na permissão do grupo)
        if (!UsuarioSessao.podeCriarOS()) {
            btnSalvar.setDisable(true);   // Desabilita botão salvar
            btnNovo.setDisable(true);     // Desabilita botão novo
            txtResultado.setText("Você não tem permissão para criar OS!");
        }
        
        // Verifica se o usuário pode deletar OS (apenas Administrador - grupo 1)
        if (!UsuarioSessao.podeDeletarOS()) {
            btnDeletar.setVisible(false); // Esconde o botão deletar
        }
        
        // Exibe informações do usuário logado na área de resultado
        txtResultado.appendText("\nUsuário: " + UsuarioSessao.getUsuarioLogado().getNome() + 
                                " | Perfil: " + UsuarioSessao.getGrupoLogado().getDescricao());
    }
    
    // ==================== MÉTODOS DE CARREGAMENTO ====================
    
    /**
     * CARREGA A LISTA DE CLIENTES NO COMBOBOX
     * 
     * Busca todos os clientes no banco e configura o ComboBox para
     * exibir o nome do cliente (ao invés do toString padrão)
     */
    private void carregarClientes() {
        try {
            List<ClienteEntity> clientes = clienteDAO.listarTodos();
            cbCliente.getItems().clear();
            cbCliente.getItems().addAll(clientes);
            
            // Configuração para exibir apenas o NOME do cliente no ComboBox
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
    
    /**
     * CARREGA A LISTA DE PRODUTOS NO COMBOBOX
     * 
     * Busca todos os produtos no banco e configura o ComboBox para
     * exibir o nome do produto
     */
    private void carregarProdutos() {
        try {
            List<ProdutoEntity> produtos = produtoDAO.listarTodos();
            cbProduto.getItems().clear();
            cbProduto.getItems().addAll(produtos);
            
            // Configuração para exibir apenas o NOME do produto no ComboBox
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
    
    /**
     * CARREGA TODAS AS ORDENS DE SERVIÇO NA TABELA
     */
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
    
    // ==================== MÉTODOS DE BUSCA ====================
    
    /**
     * PESQUISA OS POR DESCRIÇÃO
     * 
     * Busca OS cuja descrição contenha o texto digitado (case-insensitive)
     * Se o campo estiver vazio, lista todas as OS
     */
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
    
    // ==================== MÉTODO NOVO ====================
    
    /**
     * PREPARA A TELA PARA UMA NOVA OS
     * 
     * Limpa o formulário e coloca o foco no campo descrição
     */
    private void novo() {
        limparTudo();
        txtDescricao.requestFocus();
    }
    
    // ==================== MÉTODO SALVAR ====================
    
    /**
     * SALVA OU ATUALIZA UMA ORDEM DE SERVIÇO
     * 
     * Se o campo ID estiver vazio → cria uma nova OS
     * Se o campo ID tiver valor → atualiza a OS existente
     * 
     * Validações:
     * - Descrição não pode estar vazia
     * - Cliente deve ser selecionado
     * - Produto deve ser selecionado
     */
    private void salvar() {
        // ==================== VALIDAÇÕES ====================
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
        
        // ==================== CRIAÇÃO DO OBJETO OS ====================
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setDescricao(txtDescricao.getText());
        os.setOrcamento(txtOrcamento.getText().isEmpty() ? "0" : txtOrcamento.getText());
        os.setIdCliente(cbCliente.getValue().getId());
        os.setIdProduto(cbProduto.getValue().getId());
        
        // ==================== CONVERSÃO DO STATUS ====================
        // Converte o texto do ComboBox para o Enum correspondente
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
        
        // ==================== INSERÇÃO OU ATUALIZAÇÃO ====================
        try {
            if (txtId.getText().isEmpty()) {
                // CRIAÇÃO: ID vazio significa nova OS
                osDAO.inserir(os);
                mostrarAlerta("OS salva com sucesso! ID: " + os.getId());
            } else {
                // EDIÇÃO: ID preenchido significa atualização
                os.setId(Integer.parseInt(txtId.getText()));
                osDAO.atualizar(os);
                mostrarAlerta("OS atualizada com sucesso!");
            }
            limparTudo();      // Limpa o formulário
            carregarTodas();   // Recarrega a tabela
        } catch (Exception e) {
            mostrarAlerta("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ==================== MÉTODO CARREGAR DETALHES ====================
    
    /**
     * CARREGA OS DADOS DE UMA OS NOS CAMPOS DO FORMULÁRIO
     * 
     * Usado quando o usuário clica em uma OS na tabela.
     * Carrega ID, descrição, orçamento, status, cliente e produto.
     * 
     * @param os A OS selecionada na tabela
     */
    private void carregarDetalhes(OrdemServicoEntity os) {
        txtId.setText(String.valueOf(os.getId()));
        txtDescricao.setText(os.getDescricao());
        txtOrcamento.setText(os.getOrcamento());
        
        // Seleciona o status correspondente no ComboBox
        String statusStr = os.getStatusString();
        cbStatus.setValue(statusStr);
        
        // Seleciona o cliente correspondente no ComboBox
        for (ClienteEntity c : cbCliente.getItems()) {
            if (c.getId() == os.getIdCliente()) {
                cbCliente.setValue(c);
                break;
            }
        }
        
        // Seleciona o produto correspondente no ComboBox
        for (ProdutoEntity p : cbProduto.getItems()) {
            if (p.getId() == os.getIdProduto()) {
                cbProduto.setValue(p);
                break;
            }
        }
    }
    
    // ==================== MÉTODO DELETAR ====================
    
    /**
     * DELETA UMA ORDEM DE SERVIÇO
     * 
     * Verifica permissão novamente por segurança,
     * pede confirmação ao usuário e deleta a OS selecionada.
     */
    private void deletar() {
        // Verifica permissão novamente (segurança em camadas)
        if (!UsuarioSessao.podeDeletarOS()) {
            mostrarAlerta("Você não tem permissão para deletar OS!");
            return;
        }
        
        // Obtém a OS selecionada na tabela
        OrdemServicoEntity selecionado = tblOrdens.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            mostrarAlerta("Selecione uma OS para deletar!");
            return;
        }
        
        // Caixa de confirmação
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
    
    // ==================== MÉTODOS UTILITÁRIOS ====================
    
    /**
     * LIMPA TODOS OS CAMPOS DO FORMULÁRIO
     */
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
    
    /**
     * EXIBE UMA MENSAGEM DE ALERTA PARA O USUÁRIO
     * 
     * @param mensagem O texto da mensagem a ser exibida
     */
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
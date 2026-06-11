/**
 * CONTROLADOR DA TELA DE CLIENTES
 * 
 * Esta classe é responsável por gerenciar a interface gráfica de cadastro de clientes.
 * Ela faz a ponte entre a tela (FXML) e as regras de negócio + banco de dados.
 * 
 * Funcionalidades:
 * - Inserir novos clientes
 * - Buscar clientes por nome
 * - Deletar clientes
 * - Exibir lista de clientes em uma tabela
 * - Carregar dados do cliente selecionado nos campos do formulário
 * 
 * @author joao
 */
package com.eletronica.controller;

// Imports da aplicação
import com.eletronica.dao.ClienteDAO;      // Classe de acesso ao banco de dados
import com.eletronica.model.ClienteEntity; // Modelo/entidade Cliente

// Imports do JavaFX
import javafx.collections.FXCollections;   // Utilitários para listas observáveis
import javafx.collections.ObservableList;  // Lista que notifica mudanças na UI
import javafx.fxml.FXML;                   // Anotação para vincular componentes do FXML
import javafx.scene.control.*;             // Componentes da UI (TextField, Button, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Mapeia atributos para colunas da tabela

// Import para tratamento de erros SQL
import java.sql.SQLException;

public class ClienteController {
    
    @FXML private TextField txtNome;        // Campo de texto para o nome
    @FXML private TextField txtEmail;       // Campo de texto para o email
    @FXML private TextField txtTelefone;    // Campo de texto para o telefone
    @FXML private TextField txtCpfCnpj;     // Campo de texto para CPF/CNPJ
    @FXML private TextField txtRg;          // Campo de texto para RG
    @FXML private TextField txtIe;          // Campo de texto para Inscrição Estadual (opcional)
    @FXML private TextField txtBusca;       // Campo de texto para busca de clientes
    
    @FXML private Button btnSalvar;          // Botão para salvar cliente
    @FXML private Button btnLimpar;          // Botão para limpar formulário
    @FXML private Button btnBuscar;          // Botão para buscar clientes
    @FXML private Button btnDeletar;         // Botão para deletar cliente
    
    @FXML private TableView<ClienteEntity> tblClientes;  // Tabela que exibe os clientes
    @FXML private TableColumn<ClienteEntity, Integer> colId;        // Coluna ID
    @FXML private TableColumn<ClienteEntity, String> colNome;       // Coluna Nome
    @FXML private TableColumn<ClienteEntity, String> colEmail;      // Coluna Email
    @FXML private TableColumn<ClienteEntity, String> colTelefone;   // Coluna Telefone
    @FXML private TableColumn<ClienteEntity, String> colCpfCnpj;    // Coluna CPF/CNPJ
    @FXML private TableColumn<ClienteEntity, String> colRg;         // Coluna RG
    
    private ClienteDAO clienteDAO;                      
    private ObservableList<ClienteEntity> listaClientes; // Lista que alimenta a tabela
    
 
    @FXML
    public void initialize() {
        System.out.println("Inicializando ClienteController...");
        
        // Inicializa o objeto de acesso ao banco de dados
        clienteDAO = new ClienteDAO();
        
        // Cria uma lista observável (qualquer alteração atualiza a tabela automaticamente)
        listaClientes = FXCollections.observableArrayList();
        
        // Verificações de segurança - se os componentes foram carregados corretamente
        if (colId == null) System.out.println("ERRO: colId é null!");
        if (colNome == null) System.out.println("ERRO: colNome é null!");
        if (tblClientes == null) System.out.println("ERRO: tblClientes é null!");
        
        // CONFIGURAÇÃO DAS COLUNAS DA TABELA
        // PropertyValueFactory mapeia o atributo da classe ClienteEntity para a coluna
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colTelefone != null) colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        if (colCpfCnpj != null) colCpfCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpjCpf"));
        if (colRg != null) colRg.setCellValueFactory(new PropertyValueFactory<>("rg"));
        
        // Carrega todos os clientes do banco e exibe na tabela
        carregarTabela();
        
        // CONFIGURAÇÃO DOS EVENTOS DOS BOTÕES
        // Cada botão executa um método específico quando clicado
        if (btnSalvar != null) btnSalvar.setOnAction(e -> salvarCliente());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparCampos());
        if (btnBuscar != null) btnBuscar.setOnAction(e -> buscarCliente());
        if (btnDeletar != null) btnDeletar.setOnAction(e -> deletarCliente());
        
        // CONFIGURAÇÃO DO LISTENER DA TABELA
        // Quando o usuário clica em um cliente na tabela, carrega seus dados nos campos
        if (tblClientes != null) {
            tblClientes.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) carregarCampos(novo);
                });
        }
    }
    
    
    /**
     * CARREGA A TABELA COM TODOS OS CLIENTES DO BANCO
     * 
     * Este método consulta o banco de dados através do DAO,
     * limpa a lista atual e adiciona os novos dados.
     * A tabela é atualizada automaticamente por causa da ObservableList.
     */
    private void carregarTabela() {
        try {
            listaClientes.clear();                          // Limpa a lista atual
            listaClientes.addAll(clienteDAO.listarTodos()); // Adiciona todos os clientes do banco
            if (tblClientes != null) tblClientes.setItems(listaClientes); // Atualiza a tabela
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * SALVA UM NOVO CLIENTE NO BANCO DE DADOS
     * 
     * Este método:
     * 1. Valida se o nome foi preenchido
     * 2. Coleta os dados dos campos do formulário
     * 3. Cria um objeto ClienteEntity com os dados
     * 4. Chama o DAO para inserir no banco
     * 5. Limpa o formulário e atualiza a tabela
     */
    private void salvarCliente() {
        System.out.println("=== SALVAR CLIENTE ===");
        
        // VALIDAÇÃO: Verificar se o campo Nome existe
        if (txtNome == null) {
            System.out.println("ERRO: txtNome é NULL!");
            mostrarAlerta("Erro interno: campo Nome não encontrado!");
            return;
        }
        
        String nome = txtNome.getText();
        System.out.println("Nome digitado: '" + nome + "'");
        
        // VALIDAÇÃO: Nome é obrigatório
        if (nome.isEmpty()) {
            mostrarAlerta("Nome é obrigatório!");
            return;
        }
        
        // COLETA DOS DADOS DO FORMULÁRIO
        // Usa operador ternário para evitar NullPointerException
        String email = txtEmail != null ? txtEmail.getText() : "";
        String telefone = txtTelefone != null ? txtTelefone.getText() : "";
        String cpfCnpj = txtCpfCnpj != null ? txtCpfCnpj.getText() : "";
        String rg = txtRg != null ? txtRg.getText() : "";
        String ie = txtIe != null ? txtIe.getText() : "";
        
        // Log para debug
        System.out.println("Dados coletados:");
        System.out.println("  Nome: " + nome);
        System.out.println("  Email: " + email);
        System.out.println("  Telefone: " + telefone);
        System.out.println("  CPF/CNPJ: " + cpfCnpj);
        System.out.println("  RG: " + rg);
        System.out.println("  IE: " + ie);
        
        // CRIAÇÃO DO OBJETO CLIENTE
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setTelefone(telefone);
        cliente.setCnpjCpf(cpfCnpj);
        cliente.setRg(rg);
        cliente.setIe(ie);
        
        // INSERÇÃO NO BANCO DE DADOS
        try {
            System.out.println("Chamando clienteDAO.inserir()...");
            clienteDAO.inserir(cliente);  // O ID é gerado automaticamente pelo banco
            System.out.println("INSERIDO COM SUCESSO! ID gerado: " + cliente.getId());
            
            // FEEDBACK AO USUÁRIO
            mostrarAlerta("Cliente salvo com sucesso! ID: " + cliente.getId());
            
            // LIMPEZA E ATUALIZAÇÃO
            limparCampos();      // Limpa o formulário
            carregarTabela();    // Recarrega a tabela com o novo cliente
            
        } catch (SQLException e) {
            // Tratamento específico para erros de banco de dados
            System.out.println("=== ERRO SQL ===");
            System.out.println("Mensagem: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            mostrarAlerta("Erro no banco: " + e.getMessage());
        } catch (Exception e) {
            // Tratamento para qualquer outro erro
            System.out.println("=== ERRO GERAL ===");
            System.out.println("Mensagem: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro: " + e.getMessage());
        }
    }
    
    /**
     * BUSCA CLIENTES POR NOME (OU LISTA TODOS SE O CAMPO ESTIVER VAZIO)
     * 
     * O campo de busca permite filtrar clientes pelo nome usando LIKE.
     * Se o campo estiver vazio, lista todos os clientes.
     */
    private void buscarCliente() {
        try {
            String nomeBusca = txtBusca != null ? txtBusca.getText() : "";
            listaClientes.clear();
            
            if (nomeBusca.isEmpty()) {
                // Busca vazia → carrega todos
                listaClientes.addAll(clienteDAO.listarTodos());
            } else {
                // Busca por nome (usando ILIKE no PostgreSQL)
                listaClientes.addAll(clienteDAO.buscarPorNome(nomeBusca));
            }
            if (tblClientes != null) tblClientes.setItems(listaClientes);
        } catch (Exception e) {
            mostrarAlerta("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * DELETA UM CLIENTE DO BANCO DE DADOS
     * 
     * Antes de deletar, pede confirmação ao usuário.
     * O cliente deve estar selecionado na tabela.
     */
    private void deletarCliente() {
        if (tblClientes == null) return;
        
        // Obtém o cliente selecionado na tabela
        ClienteEntity selecionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            mostrarAlerta("Selecione um cliente para deletar!");
            return;
        }
        
        // CAIXA DE CONFIRMAÇÃO
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza?");
        confirm.setContentText("Cliente: " + selecionado.getNome());
        
        // Só deleta se o usuário confirmar
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                clienteDAO.deletar(selecionado.getId());  // Deleta pelo ID
                mostrarAlerta("Cliente deletado!");
                limparCampos();      // Limpa o formulário
                carregarTabela();    // Recarrega a tabela sem o cliente deletado
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
        // IE pode ser null, então verifica antes
        if (txtIe != null) txtIe.setText(cliente.getIe() != null ? cliente.getIe() : "");
    }
    
    /**
     * LIMPA TODOS OS CAMPOS DO FORMULÁRIO E LIMPA A SELEÇÃO DA TABELA
     * 
     * Prepara a tela para um novo cadastro.
     */
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
        alert.setHeaderText(null);  // Sem cabeçalho, apenas o conteúdo
        alert.setContentText(mensagem);
        alert.showAndWait();  // Exibe e aguarda o usuário clicar em OK
    }
}
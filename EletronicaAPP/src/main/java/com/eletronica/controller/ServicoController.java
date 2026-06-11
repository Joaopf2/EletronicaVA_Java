/**
 * CONTROLADOR DA TELA DE CONSULTA DE SERVIÇOS
 * 
 * Esta classe é responsável por gerenciar a consulta de serviços no sistema.
 * Diferente das outras telas (Cliente, Produto), esta é APENAS para CONSULTA.
 * 
 * Funcionalidades:
 * - Listar todos os serviços cadastrados
 * - Buscar serviços por descrição
 * - Exibir detalhes do serviço selecionado
 * - SEM operações de inserir, atualizar ou deletar (apenas visualização)
 * 
 * Por que apenas consulta?
 * - No diagrama de classes, Serviço tem relacionamento de herança com Ordem de Serviço
 * - Os serviços são predefinidos e gerenciados pelo administrador
 * - Esta tela serve apenas para consultar quais serviços estão disponíveis
 * 
 * @author joao
 */
package com.eletronica.controller;

// Imports da aplicação
import com.eletronica.dao.ServicoDAO;      // Classe de acesso ao banco de dados
import com.eletronica.model.ServicoEntity; // Modelo/entidade Serviço

// Imports do JavaFX
import javafx.collections.FXCollections;   // Utilitários para listas observáveis
import javafx.collections.ObservableList;  // Lista que notifica mudanças na UI
import javafx.fxml.FXML;                   // Anotação para vincular componentes do FXML
import javafx.scene.control.*;             // Componentes da UI (TextField, Button, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Mapeia atributos para colunas

public class ServicoController {
    
    // ==================== COMPONENTES DO FXML ====================
    // Estes atributos são injetados automaticamente pelo JavaFX
    // Os nomes devem ser IDÊNTICOS aos fx:id definidos no Scene Builder
    
    // Componentes de busca
    @FXML private TextField txtBusca;      // Campo para digitar o texto da busca
    @FXML private Button btnPesquisar;     // Botão para executar a busca
    @FXML private Button btnLimpar;        // Botão para limpar a busca
    
    // Campos de detalhes do serviço (readonly - apenas para exibição)
    @FXML private TextField txtId;          // Exibe o ID do serviço (readonly)
    @FXML private TextField txtDescricao;   // Exibe a descrição do serviço (readonly)
    
    // Área de texto para exibir resultados/mensagens
    @FXML private TextArea txtResultado;
    
    // Tabela de serviços
    @FXML private TableView<ServicoEntity> tblServicos;  // Tabela principal
    @FXML private TableColumn<ServicoEntity, Integer> colId;        // Coluna ID
    @FXML private TableColumn<ServicoEntity, String> colDescricao;  // Coluna Descrição
    
    // ==================== ATRIBUTOS PRIVADOS ====================
    
    private ServicoDAO servicoDAO;                      // Objeto para operações no banco
    private ObservableList<ServicoEntity> listaServicos; // Lista que alimenta a tabela
    
    // ==================== MÉTODO INICIALIZADOR ====================
    
    /**
     * Método chamado AUTOMATICAMENTE após o carregamento do FXML.
     * Responsável por:
     * - Inicializar os objetos DAO
     * - Configurar as colunas da tabela
     * - Carregar dados iniciais
     * - Configurar eventos dos botões
     * - Configurar listener para seleção de linha na tabela
     * 
     * NOTA: Não há configuração de permissões aqui porque esta tela
     * é apenas de consulta e não requer permissões especiais.
     */
    @FXML
    public void initialize() {
        // Inicializa o objeto de acesso ao banco de dados
        servicoDAO = new ServicoDAO();
        
        // Cria uma lista observável (qualquer alteração atualiza a tabela automaticamente)
        listaServicos = FXCollections.observableArrayList();
        
        // ==================== CONFIGURAÇÃO DAS COLUNAS DA TABELA ====================
        // PropertyValueFactory mapeia o atributo da classe ServicoEntity para a coluna
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colDescricao != null) colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        
        // ==================== CARREGAMENTO DE DADOS ====================
        carregarTodos();  // Carrega todos os serviços do banco ao iniciar
        
        // ==================== EVENTOS DOS BOTÕES ====================
        if (btnPesquisar != null) btnPesquisar.setOnAction(e -> pesquisar());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparTudo());
        
        // ==================== LISTENER DA TABELA ====================
        // Quando o usuário clica em um serviço na tabela, carrega seus detalhes nos campos
        if (tblServicos != null) {
            tblServicos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) {
                        exibirDetalhes(novo);
                    }
                });
        }
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * CARREGA A TABELA COM TODOS OS SERVIÇOS DO BANCO
     * 
     * Este método consulta o banco de dados através do DAO,
     * limpa a lista atual e adiciona todos os serviços.
     * A tabela é atualizada automaticamente por causa da ObservableList.
     * 
     * A área de resultado exibe a contagem total de serviços.
     */
    private void carregarTodos() {
        try {
            listaServicos.clear();                    // Limpa a lista atual
            listaServicos.addAll(servicoDAO.listarTodos()); // Adiciona todos os serviços
            tblServicos.setItems(listaServicos);      // Atualiza a tabela
            txtResultado.setText("Total de serviços: " + listaServicos.size());
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar serviços: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * PESQUISA SERVIÇOS POR DESCRIÇÃO
     * 
     * O campo de busca permite filtrar serviços cuja descrição contenha
     * o texto digitado (busca case-insensitive usando ILIKE no PostgreSQL).
     * 
     * Funcionamento:
     * - Se o campo de busca estiver vazio → lista todos os serviços
     * - Se o campo tiver texto → busca serviços que contenham o texto
     * 
     * A área de resultado exibe quantos serviços foram encontrados
     * ou uma mensagem informando que nenhum foi encontrado.
     */
    private void pesquisar() {
        String busca = txtBusca.getText().trim();  // Remove espaços extras do início/fim
        
        // Se a busca estiver vazia, carrega todos os serviços
        if (busca.isEmpty()) {
            carregarTodos();
            return;
        }
        
        try {
            // Realiza a busca no banco de dados
            listaServicos.clear();
            listaServicos.addAll(servicoDAO.buscarPorDescricao(busca));
            tblServicos.setItems(listaServicos);
            
            // Exibe resultado da busca na área de texto
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
    
    /**
     * EXIBE OS DETALHES DO SERVIÇO SELECIONADO
     * 
     * Quando o usuário clica em um serviço na tabela, este método
     * carrega os dados nos campos de detalhes (ID e Descrição).
     * 
     * @param servico O serviço selecionado na tabela
     */
    private void exibirDetalhes(ServicoEntity servico) {
        if (txtId != null) txtId.setText(String.valueOf(servico.getId()));
        if (txtDescricao != null) txtDescricao.setText(servico.getDescricao());
    }
    
    /**
     * LIMPA TODOS OS CAMPOS DA TELA
     * 
     * - Limpa o campo de busca
     * - Recarrega a tabela com todos os serviços
     * - Limpa os campos de detalhes
     * - Remove a seleção da tabela
     */
    private void limparTudo() {
        txtBusca.clear();           // Limpa campo de busca
        carregarTodos();            // Recarrega tabela com todos os serviços
        limparDetalhes();           // Limpa campos de detalhes
    }
    
    /**
     * LIMPA OS CAMPOS DE DETALHES DO SERVIÇO
     * 
     * Remove o texto dos campos ID e Descrição,
     * e limpa a seleção da tabela.
     */
    private void limparDetalhes() {
        if (txtId != null) txtId.clear();
        if (txtDescricao != null) txtDescricao.clear();
        tblServicos.getSelectionModel().clearSelection();
    }
    
    /**
     * EXIBE UMA MENSAGEM DE ALERTA/INFORMAÇÃO PARA O USUÁRIO
     * 
     * Utiliza o componente Alert do JavaFX para mostrar mensagens popup.
     * 
     * @param mensagem O texto da mensagem a ser exibida
     */
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);  // Sem cabeçalho, apenas o conteúdo
        alert.setContentText(mensagem);
        alert.showAndWait();  // Exibe e aguarda o usuário clicar em OK
    }
}
/**
 * CONTROLADOR DA TELA DE PRODUTOS
 * 
 * Esta classe é responsável por gerenciar o cadastro de produtos no sistema.
 * Produtos são os equipamentos eletrônicos que chegam para conserto/manutenção.
 * 
 * Funcionalidades:
 * - Inserir novos produtos
 * - Buscar produtos por nome
 * - Deletar produtos
 * - Exibir lista de produtos em uma tabela
 * - Carregar dados do produto selecionado nos campos do formulário
 * 
 * Campos do produto:
 * - Nome: Nome/descrição do produto
 * - Tipo: Tipo do produto (Smartphone, Notebook, TV, etc.)
 * - Modelo: Modelo específico (iPhone 12, Galaxy S21, etc.)
 * - Marca: Fabricante (Apple, Samsung, LG, etc.)
 * - Categoria: Categoria do produto (Celular, Computador, Áudio, etc.)
 * - Defeito: Descrição do problema apresentado
 * 
 * @author joao
 */
package com.eletronica.controller;

// Imports da aplicação
import com.eletronica.dao.ProdutoDAO;      // Classe de acesso ao banco de dados
import com.eletronica.model.ProdutoEntity; // Modelo/entidade Produto

// Imports do JavaFX
import javafx.collections.FXCollections;   // Utilitários para listas observáveis
import javafx.collections.ObservableList;  // Lista que notifica mudanças na UI
import javafx.fxml.FXML;                   // Anotação para vincular componentes do FXML
import javafx.scene.control.*;             // Componentes da UI (TextField, Button, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Mapeia atributos para colunas

// Import para tratamento de erros SQL
import java.sql.SQLException;

public class ProdutoController {
    
    // ==================== COMPONENTES DO FXML ====================
    // Estes atributos são injetados automaticamente pelo JavaFX
    // Os nomes devem ser IDÊNTICOS aos fx:id definidos no Scene Builder
    
    // Campos do formulário
    @FXML private TextField txtNome;       // Nome do produto
    @FXML private TextField txtTipo;       // Tipo (Smartphone, Notebook, etc.)
    @FXML private TextField txtModelo;     // Modelo específico
    @FXML private TextField txtMarca;      // Fabricante
    @FXML private TextField txtCategoria;  // Categoria do produto
    @FXML private TextField txtDefeito;    // Defeito apresentado
    
    @FXML private TextField txtBusca;      // Campo de busca por nome
    
    // Botões
    @FXML private Button btnSalvar;        // Salvar produto
    @FXML private Button btnLimpar;        // Limpar formulário
    @FXML private Button btnBuscar;        // Buscar produtos
    @FXML private Button btnDeletar;       // Deletar produto
    
    // Tabela de produtos
    @FXML private TableView<ProdutoEntity> tblProdutos;  // Tabela principal
    @FXML private TableColumn<ProdutoEntity, Integer> colId;        // Coluna ID
    @FXML private TableColumn<ProdutoEntity, String> colNome;       // Coluna Nome
    @FXML private TableColumn<ProdutoEntity, String> colTipo;       // Coluna Tipo
    @FXML private TableColumn<ProdutoEntity, String> colModelo;     // Coluna Modelo
    @FXML private TableColumn<ProdutoEntity, String> colMarca;      // Coluna Marca
    @FXML private TableColumn<ProdutoEntity, String> colCategoria;  // Coluna Categoria
    @FXML private TableColumn<ProdutoEntity, String> colDefeito;    // Coluna Defeito
    
    // ==================== ATRIBUTOS PRIVADOS ====================
    
    private ProdutoDAO produtoDAO;                      // Objeto para operações no banco
    private ObservableList<ProdutoEntity> listaProdutos; // Lista que alimenta a tabela
    
    // ==================== MÉTODO INICIALIZADOR ====================
    
    /**
     * Método chamado AUTOMATICAMENTE após o carregamento do FXML.
     * Responsável por:
     * - Inicializar os objetos DAO
     * - Configurar as colunas da tabela
     * - Carregar dados iniciais
     * - Configurar eventos dos botões
     * - Configurar listener para seleção de linha na tabela
     */
    @FXML
    public void initialize() {
        System.out.println("Inicializando ProdutoController...");
        
        // Inicializa o objeto de acesso ao banco de dados
        produtoDAO = new ProdutoDAO();
        
        // Cria uma lista observável (qualquer alteração atualiza a tabela automaticamente)
        listaProdutos = FXCollections.observableArrayList();
        
        // ==================== VERIFICAÇÃO DOS COMPONENTES (DEBUG) ====================
        if (colId == null) System.out.println("ERRO: colId é null!");
        if (colNome == null) System.out.println("ERRO: colNome é null!");
        if (tblProdutos == null) System.out.println("ERRO: tblProdutos é null!");
        
        // ==================== CONFIGURAÇÃO DAS COLUNAS DA TABELA ====================
        // PropertyValueFactory mapeia o atributo da classe ProdutoEntity para a coluna
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colTipo != null) colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        if (colModelo != null) colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        if (colMarca != null) colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        if (colCategoria != null) colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        if (colDefeito != null) colDefeito.setCellValueFactory(new PropertyValueFactory<>("defeito"));
        
        // ==================== CARREGAMENTO DE DADOS ====================
        carregarTabela();  // Carrega todos os produtos do banco
        
        // ==================== EVENTOS DOS BOTÕES ====================
        if (btnSalvar != null) btnSalvar.setOnAction(e -> salvarProdutos());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparCampos());
        if (btnBuscar != null) btnBuscar.setOnAction(e -> buscarProdutos());
        if (btnDeletar != null) btnDeletar.setOnAction(e -> deletarProdutos());
        
        // ==================== LISTENER DA TABELA ====================
        // Quando o usuário clica em um produto na tabela, carrega seus dados nos campos
        if (tblProdutos != null) {
            tblProdutos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) carregarCampos(novo);
                });
        }
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * CARREGA A TABELA COM TODOS OS PRODUTOS DO BANCO
     * 
     * Este método consulta o banco de dados através do DAO,
     * limpa a lista atual e adiciona os novos dados.
     * A tabela é atualizada automaticamente por causa da ObservableList.
     */
    private void carregarTabela() {
        try {
            listaProdutos.clear();                          // Limpa a lista atual
            listaProdutos.addAll(produtoDAO.listarTodos()); // Adiciona todos os produtos
            if (tblProdutos != null) tblProdutos.setItems(listaProdutos); // Atualiza a tabela
        } catch (Exception e) {
            mostrarAlerta("Erro ao carregar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * SALVA UM NOVO PRODUTO NO BANCO DE DADOS
     * 
     * Este método:
     * 1. Valida se o nome foi preenchido
     * 2. Coleta os dados dos campos do formulário
     * 3. Cria um objeto ProdutoEntity com os dados
     * 4. Chama o DAO para inserir no banco
     * 5. Limpa o formulário e atualiza a tabela
     */
    private void salvarProdutos() {
        System.out.println("=== SALVAR PRODUTO ===");
        
        // ==================== VALIDAÇÃO DO NOME ====================
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
        
        // ==================== COLETA DOS DADOS ====================
        // Usa operador ternário para evitar NullPointerException
        String tipo = txtTipo != null ? txtTipo.getText() : "";
        String modelo = txtModelo != null ? txtModelo.getText() : "";
        String marca = txtMarca != null ? txtMarca.getText() : "";
        String categoria = txtCategoria != null ? txtCategoria.getText() : "";
        String defeito = txtDefeito != null ? txtDefeito.getText() : "";
        
        // Log para debug
        System.out.println("Dados coletados:");
        System.out.println("  Nome: " + nome);
        System.out.println("  Tipo: " + tipo);
        System.out.println("  Modelo: " + modelo);
        System.out.println("  Marca: " + marca);
        System.out.println("  CATEGORIA: " + categoria);
        System.out.println("  DEFEITO: " + defeito);
        
        // ==================== CRIAÇÃO DO OBJETO PRODUTO ====================
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome(nome);
        produto.setTipo(tipo);
        produto.setModelo(modelo);
        produto.setMarca(marca);
        produto.setCategoria(categoria);
        produto.setDefeito(defeito);
        
        // ==================== INSERÇÃO NO BANCO ====================
        try {
            System.out.println("Chamando produtoDAO.inserir()...");
            produtoDAO.inserir(produto);  // O ID é gerado automaticamente pelo banco
            System.out.println("INSERIDO COM SUCESSO! ID gerado: " + produto.getId());
            
            // Feedback ao usuário
            mostrarAlerta("Produto salvo com sucesso! ID: " + produto.getId());
            
            // Limpeza e atualização
            limparCampos();      // Limpa o formulário
            carregarTabela();    // Recarrega a tabela com o novo produto
            
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
     * BUSCA PRODUTOS POR NOME (OU LISTA TODOS SE O CAMPO ESTIVER VAZIO)
     * 
     * O campo de busca permite filtrar produtos pelo nome usando LIKE.
     * Se o campo estiver vazio, lista todos os produtos.
     */
    private void buscarProdutos() {
        try {
            String nomeBusca = txtBusca != null ? txtBusca.getText() : "";
            listaProdutos.clear();
            
            if (nomeBusca.isEmpty()) {
                // Busca vazia → carrega todos
                listaProdutos.addAll(produtoDAO.listarTodos());
            } else {
                // Busca por nome (usando ILIKE no PostgreSQL)
                listaProdutos.addAll(produtoDAO.buscarPorNome(nomeBusca));
            }
            if (tblProdutos != null) tblProdutos.setItems(listaProdutos);
        } catch (Exception e) {
            mostrarAlerta("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * DELETA UM PRODUTO DO BANCO DE DADOS
     * 
     * Antes de deletar, pede confirmação ao usuário.
     * O produto deve estar selecionado na tabela.
     */
    private void deletarProdutos() {
        if (tblProdutos == null) return;
        
        // Obtém o produto selecionado na tabela
        ProdutoEntity selecionado = tblProdutos.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            mostrarAlerta("Selecione um produto para deletar!");
            return;
        }
        
        // CAIXA DE CONFIRMAÇÃO
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Tem certeza?");
        confirm.setContentText("Produto: " + selecionado.getNome());
        
        // Só deleta se o usuário confirmar
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                produtoDAO.deletar(selecionado.getId());
                mostrarAlerta("Produto deletado!");
                limparCampos();      // Limpa o formulário
                carregarTabela();    // Recarrega a tabela sem o produto deletado
            } catch (Exception e) {
                mostrarAlerta("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * CARREGA OS DADOS DE UM PRODUTO NOS CAMPOS DO FORMULÁRIO
     * 
     * Usado quando o usuário clica em um produto na tabela.
     * Permite visualizar/editar os dados antes de salvar.
     * 
     * @param produto O produto selecionado na tabela
     */
    private void carregarCampos(ProdutoEntity produto) {
        if (txtNome != null) txtNome.setText(produto.getNome());
        if (txtTipo != null) txtTipo.setText(produto.getTipo());
        if (txtModelo != null) txtModelo.setText(produto.getModelo());
        if (txtMarca != null) txtMarca.setText(produto.getMarca());
        if (txtCategoria != null) txtCategoria.setText(produto.getCategoria());
        // Defeito pode ser null, então verifica antes
        if (txtDefeito != null) txtDefeito.setText(produto.getDefeito() != null ? produto.getDefeito() : "");
    }
    
    /**
     * LIMPA TODOS OS CAMPOS DO FORMULÁRIO E LIMPA A SELEÇÃO DA TABELA
     * 
     * Prepara a tela para um novo cadastro.
     */
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
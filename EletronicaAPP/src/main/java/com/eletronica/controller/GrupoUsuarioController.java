/**
 * CONTROLADOR DA TELA DE GRUPOS DE USUÁRIOS
 * 
 * Esta classe é responsável por gerenciar a interface gráfica de cadastro de grupos de usuários.
 * Grupos definem quais permissões um usuário tem no sistema (ex: Administrador, Técnico, Recepcionista).
 * 
 * Funcionalidades:
 * - Inserir novos grupos de usuários
 * - Buscar grupos por descrição
 * - Deletar grupos
 * - Exibir lista de grupos em uma tabela
 * - Carregar dados do grupo selecionado nos campos do formulário
 * - Controlar permissões baseadas no usuário logado (quem pode gerenciar grupos)
 * 
 * @author joao
 */
package com.eletronica.controller;


import com.eletronica.util.UsuarioSessao;   // Classe que gerencia a sessão do usuário logado
import com.eletronica.dao.GrupoUsuarioDAO;
import com.eletronica.model.GrupoUsuarioEntity;
import javafx.collections.FXCollections;   // Utilitários para listas observáveis
import javafx.collections.ObservableList;  // Lista que notifica mudanças na UI
import javafx.fxml.FXML;                   // Anotação para vincular componentes do FXML
import javafx.scene.control.*;             // Componentes da UI (TextField, Button, CheckBox, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Mapeia atributos para colunas da tabela

public class GrupoUsuarioController {
    
    @FXML private TextField txtDescricao;      
    @FXML private TextField txtGrupo;          
    @FXML private CheckBox chkPermissao;       
    @FXML private CheckBox chkManterUsuario;   
    @FXML private CheckBox chkManterServico;  
    
    // Botões
    @FXML private Button btnSalvar;    
    @FXML private Button btnLimpar;    
    @FXML private Button btnDeletar;   
    @FXML private Button btnBuscar;       
    @FXML private TextField txtBusca;   
    
    // Tabela de grupos
    @FXML private TableView<GrupoUsuarioEntity> tblGrupos;  // Tabela principal
    @FXML private TableColumn<GrupoUsuarioEntity, Integer> colId;        // Coluna ID
    @FXML private TableColumn<GrupoUsuarioEntity, String> colDescricao;  // Coluna Descrição
    @FXML private TableColumn<GrupoUsuarioEntity, Integer> colGrupo;     // Coluna Código
    @FXML private TableColumn<GrupoUsuarioEntity, Boolean> colPermissao; // Coluna Permissão
    @FXML private TableColumn<GrupoUsuarioEntity, Boolean> colManterUsuario; // Coluna Manter Usuário
    @FXML private TableColumn<GrupoUsuarioEntity, Boolean> colManterServico; // Coluna Manter Serviço
    
    // Área de texto para exibir resultados/mensagens
    @FXML private TextArea txtResultado;
    
  
    
    private GrupoUsuarioDAO grupoDAO;                     
    private ObservableList<GrupoUsuarioEntity> listaGrupos; 
    
   
    @FXML
    public void initialize() {
        // Inicializa o objeto de acesso ao banco de dados
        grupoDAO = new GrupoUsuarioDAO();
        
        // Cria uma lista observável (qualquer alteração atualiza a tabela automaticamente)
        listaGrupos = FXCollections.observableArrayList();
        
        // CONFIGURAÇÃO DAS COLUNAS DA TABELA
        // PropertyValueFactory mapeia o atributo da classe GrupoUsuarioEntity para a coluna
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colPermissao.setCellValueFactory(new PropertyValueFactory<>("permissao"));
        colManterUsuario.setCellValueFactory(new PropertyValueFactory<>("manterUsuario"));
        colManterServico.setCellValueFactory(new PropertyValueFactory<>("manterServico"));
        
        // Carrega todos os grupos do banco e exibe na tabela
        carregarTabela();
        
        // CONFIGURAÇÃO DOS EVENTOS DOS BOTÕES
        btnSalvar.setOnAction(e -> salvar());   // Salvar grupo
        btnLimpar.setOnAction(e -> limpar());   // Limpar formulário
        btnDeletar.setOnAction(e -> deletar()); // Deletar grupo
        btnBuscar.setOnAction(e -> buscar());   // Buscar grupos
        
        // CONFIGURAÇÃO DO LISTENER DA TABELA
        // Quando o usuário clica em um grupo na tabela, carrega seus dados nos campos
        tblGrupos.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, novo) -> {
                if (novo != null) carregarCampos(novo);
            });
        
        // CONTROLE DE PERMISSÕES
        // Verifica se o usuário logado tem permissão para gerenciar usuários/grupos
        // Se não tiver, desabilita os botões de salvar e deletar
        if (!UsuarioSessao.podeManterUsuario()) {
            btnSalvar.setDisable(true);   // Botão salvar fica cinza/desabilitado
            btnDeletar.setDisable(true);  // Botão deletar fica cinza/desabilitado
            txtResultado.setText("Você não tem permissão para gerenciar grupos de usuários!");
        }
    }
    
    
    
     // A tabela é atualizada automaticamente por causa da ObservableList.
     
    private void carregarTabela() {
        try {
            listaGrupos.clear();                              // Limpa a lista atual
            listaGrupos.addAll(grupoDAO.listarTodos());      // Adiciona todos os grupos do banco
            tblGrupos.setItems(listaGrupos);                 // Atualiza a tabela
            txtResultado.setText("Total: " + listaGrupos.size() + " grupos");
        } catch (Exception e) {
            txtResultado.setText("Erro ao carregar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * SALVA UM NOVO GRUPO DE USUÁRIO NO BANCO DE DADOS
     * 
     * Este método:
     * 1. Valida se a descrição e código foram preenchidos
     * 2. Coleta os dados dos campos do formulário
     * 3. Cria um objeto GrupoUsuarioEntity com os dados
     * 4. Chama o DAO para inserir no banco
     * 5. Limpa o formulário e atualiza a tabela
     */
    private void salvar() {
        // VALIDAÇÃO: Descrição é obrigatória
        if (txtDescricao.getText().isEmpty()) {
            txtResultado.setText("Descrição é obrigatória!");
            return;
        }
        
        // VALIDAÇÃO: Código do grupo é obrigatório
        if (txtGrupo.getText().isEmpty()) {
            txtResultado.setText("Código do grupo é obrigatório!");
            return;
        }
        
        try {
            // CRIAÇÃO DO OBJETO GRUPO
            GrupoUsuarioEntity grupo = new GrupoUsuarioEntity();
            grupo.setDescricao(txtDescricao.getText());
            grupo.setGrupo(Integer.parseInt(txtGrupo.getText()));  // Converte texto para número
            grupo.setPermissao(chkPermissao.isSelected());         // Pega estado do CheckBox
            grupo.setManterUsuario(chkManterUsuario.isSelected()); // Pega estado do CheckBox
            grupo.setManterServico(chkManterServico.isSelected()); // Pega estado do CheckBox
            
            // INSERÇÃO NO BANCO DE DADOS
            grupoDAO.inserir(grupo);
            txtResultado.setText("Grupo salvo com sucesso! ID: " + grupo.getId());
            
            // LIMPEZA E ATUALIZAÇÃO
            limpar();           // Limpa o formulário
            carregarTabela();   // Recarrega a tabela com o novo grupo
            
        } catch (NumberFormatException e) {
            // Erro quando o código do grupo não é um número válido
            txtResultado.setText("Código do grupo deve ser um número!");
        } catch (Exception e) {
            txtResultado.setText("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * BUSCA GRUPOS POR DESCRIÇÃO (OU LISTA TODOS SE O CAMPO ESTIVER VAZIO)
     * 
     * O campo de busca permite filtrar grupos pela descrição usando LIKE.
     * Se o campo estiver vazio, lista todos os grupos.
     */
    private void buscar() {
        String busca = txtBusca.getText().trim();  // Remove espaços extras do início/fim
        
        try {
            listaGrupos.clear();  // Limpa a lista atual
            
            if (busca.isEmpty()) {
                // Busca vazia → carrega todos
                listaGrupos.addAll(grupoDAO.listarTodos());
            } else {
                // Busca por descrição (usando ILIKE no PostgreSQL)
                listaGrupos.addAll(grupoDAO.buscarPorDescricao(busca));
            }
            
            tblGrupos.setItems(listaGrupos);
            txtResultado.setText("Encontrados: " + listaGrupos.size());
        } catch (Exception e) {
            txtResultado.setText("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * DELETA UM GRUPO DE USUÁRIO DO BANCO DE DADOS
     * 
     * Antes de deletar, pede confirmação ao usuário.
     * O grupo deve estar selecionado na tabela.
     * 
     */
    private void deletar() {
        // Obtém o grupo selecionado na tabela
        GrupoUsuarioEntity selecionado = tblGrupos.getSelectionModel().getSelectedItem();
        
        if (selecionado == null) {
            txtResultado.setText("Selecione um grupo para deletar!");
            return;
        }
        
        // CAIXA DE CONFIRMAÇÃO
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Deletar grupo: " + selecionado.getDescricao());
        confirm.setContentText("Tem certeza?");
        
        // Só deleta se o usuário confirmar
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                grupoDAO.deletar(selecionado.getId());  // Deleta pelo ID
                txtResultado.setText("Grupo deletado com sucesso!");
                
                // LIMPEZA E ATUALIZAÇÃO
                limpar();           // Limpa o formulário
                carregarTabela();   // Recarrega a tabela sem o grupo deletado
            } catch (Exception e) {
                txtResultado.setText("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * CARREGA OS DADOS DE UM GRUPO NOS CAMPOS DO FORMULÁRIO
     * 
     * Usado quando o usuário clica em um grupo na tabela.
     * Permite visualizar os dados antes de editar/salvar.
     * 
     * @param grupo O grupo selecionado na tabela
     */
    private void carregarCampos(GrupoUsuarioEntity grupo) {
        txtDescricao.setText(grupo.getDescricao());
        txtGrupo.setText(String.valueOf(grupo.getGrupo()));  // Converte número para texto
        chkPermissao.setSelected(grupo.isPermissao());
        chkManterUsuario.setSelected(grupo.isManterUsuario());
        chkManterServico.setSelected(grupo.isManterServico());
    }
    
    
    private void limpar() {
        txtDescricao.clear();           // Limpa campo descrição
        txtGrupo.clear();               // Limpa campo código
        chkPermissao.setSelected(false);     // Desmarca permissão
        chkManterUsuario.setSelected(false); // Desmarca manter usuário
        chkManterServico.setSelected(false); // Desmarca manter serviço
        txtBusca.clear();               // Limpa campo de busca
        tblGrupos.getSelectionModel().clearSelection(); // Remove seleção da tabela
    }
}
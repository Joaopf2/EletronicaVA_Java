/**
 * CONTROLADOR DA TELA DE CADASTRO DE USUÁRIOS
 * 
 * Esta classe é responsável por gerenciar o cadastro de usuários no sistema.
 * Os usuários são as pessoas que podem acessar o sistema (funcionários, técnicos, administradores).
 * 
 * Funcionalidades:
 * - Inserir novos usuários
 * - Vincular usuário a um grupo (que define suas permissões)
 * - Validar senha e confirmação de senha
 * - Controlar permissões (apenas quem tem manterUsuario pode cadastrar)
 * 
 * Relacionamentos:
 * - Um usuário pertence a UM grupo (FK: id_grupo_usuario)
 * - As permissões do usuário vêm do grupo ao qual ele pertence
 * 
 * @author joao
 */
package com.eletronica.controller;

// Imports da aplicação
import com.eletronica.dao.GrupoUsuarioDAO;   // DAO para buscar grupos disponíveis
import com.eletronica.dao.UsuarioDAO;        // DAO para operações com usuários
import com.eletronica.model.GrupoUsuarioEntity; // Modelo de Grupo de Usuário
import com.eletronica.model.UsuarioEntity;   // Modelo/entidade Usuário
import com.eletronica.util.UsuarioSessao;    // Classe que gerencia a sessão do usuário

// Imports do JavaFX
import javafx.collections.FXCollections;   // Utilitários para listas observáveis
import javafx.collections.ObservableList;  // Lista que notifica mudanças na UI
import javafx.fxml.FXML;                   // Anotação para vincular componentes do FXML
import javafx.scene.control.*;             // Componentes da UI (TextField, ComboBox, etc.)

public class UsuarioController {

    // ==================== COMPONENTES DO FXML ====================
    // Estes atributos são injetados automaticamente pelo JavaFX
    // Os nomes devem ser IDÊNTICOS aos fx:id definidos no Scene Builder
    
    // Campos do formulário
    @FXML private TextField txtNome;              // Nome completo do usuário
    @FXML private TextField txtEmail;             // Email do usuário (usado para login)
    @FXML private PasswordField txtSenha;         // Senha do usuário (oculta os caracteres)
    @FXML private PasswordField txtConfirmarSenha; // Confirmação de senha (evita erros de digitação)
    @FXML private ComboBox<GrupoUsuarioEntity> cbGrupo; // ComboBox para selecionar o grupo
    
    // Botões
    @FXML private Button btnSalvar;      // Botão para salvar usuário
    @FXML private Button btnLimpar;      // Botão para limpar formulário
    
    // Área de texto para exibir resultados/mensagens
    @FXML private TextArea txtResultado;
    
    // ==================== ATRIBUTOS PRIVADOS ====================
    
    private UsuarioDAO usuarioDAO;                        // DAO para operações de usuário
    private GrupoUsuarioDAO grupoDAO;                    // DAO para buscar grupos
    private ObservableList<GrupoUsuarioEntity> listaGrupos; // Lista de grupos para o ComboBox
    
    // ==================== MÉTODO INICIALIZADOR ====================
    
    /**
     * Método chamado AUTOMATICAMENTE após o carregamento do FXML.
     * Responsável por:
     * - Inicializar os objetos DAO
     * - Carregar a lista de grupos no ComboBox
     * - Configurar eventos dos botões
     * - Controlar permissões baseadas no usuário logado
     */
    @FXML
    public void initialize() {
        // Inicializa DAOs
        usuarioDAO = new UsuarioDAO();
        grupoDAO = new GrupoUsuarioDAO();
        
        // Cria uma lista observável para os grupos
        listaGrupos = FXCollections.observableArrayList();
        
        // Carrega os grupos no ComboBox
        carregarGrupos();
        
        // Configura eventos dos botões
        btnSalvar.setOnAction(e -> salvar());
        btnLimpar.setOnAction(e -> limpar());
        
        // ==================== CONTROLE DE PERMISSÃO ====================
        // Verifica se o usuário logado tem permissão para gerenciar usuários
        // Se não tiver, desabilita o botão salvar (impede cadastro)
        if (!UsuarioSessao.podeManterUsuario()) {
            btnSalvar.setDisable(true);  // Botão fica cinza/desabilitado
            txtResultado.setText("Você não tem permissão para cadastrar usuários!");
        }
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * CARREGA A LISTA DE GRUPOS NO COMBOBOX
     * 
     * Busca todos os grupos de usuários no banco de dados e os adiciona
     * ao ComboBox para que o usuário possa selecionar um grupo.
     * 
     * O ComboBox exibe a descrição do grupo (ex: "Administrador", "Técnico")
     * através do método toString() da classe GrupoUsuarioEntity.
     */
    private void carregarGrupos() {
        try {
            listaGrupos.clear();                        // Limpa a lista atual
            listaGrupos.addAll(grupoDAO.listarTodos()); // Adiciona todos os grupos
            cbGrupo.setItems(listaGrupos);              // Popula o ComboBox
            cbGrupo.setPromptText("Selecione um grupo"); // Texto padrão quando nada está selecionado
        } catch (Exception e) {
            txtResultado.setText("Erro ao carregar grupos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * SALVA UM NOVO USUÁRIO NO BANCO DE DADOS
     * 
     * Este método realiza várias validações antes de inserir:
     * 1. Nome não pode estar vazio
     * 2. Email não pode estar vazio
     * 3. Senha não pode estar vazia
     * 4. Senha e confirmação de senha devem ser iguais
     * 5. Um grupo deve ser selecionado
     * 
     * Após as validações, cria um objeto UsuarioEntity e insere no banco.
     */
    private void salvar() {
        // ==================== VALIDAÇÃO: NOME ====================
        if (txtNome.getText().isEmpty()) {
            txtResultado.setText("Nome é obrigatório!");
            txtNome.requestFocus();  // Coloca o cursor no campo Nome
            return;
        }
        
        // ==================== VALIDAÇÃO: EMAIL ====================
        if (txtEmail.getText().isEmpty()) {
            txtResultado.setText("Email é obrigatório!");
            txtEmail.requestFocus();
            return;
        }
        
        // ==================== VALIDAÇÃO: SENHA ====================
        if (txtSenha.getText().isEmpty()) {
            txtResultado.setText("Senha é obrigatória!");
            txtSenha.requestFocus();
            return;
        }
        
        // ==================== VALIDAÇÃO: CONFIRMAÇÃO DE SENHA ====================
        if (!txtSenha.getText().equals(txtConfirmarSenha.getText())) {
            txtResultado.setText("Senhas não conferem!");
            txtSenha.clear();           // Limpa o campo senha
            txtConfirmarSenha.clear();  // Limpa o campo confirmação
            txtSenha.requestFocus();    // Volta o cursor para o campo senha
            return;
        }
        
        // ==================== VALIDAÇÃO: GRUPO ====================
        if (cbGrupo.getValue() == null) {
            txtResultado.setText("Selecione um grupo!");
            cbGrupo.requestFocus();
            return;
        }
        
        // ==================== CRIAÇÃO DO OBJETO USUÁRIO ====================
        try {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setNome(txtNome.getText());
            usuario.setEmail(txtEmail.getText());
            usuario.setSenha(txtSenha.getText());  // NOTA: Em produção, a senha deveria ser criptografada
            usuario.setIdGrupoUsuario(cbGrupo.getValue().getId());  // Pega o ID do grupo selecionado
            
            // ==================== INSERÇÃO NO BANCO ====================
            usuarioDAO.inserir(usuario);
            txtResultado.setText("Usuário salvo com sucesso! ID: " + usuario.getId());
            
            // ==================== LIMPEZA DO FORMULÁRIO ====================
            limpar();  // Limpa todos os campos para um novo cadastro
            
        } catch (Exception e) {
            txtResultado.setText("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * LIMPA TODOS OS CAMPOS DO FORMULÁRIO
     * 
     * Este método:
     * - Limpa os campos de texto (Nome, Email)
     * - Limpa os campos de senha (Senha, Confirmar Senha)
     * - Limpa a seleção do ComboBox
     * 
     * Prepara a tela para um novo cadastro.
     */
    private void limpar() {
        txtNome.clear();
        txtEmail.clear();
        txtSenha.clear();
        txtConfirmarSenha.clear();
        cbGrupo.setValue(null);  // Remove a seleção do ComboBox
    }
}
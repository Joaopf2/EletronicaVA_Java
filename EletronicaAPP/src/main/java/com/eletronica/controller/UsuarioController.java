/**
 * CONTROLADOR DA TELA DE CADASTRO DE USUÁRIOS
 * 
 * Esta classe é responsável por gerenciar o cadastro de usuários no sistema.
 * Os usuários são as pessoas que podem acessar o sistema (funcionários, técnicos, administradores).
 * 
 * Funcionalidades:
 * - Inserir novos usuários
 * - Vincular usuário a um grupo (que define suas permissões)
 * - Validar senha e confirmação de senha (gravada como hash BCrypt pelo DAO)
 * - Na edição, deixar a senha em branco mantém a senha atual
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
import javafx.scene.control.cell.PropertyValueFactory;

public class UsuarioController {

    // Campos do formulário
    @FXML private TextField txtNome;              // Nome completo do usuário
    @FXML private TextField txtEmail;             // Email do usuário (usado para login)
    @FXML private PasswordField txtSenha;         // Senha do usuário (oculta os caracteres)
    @FXML private PasswordField txtConfirmarSenha; // Confirmação de senha (evita erros de digitação)
    @FXML private ComboBox<GrupoUsuarioEntity> cbGrupo; // ComboBox para selecionar o grupo
    
    // Botões e busca
    @FXML private Button btnSalvar;      // Botão para salvar usuário
    @FXML private Button btnLimpar;      // Botão para limpar formulário
    @FXML private Button btnDeletar;     // Botão para deletar usuário
    @FXML private Button btnBuscar;      // Botão para buscar usuários
    @FXML private TextField txtBusca;    // Campo de busca por nome

    // Tabela de usuários
    @FXML private TableView<UsuarioEntity> tblUsuarios;
    @FXML private TableColumn<UsuarioEntity, Integer> colId;
    @FXML private TableColumn<UsuarioEntity, String> colNome;
    @FXML private TableColumn<UsuarioEntity, String> colEmail;
    @FXML private TableColumn<UsuarioEntity, String> colGrupo;
    
    // Área de texto para exibir resultados/mensagens
    @FXML private TextArea txtResultado;
    
    
    
    private UsuarioDAO usuarioDAO;                        // DAO para operações de usuário
    private GrupoUsuarioDAO grupoDAO;                    // DAO para buscar grupos
    private ObservableList<GrupoUsuarioEntity> listaGrupos; // Lista de grupos para o ComboBox
    private ObservableList<UsuarioEntity> listaUsuarios;    // Lista de usuários para a TableView
    
    @FXML
    public void initialize() {
        // Inicializa DAOs
        usuarioDAO = new UsuarioDAO();
        grupoDAO = new GrupoUsuarioDAO();
        
        // Cria uma lista observável para os grupos e usuários
        listaGrupos = FXCollections.observableArrayList();
        listaUsuarios = FXCollections.observableArrayList();

        // Configura colunas da tabela se presentes no FXML
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colGrupo != null) colGrupo.setCellValueFactory(new PropertyValueFactory<>("nomeGrupo"));
        
        // Carrega os grupos no ComboBox e os usuários na tabela
        carregarGrupos();
        carregarTabela();
        
        // Configura eventos dos botões
        if (btnSalvar != null) btnSalvar.setOnAction(e -> salvar());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limpar());
        if (btnDeletar != null) btnDeletar.setOnAction(e -> deletar());
        if (btnBuscar != null) btnBuscar.setOnAction(e -> buscar());
        if (txtBusca != null) txtBusca.setOnAction(e -> buscar());

        // Listener de seleção da tabela
        if (tblUsuarios != null) {
            tblUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    if (novo != null) carregarCampos(novo);
                });
        }
        
        // ==================== CONTROLE DE PERMISSÃO ====================
        // Verifica se o usuário logado tem permissão para gerenciar usuários
        // Se não tiver, desabilita o botão salvar (impede cadastro)
        if (!UsuarioSessao.podeManterUsuario()) {
            if (btnSalvar != null) btnSalvar.setDisable(true);  // Botão fica cinza/desabilitado
            if (btnDeletar != null) btnDeletar.setDisable(true);
            exibirMensagem("Você não tem permissão para cadastrar usuários!");
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
            exibirMensagem("Erro ao carregar grupos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * CARREGA A TABELA COM TODOS OS USUÁRIOS CADASTRADOS
     */
    private void carregarTabela() {
        if (tblUsuarios == null) return;
        try {
            listaUsuarios.clear();
            listaUsuarios.addAll(usuarioDAO.listarTodos());
            tblUsuarios.setItems(listaUsuarios);
            exibirMensagem("Total de usuários: " + listaUsuarios.size());
        } catch (Exception e) {
            exibirMensagem("Erro ao carregar usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * BUSCA USUÁRIOS PELO NOME
     */
    private void buscar() {
        if (tblUsuarios == null) return;
        String termo = txtBusca != null ? txtBusca.getText().trim() : "";
        try {
            listaUsuarios.clear();
            if (termo.isEmpty()) {
                listaUsuarios.addAll(usuarioDAO.listarTodos());
            } else {
                listaUsuarios.addAll(usuarioDAO.buscarPorNome(termo));
            }
            tblUsuarios.setItems(listaUsuarios);
            exibirMensagem("Encontrados: " + listaUsuarios.size() + " usuário(s)");
        } catch (Exception e) {
            exibirMensagem("Erro na busca: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * SALVA OU ATUALIZA UM USUÁRIO NO BANCO DE DADOS
     * 
     * Este método realiza várias validações antes de inserir/atualizar:
     * 1. Nome não pode estar vazio
     * 2. Email não pode estar vazio
     * 3. Senha não pode estar vazia
     * 4. Senha e confirmação de senha devem ser iguais
     * 5. Um grupo deve ser selecionado
     * 
     * Após as validações, cria um objeto UsuarioEntity e insere ou atualiza no banco.
     */
    private void salvar() {
        // ==================== VALIDAÇÃO: NOME ====================
        if (txtNome.getText().trim().isEmpty()) {
            exibirMensagem("Nome é obrigatório!");
            txtNome.requestFocus();  // Coloca o cursor no campo Nome
            return;
        }
        
        // ==================== VALIDAÇÃO: EMAIL ====================
        if (txtEmail.getText().trim().isEmpty()) {
            exibirMensagem("Email é obrigatório!");
            txtEmail.requestFocus();
            return;
        }
        
        // Usuário selecionado na tabela = modo edição
        UsuarioEntity selecionado = tblUsuarios != null
                ? tblUsuarios.getSelectionModel().getSelectedItem()
                : null;
        boolean edicao = selecionado != null && selecionado.getId() > 0;
        
        // ==================== VALIDAÇÃO: SENHA ====================
        // Obrigatória no cadastro; na edição, em branco = manter a senha atual
        if (txtSenha.getText().isEmpty() && !edicao) {
            exibirMensagem("Senha é obrigatória!");
            txtSenha.requestFocus();
            return;
        }
        
        // ==================== VALIDAÇÃO: CONFIRMAÇÃO DE SENHA ====================
        if (!txtSenha.getText().equals(txtConfirmarSenha.getText())) {
            exibirMensagem("Senhas não conferem!");
            txtSenha.clear();           // Limpa o campo senha
            txtConfirmarSenha.clear();  // Limpa o campo confirmação
            txtSenha.requestFocus();    // Volta o cursor para o campo senha
            return;
        }
        
        // ==================== VALIDAÇÃO: GRUPO ====================
        if (cbGrupo.getValue() == null) {
            exibirMensagem("Selecione um grupo!");
            cbGrupo.requestFocus();
            return;
        }
        
        // ==================== CRIAÇÃO DO OBJETO USUÁRIO ====================
        try {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setNome(txtNome.getText().trim());
            usuario.setEmail(txtEmail.getText().trim());
            // Senha em texto puro apenas em memória: o DAO grava somente o hash BCrypt.
            // Vazia na edição => o DAO mantém a senha atual.
            usuario.setSenha(txtSenha.getText());
            usuario.setIdGrupoUsuario(cbGrupo.getValue().getId());  // Pega o ID do grupo selecionado

            // ==================== INSERÇÃO OU ATUALIZAÇÃO NO BANCO ====================
            if (edicao) {
                usuario.setId(selecionado.getId());
                usuarioDAO.atualizar(usuario);
                exibirMensagem("Usuário atualizado com sucesso! ID: " + usuario.getId());
            } else {
                usuarioDAO.inserir(usuario);
                exibirMensagem("Usuário salvo com sucesso! ID: " + usuario.getId());
            }
            
            // ==================== LIMPEZA E ATUALIZAÇÃO ====================
            limpar();  // Limpa todos os campos para um novo cadastro
            carregarTabela();
            
        } catch (Exception e) {
            exibirMensagem("Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * DELETA O USUÁRIO SELECIONADO NA TABELA
     */
    private void deletar() {
        if (tblUsuarios == null) return;
        UsuarioEntity selecionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirMensagem("Selecione um usuário na tabela para deletar!");
            return;
        }

        if (UsuarioSessao.getUsuarioLogado() != null && selecionado.getId() == UsuarioSessao.getUsuarioLogado().getId()) {
            exibirMensagem("Você não pode deletar o próprio usuário logado!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar exclusão");
        confirm.setHeaderText("Deletar usuário: " + selecionado.getNome());
        confirm.setContentText("Tem certeza que deseja excluir este usuário?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                usuarioDAO.deletar(selecionado.getId());
                exibirMensagem("Usuário deletado com sucesso!");
                limpar();
                carregarTabela();
            } catch (Exception e) {
                exibirMensagem("Erro ao deletar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * CARREGA OS DADOS DO USUÁRIO SELECIONADO NOS CAMPOS DO FORMULÁRIO
     */
    private void carregarCampos(UsuarioEntity usuario) {
        txtNome.setText(usuario.getNome());
        txtEmail.setText(usuario.getEmail());
        // A senha nunca é exibida (no banco existe apenas o hash BCrypt).
        // Campos vazios na edição = manter a senha atual.
        txtSenha.clear();
        txtConfirmarSenha.clear();
        txtSenha.setPromptText("Em branco = manter atual");
        txtConfirmarSenha.setPromptText("Em branco = manter atual");
        for (GrupoUsuarioEntity g : listaGrupos) {
            if (g.getId() == usuario.getIdGrupoUsuario()) {
                cbGrupo.setValue(g);
                break;
            }
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
        txtSenha.setPromptText("********");
        txtConfirmarSenha.setPromptText("********");
        cbGrupo.setValue(null);  // Remove a seleção do ComboBox
        if (txtBusca != null) txtBusca.clear();
        if (tblUsuarios != null) tblUsuarios.getSelectionModel().clearSelection();
    }

    private void exibirMensagem(String msg) {
        if (txtResultado != null) {
            txtResultado.setText(msg);
        }
    }
}
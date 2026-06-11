/**
 * CONTROLADOR DA TELA DE LOGIN
 * 
 * Esta classe é responsável por autenticar os usuários no sistema.
 * Ela valida as credenciais (email e senha) e inicia a sessão do usuário.
 * 
 * Funcionalidades:
 * - Validar email e senha
 * - Buscar usuário no banco de dados
 * - Verificar se o usuário existe e a senha está correta
 * - Criar sessão para o usuário logado
 * - Abrir o menu principal após login bem-sucedido
 * 
 * @author joao
 */
package com.eletronica.controller;

import com.eletronica.dao.UsuarioDAO;           // Classe de acesso ao banco de dados de usuários
import com.eletronica.model.GrupoUsuarioEntity; // Modelo de Grupo de Usuário
import com.eletronica.model.UsuarioEntity;      // Modelo/entidade Usuário
import com.eletronica.util.UsuarioSessao;       // Classe que gerencia a sessão do usuário

// Imports do JavaFX
import javafx.fxml.FXML;           // Anotação para vincular componentes do FXML
import javafx.fxml.FXMLLoader;     // Carrega arquivos FXML
import javafx.scene.Parent;        // Nó base para a cena
import javafx.scene.Scene;         // Representa a cena/interface
import javafx.scene.control.*;     // Componentes da UI (TextField, PasswordField, Button, Label)
import javafx.stage.Stage;         // Janela da aplicação

public class LoginController {
    
    // ==================== COMPONENTES DO FXML ====================
    // Estes atributos são injetados automaticamente pelo JavaFX
    // Os nomes devem ser IDÊNTICOS aos fx:id definidos no Scene Builder
    
    @FXML private TextField txtEmail;          // Campo para digitar o email
    @FXML private PasswordField txtSenha;      // Campo para digitar a senha (oculta os caracteres)
    @FXML private Button btnEntrar;            // Botão para realizar o login
    @FXML private Button btnCancelar;          // Botão para cancelar/fechar o sistema
    @FXML private Label lblMensagem;           // Label para exibir mensagens de erro/sucesso
    
    // ==================== ATRIBUTOS PRIVADOS ====================
    
    private UsuarioDAO usuarioDAO;  // Objeto para operações no banco de dados
    
    // ==================== MÉTODO INICIALIZADOR ====================
    
    /**
     * Método chamado AUTOMATICAMENTE após o carregamento do FXML.
     * Responsável por:
     * - Inicializar o objeto DAO
     * - Configurar eventos dos botões
     * - Permitir login ao pressionar ENTER no campo senha
     */
    @FXML
    public void initialize() {
        // Inicializa o objeto de acesso ao banco de dados
        usuarioDAO = new UsuarioDAO();
        
        // CONFIGURAÇÃO DOS EVENTOS
        // Ao clicar no botão "Entrar", executa o método fazerLogin()
        btnEntrar.setOnAction(e -> fazerLogin());
        
        // Ao clicar no botão "Cancelar", fecha a aplicação completamente
        btnCancelar.setOnAction(e -> System.exit(0));
        
        // Permite fazer login pressionando a tecla ENTER quando o campo senha estiver focado
        // Isso melhora a experiência do usuário (não precisa clicar no botão)
        txtSenha.setOnAction(e -> fazerLogin());
    }
    
    // ==================== MÉTODOS PRIVADOS ====================
    
    /**
     * REALIZA O PROCESSO DE AUTENTICAÇÃO DO USUÁRIO
     * 
     * Este método:
     * 1. Coleta email e senha digitados
     * 2. Valida se os campos não estão vazios
     * 3. Busca o usuário no banco de dados pelo email
     * 4. Verifica se o usuário existe e a senha está correta
     * 5. Cria uma sessão para o usuário logado
     * 6. Abre o menu principal
     * 7. Fecha a tela de login
     */
    private void fazerLogin() {
        // Coleta os dados dos campos e remove espaços extras
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText();  // Senha não tem trim para permitir espaços
        // Nota: Não se usa trim() na senha porque a senha pode conter espaços propositais
        
        // ==================== VALIDAÇÕES ====================
        
        // VALIDAÇÃO: Email não pode estar vazio
        if (email.isEmpty()) {
            lblMensagem.setText("Digite o email!");  // Exibe mensagem de erro
            txtEmail.requestFocus();                 // Coloca o cursor no campo email
            return;  // Interrompe a execução
        }
        
        // VALIDAÇÃO: Senha não pode estar vazia
        if (senha.isEmpty()) {
            lblMensagem.setText("Digite a senha!");
            txtSenha.requestFocus();  // Coloca o cursor no campo senha
            return;
        }
        
        // ==================== AUTENTICAÇÃO NO BANCO ====================
        
        try {
            // Busca o usuário no banco pelo email (inclui os dados do grupo)
            // O método buscarPorEmailComGrupo retorna null se o email não existir
            UsuarioEntity usuario = usuarioDAO.buscarPorEmailComGrupo(email);
            
            // Verifica se o usuário existe
            if (usuario == null) {
                lblMensagem.setText("Usuário não encontrado!");
                txtEmail.requestFocus();
                return;
            }
            
            // Verifica se a senha está correta
            // NOTA: Em um sistema real, a senha deveria estar criptografada (hash)
            // Aqui estamos comparando texto puro por simplicidade
            if (!usuario.getSenha().equals(senha)) {
                lblMensagem.setText("Senha incorreta!");
                txtSenha.clear();        // Limpa o campo senha
                txtSenha.requestFocus(); // Foca novamente no campo senha
                return;
            }
            
            // ==================== LOGIN BEM-SUCEDIDO ====================
            
            // Cria a sessão do usuário (armazena quem está logado e suas permissões)
            // O segundo parâmetro (usuario.getGrupo()) contém todas as permissões do grupo
            UsuarioSessao.login(usuario, usuario.getGrupo());
            
            // Abre a janela principal do sistema (menu)
            abrirMenuPrincipal();
            
            // Fecha a janela de login
            // Obtém a referência da Stage atual através do botão "Entrar"
            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            stage.close();
            
        } catch (Exception e) {
            // Captura qualquer erro durante o processo (ex: falha na conexão com o banco)
            lblMensagem.setText("Erro: " + e.getMessage());
            e.printStackTrace();  // Imprime o erro completo no console para debug
        }
    }
    
    /**
     * ABRE A TELA PRINCIPAL DO SISTEMA (MENU)
     * 
     * Este método:
     * 1. Carrega o arquivo FXML do menu principal
     * 2. Cria uma nova janela (Stage)
     * 3. Configura o título e tamanho da janela
     * 4. Exibe a janela em modo maximizado
     * 
     * @throws Exception Se o arquivo FXML não for encontrado ou houver erro no carregamento
     */
    private void abrirMenuPrincipal() throws Exception {
        // Carrega o arquivo FXML do menu principal
        // O caminho deve começar com "/" para buscar a partir da raiz das resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eletronica/view/FrmMenuPrincipal.fxml"));
        Parent root = loader.load();  // Carrega o layout
        
        // Cria uma nova janela (Stage)
        Stage stage = new Stage();
        stage.setTitle("Eletrônica - Sistema de Gestão");  // Título da janela
        
        // Define a cena com tamanho inicial 1200x800 pixels
        stage.setScene(new Scene(root, 1200, 800));
        
        // Maximiza a janela para ocupar toda a tela
        // O usuário ainda pode restaurar para o tamanho normal se quiser
        stage.setMaximized(true);
        
        // Exibe a janela
        stage.show();
    }
}
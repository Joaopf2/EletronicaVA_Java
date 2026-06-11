/**
 * CONTROLADOR DA TELA DE MENU PRINCIPAL
 * 
 * Esta classe é responsável por gerenciar a tela principal do sistema.
 * Ela exibe o menu com botões para acessar todas as funcionalidades do sistema,
 * controla as permissões do usuário logado (escondendo botões não autorizados),
 * e centraliza a abertura de todas as outras telas.
 * 
 * Funcionalidades:
 * - Exibir informações do usuário logado (nome e perfil)
 * - Controlar visibilidade dos botões baseado nas permissões do usuário
 * - Abrir telas de Cliente, Produto, Serviço, Ordem de Serviço, Usuário e Grupo
 * - Centralizar a abertura de telas com tamanhos padronizados
 * - Sair do sistema com confirmação
 * 
 * @author joao
 */
package com.eletronica.controller;

// Imports da aplicação
import com.eletronica.util.UsuarioSessao;   // Classe que gerencia a sessão do usuário logado

// Imports do JavaFX
import javafx.fxml.FXML;           // Anotação para vincular componentes do FXML
import javafx.fxml.FXMLLoader;     // Carrega arquivos FXML
import javafx.scene.Parent;        // Nó base para a cena
import javafx.scene.Scene;         // Representa a cena/interface
import javafx.scene.control.Alert; // Caixa de diálogo para alertas/confirmações
import javafx.scene.control.Button; // Botão da interface
import javafx.scene.control.ButtonType; // Tipos de botões para diálogos
import javafx.scene.control.Label; // Label (texto) da interface
import javafx.stage.Stage;         // Janela da aplicação

public class MenuPrincipalController {

    // ==================== COMPONENTES DO FXML ====================
    // Estes atributos são injetados automaticamente pelo JavaFX
    // Os nomes devem ser IDÊNTICOS aos fx:id definidos no Scene Builder
    
    @FXML private Button btnClientes;        // Botão para abrir tela de Clientes
    @FXML private Button btnProdutos;        // Botão para abrir tela de Produtos
    @FXML private Button btnServicos;        // Botão para abrir tela de Serviços
    @FXML private Button btnOrdens;          // Botão para abrir tela de Ordem de Serviço
    @FXML private Button btnUsuarios;        // Botão para abrir tela de Usuários
    @FXML private Button btnGrupoUsuario;    // Botão para abrir tela de Grupos de Usuários
    @FXML private Label lblUsuarioLogado;    // Label que exibe o nome do usuário logado
    @FXML private Label lblPerfil;           // Label que exibe o perfil do usuário logado

    // ==================== MÉTODO INICIALIZADOR ====================
    
    /**
     * Método chamado AUTOMATICAMENTE após o carregamento do FXML.
     * Responsável por:
     * - Verificar se há um usuário logado
     * - Exibir informações do usuário na tela
     * - Controlar permissões (esconder botões não autorizados)
     * - Configurar eventos dos botões
     * - Configurar ação do botão sair
     */
    @FXML
    public void initialize() {
        System.out.println("=== INICIALIZANDO MENU ===");

        // ==================== VERIFICAÇÃO DE SESSÃO ====================
        // Verifica se existe um usuário logado na sessão
        // Isso é crucial para evitar acesso indevido ao sistema
        if (!UsuarioSessao.isLogado()) {
            System.out.println("ERRO: Nenhum usuário logado!");
            return;  // Interrompe a inicialização se não houver usuário logado
        }

        // ==================== EXIBIR INFORMAÇÕES DO USUÁRIO ====================
        // Carrega os dados do usuário logado nos labels do menu
        if (lblUsuarioLogado != null) {
            lblUsuarioLogado.setText("Usuário: " + UsuarioSessao.getUsuarioLogado().getNome());
        }
        if (lblPerfil != null) {
            lblPerfil.setText("Perfil: " + UsuarioSessao.getGrupoLogado().getDescricao());
        }

        // ==================== CONTROLE DE PERMISSÕES ====================
        // Esconde botões que o usuário não tem permissão para acessar
        // Isso é feito para melhorar a experiência do usuário
        if (!UsuarioSessao.podeManterUsuario()) {
            if (btnUsuarios != null) btnUsuarios.setVisible(false);
            if (btnGrupoUsuario != null) btnGrupoUsuario.setVisible(false);
            System.out.println("Botões de usuário/grupo ocultos (sem permissão)");
        }
        // Nota: Os botões de Serviço, Cliente, Produto e OS são visíveis para todos
        // que conseguiram fazer login

        // ==================== VERIFICAÇÃO DOS BOTÕES (DEBUG) ====================
        // Log para verificar se os botões foram carregados corretamente
        System.out.println("btnClientes: " + (btnClientes == null ? "NULL" : "OK"));
        System.out.println("btnProdutos: " + (btnProdutos == null ? "NULL" : "OK"));
        System.out.println("btnServicos: " + (btnServicos == null ? "NULL" : "OK"));
        System.out.println("btnOrdens: " + (btnOrdens == null ? "NULL" : "OK"));
        System.out.println("btnUsuarios: " + (btnUsuarios == null ? "NULL" : "OK"));
        System.out.println("btnGrupoUsuario: " + (btnGrupoUsuario == null ? "NULL" : "OK"));

        // ==================== CONFIGURAÇÃO DOS EVENTOS ====================
        // Cada botão executa seu respectivo método quando clicado
        // Verificação de null por segurança (caso algum botão não exista no FXML)
        if (btnClientes != null) btnClientes.setOnAction(e -> abrirTelaClientes());
        if (btnProdutos != null) btnProdutos.setOnAction(e -> abrirTelaProdutos());
        if (btnServicos != null) btnServicos.setOnAction(e -> abrirTelaServicos());
        if (btnOrdens != null) btnOrdens.setOnAction(e -> abrirTelaOrdens());
        if (btnUsuarios != null) btnUsuarios.setOnAction(e -> abrirTelaUsuarios());
        if (btnGrupoUsuario != null) btnGrupoUsuario.setOnAction(e -> abrirTelaGrupoUsuarios());

        // Log de conclusão
        System.out.println("Menu inicializado com sucesso!");
        System.out.println("Usuário: " + UsuarioSessao.getUsuarioLogado().getNome());
        System.out.println("Perfil: " + UsuarioSessao.getGrupoLogado().getDescricao());
    }

    // ==================== MÉTODO CENTRALIZADO ====================
    
    /**
     * MÉTODO CENTRALIZADO PARA ABRIR TELAS
     * 
     * Este método encapsula toda a lógica de abertura de telas:
     * - Carrega o arquivo FXML
     * - Cria uma nova janela (Stage)
     * - Define título, tamanho e tamanho mínimo
     * - Exibe a janela
     * 
     * Vantagens:
     * - Evita código duplicado
     * - Padroniza o comportamento de todas as telas
     * - Facilita manutenção (muda uma vez, afeta todas)
     * 
     * @param fxmlPath Caminho para o arquivo FXML (ex: "/com/eletronica/view/FrmCliente.fxml")
     * @param titulo Título que aparece na barra da janela
     * @param width Largura inicial da janela em pixels
     * @param height Altura inicial da janela em pixels
     */
    private void abrirTela(String fxmlPath, String titulo, int width, int height) {
        try {
            System.out.println("Abrindo: " + fxmlPath);
            
            // Localiza o arquivo FXML no classpath
            java.net.URL url = getClass().getResource(fxmlPath);

            // Verifica se o arquivo existe
            if (url == null) {
                System.out.println("ERRO: Arquivo não encontrado - " + fxmlPath);
                return;  // Interrompe se o arquivo não for encontrado
            }

            // Carrega o layout do arquivo FXML
            Parent root = FXMLLoader.load(url);
            
            // Cria uma nova janela
            Stage stage = new Stage();
            stage.setTitle(titulo);
            
            // Define a cena com o tamanho especificado
            stage.setScene(new Scene(root, width, height));
            
            // Define o tamanho mínimo que a janela pode ter (60% do tamanho inicial)
            // Isso evita que o usuário reduza demais e quebre o layout
            stage.setMinWidth(width * 0.6);
            stage.setMinHeight(height * 0.6);
            
            // Exibe a janela
            stage.show();
            
        } catch (Exception e) {
            // Captura e exibe qualquer erro durante a abertura
            System.out.println("Erro ao abrir " + titulo + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== MÉTODOS DAS TELAS ====================
    // Cada método abaixo chama o método centralizado com os parâmetros específicos
    
    /**
     * Abre a tela de cadastro de Clientes
     * Tamanho: 1000x700 pixels
     */
    private void abrirTelaClientes() {
        abrirTela("/com/eletronica/view/FrmCliente.fxml", "Cadastro de Clientes", 1000, 700);
    }

    /**
     * Abre a tela de cadastro de Produtos
     * Tamanho: 900x600 pixels
     */
    private void abrirTelaProdutos() {
        abrirTela("/com/eletronica/view/FrmProduto.fxml", "Cadastro de Produtos", 900, 600);
    }

    /**
     * Abre a tela de consulta de Serviços
     * Tamanho: 800x600 pixels
     */
    private void abrirTelaServicos() {
        abrirTela("/com/eletronica/view/FrmServico.fxml", "Consulta de Serviços", 800, 600);
    }

    /**
     * Abre a tela de Ordem de Serviço (criação e gerenciamento)
     * Tamanho: 1200x800 pixels (maior por ser a tela mais complexa)
     */
    private void abrirTelaOrdens() {
        abrirTela("/com/eletronica/view/FrmOrdemServico.fxml", "Ordens de Serviço", 1200, 800);
    }

    /**
     * Abre a tela de cadastro de Usuários
     * ANTES de abrir, verifica se o usuário logado tem permissão
     * Se não tiver, exibe um alerta e não abre a tela
     * Tamanho: 900x600 pixels
     */
    private void abrirTelaUsuarios() {
        // Verificação de permissão (segurança em camadas)
        if (!UsuarioSessao.podeManterUsuario()) {
            System.out.println("Acesso negado: usuário não tem permissão!");
            
            // Exibe alerta informativo para o usuário
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso Negado");
            alert.setHeaderText(null);  // Sem cabeçalho
            alert.setContentText("Você não tem permissão para acessar esta funcionalidade!");
            alert.showAndWait();  // Aguarda o usuário clicar em OK
            return;  // Não abre a tela
        }
        abrirTela("/com/eletronica/view/FrmUsuario.fxml", "Cadastro de Usuários", 900, 600);
    }

    /**
     * Abre a tela de cadastro de Grupos de Usuários
     * ANTES de abrir, verifica se o usuário logado tem permissão
     * Se não tiver, exibe um alerta e não abre a tela
     * Tamanho: 1000x700 pixels
     */
    private void abrirTelaGrupoUsuarios() {
        // Verificação de permissão (segurança em camadas)
        if (!UsuarioSessao.podeManterUsuario()) {
            System.out.println("Acesso negado: usuário não tem permissão!");
            
            // Exibe alerta informativo para o usuário
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso Negado");
            alert.setHeaderText(null);
            alert.setContentText("Você não tem permissão para acessar esta funcionalidade!");
            alert.showAndWait();
            return;  // Não abre a tela
        }
        abrirTela("/com/eletronica/view/FrmGrupoUsuario.fxml", "Cadastro de Grupos de Usuários", 1000, 700);
    }

    /**
     * MÉTODO SAIR
     * 
     * Responsável por encerrar a sessão do usuário e fechar o sistema.
     * Antes de sair, pede confirmação para evitar saídas acidentais.
     * 
     * Este método está vinculado ao botão "Sair" no FXML (onAction="#sair")
     */
    @FXML
    private void sair() {
        // Cria um diálogo de confirmação
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sair");
        confirm.setHeaderText("Deseja realmente sair?");
        confirm.setContentText("Você será desconectado do sistema.");
        
        // Aguarda a resposta do usuário
        // Se clicar em OK (ButtonType.OK), sai do sistema
        if (confirm.showAndWait().get() == ButtonType.OK) {
            UsuarioSessao.logout();  // Limpa a sessão (importante para segurança)
            System.exit(0);          // Encerra a aplicação completamente
        }
        // Se clicar em Cancelar, não faz nada (apenas fecha o diálogo)
    }
}
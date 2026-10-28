/**
 * CLASSE PRINCIPAL - PONTO DE ENTRADA DO SISTEMA
 * 
 * Esta classe é responsável por iniciar a aplicação JavaFX.
 * Ela carrega a primeira tela (Login) e define as configurações iniciais da janela.
 * 
 * @author joao
 */
package com.eletronica;


import javafx.application.Application;  // Classe base para aplicações JavaFX
import javafx.fxml.FXMLLoader;          // Carrega arquivos FXML (interface gráfica)
import javafx.scene.Parent;             // Nó base para a cena (Scene)
import javafx.scene.Scene;              // Representa a cena/interface do JavaFX
import javafx.stage.Stage;              // Janela principal da aplicação

public class App extends Application {
   
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // Converte para BCrypt senhas legadas ainda gravadas em texto puro (idempotente)
        migrarSenhasLegadas();
        
        // Carrega o arquivo FXML da tela de Login
        // O arquivo está localizado em: src/main/resources/com/eletronica/view/FrmLogin.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmLogin.fxml"));
        
        primaryStage.setTitle("Login - Sistema Eletrônica");
        
        // Cria uma nova cena com o conteúdo do FXML e define tamanho 550x480 pixels
        Scene scene = new Scene(root, 550, 480);
        com.eletronica.util.ThemeManager.registrarCena(scene);
        primaryStage.setScene(scene);
        
        // Impede que o usuário redimensione a janela (mantém tamanho fixo)
        // Isso é feito para manter o layout da tela de login consistente
        primaryStage.setResizable(false);
        
        // Exibe a janela na tela
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        com.eletronica.util.Database.closeConnection();
        super.stop();
    }
    
    /**
     * MIGRAÇÃO DE SEGURANÇA EXECUTADA NA INICIALIZAÇÃO
     * 
     * Garante que nenhuma senha permaneça em texto puro no banco.
     * Se o banco estiver indisponível, apenas registra o aviso: a tela de
     * login continuará exibindo o erro de conexão normalmente.
     */
    private void migrarSenhasLegadas() {
        try {
            int convertidas = new com.eletronica.dao.UsuarioDAO().migrarSenhasTextoPuro();
            if (convertidas > 0) {
                System.out.println("[Segurança] " + convertidas + " senha(s) em texto puro convertida(s) para BCrypt.");
            }
        } catch (Exception e) {
            System.err.println("[Segurança] Não foi possível verificar/migrar as senhas: " + e.getMessage());
        }
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}
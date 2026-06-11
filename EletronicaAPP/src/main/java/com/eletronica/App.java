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
        
        // Carrega o arquivo FXML da tela de Login
        // O arquivo está localizado em: src/main/resources/com/eletronica/view/FrmLogin.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/com/eletronica/view/FrmLogin.fxml"));
        
        primaryStage.setTitle("Login - Sistema Eletrônica");
        
        // Cria uma nova cena com o conteúdo do FXML e define tamanho 500x450 pixels
        primaryStage.setScene(new Scene(root, 500, 450));
        
        // Impede que o usuário redimensione a janela (mantém tamanho fixo)
        // Isso é feito para manter o layout da tela de login consistente
        primaryStage.setResizable(false);
        
        // Exibe a janela na tela
        primaryStage.show();
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}
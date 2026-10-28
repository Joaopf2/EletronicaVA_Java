package com.eletronica.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * GERENCIADOR CENTRAL DE JANELAS E NAVEGAÇÃO
 * 
 * Regras implementadas:
 * 1. Sempre que uma nova tela secundária for aberta, qualquer outra tela secundária anterior
 *    é imediatamente fechada, evitando acúmulo de janelas e perda de foco pelo usuário.
 * 2. Toda tela aberta ocupa toda a tela por padrão (stage.setMaximized(true)).
 * 3. O tema ativo (Escuro ou Claro) é registrado e sincronizado automaticamente.
 */
public class ScreenManager {

    private static Stage telaPrincipal = null;
    private static Stage telaSecundariaAberta = null;

    public static void setTelaPrincipal(Stage stage) {
        telaPrincipal = stage;
        if (stage != null && stage.getScene() != null) {
            ThemeManager.registrarCena(stage.getScene());
        }
    }

    public static Stage getTelaPrincipal() {
        return telaPrincipal;
    }

    public static Stage getTelaSecundariaAberta() {
        return telaSecundariaAberta;
    }

    /**
     * Fecha a janela secundária atualmente aberta, se houver
     */
    public static void fecharTelaSecundariaAberta() {
        if (telaSecundariaAberta != null && telaSecundariaAberta.isShowing()) {
            Stage anterior = telaSecundariaAberta;
            telaSecundariaAberta = null;
            anterior.close();
        }
    }

    /**
     * Abre uma tela secundária maximizada por padrão, fechando qualquer tela secundária anterior.
     * 
     * @param fxmlPath Caminho do recurso FXML (ex: "/com/eletronica/view/FrmCliente.fxml")
     * @param titulo Título da janela
     * @param onClosed Callback a ser executado quando a tela for fechada (ex: recarregar dashboard)
     * @return O Stage criado e exibido
     */
    public static Stage abrirTelaSecundaria(String fxmlPath, String titulo, Runnable onClosed) {
        try {
            // 1. Fecha qualquer tela secundária aberta para evitar acúmulo de janelas
            fecharTelaSecundariaAberta();

            URL url = ScreenManager.class.getResource(fxmlPath);
            if (url == null) {
                System.err.println("ERRO: FXML não encontrado: " + fxmlPath);
                return null;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = new Stage();
            stage.setTitle(titulo);

            Scene scene = new Scene(root, 1200, 800);
            ThemeManager.registrarCena(scene);
            stage.setScene(scene);

            // 2. Ocupa toda a tela por padrão conforme solicitado
            stage.setMaximized(true);

            stage.setOnHidden(e -> {
                ThemeManager.desregistrarCena(scene);
                if (telaSecundariaAberta == stage) {
                    telaSecundariaAberta = null;
                }
                if (onClosed != null) {
                    onClosed.run();
                }
            });

            telaSecundariaAberta = stage;
            stage.show();
            stage.toFront();
            return stage;

        } catch (Exception e) {
            System.err.println("Erro ao abrir janela " + titulo + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

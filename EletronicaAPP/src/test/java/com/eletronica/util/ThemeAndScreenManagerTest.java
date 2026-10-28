package com.eletronica.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ThemeAndScreenManagerTest {

    @BeforeAll
    static void initJavaFx() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
            });
        } catch (IllegalStateException alreadyStarted) {
            Platform.setImplicitExit(false);
        }
    }

    @Test
    void testStylesheetsExistemNoClasspath() {
        URL darkCss = getClass().getResource("/com/eletronica/view/style.css");
        assertNotNull(darkCss, "style.css (Dark Theme) deve existir nas resources");

        URL lightCss = getClass().getResource("/com/eletronica/view/style-light.css");
        assertNotNull(lightCss, "style-light.css (Light Theme) deve existir nas resources");
    }

    @Test
    void testAlternanciaDeTema() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicBoolean listenerChamado = new AtomicBoolean(false);
        AtomicBoolean temaRecebidoPeloListener = new AtomicBoolean(false);

        Platform.runLater(() -> {
            try {
                // Cria cena de teste
                StackPane root = new StackPane();
                Scene scene = new Scene(root, 400, 300);

                ThemeManager.ThemeChangeListener listener = isDark -> {
                    listenerChamado.set(true);
                    temaRecebidoPeloListener.set(isDark);
                };
                ThemeManager.addListener(listener);

                // 1. Inicia em Dark
                ThemeManager.setTemaDark(true);
                assertTrue(ThemeManager.isDark());
                ThemeManager.registrarCena(scene);

                boolean temDarkNoRoot = root.getStylesheets().stream().anyMatch(s -> s.endsWith("style.css"));
                boolean temDarkNaScene = scene.getStylesheets().stream().anyMatch(s -> s.endsWith("style.css"));
                assertTrue(temDarkNoRoot || temDarkNaScene, "Deve conter style.css no tema escuro");

                // 2. Alterna para Light
                ThemeManager.setTemaDark(false);
                assertFalse(ThemeManager.isDark(), "Tema deve ser claro agora");
                assertTrue(listenerChamado.get(), "Listener deve ter sido notificado");
                assertFalse(temaRecebidoPeloListener.get(), "Listener deve ter recebido false (light)");

                boolean temLightNoRoot = root.getStylesheets().stream().anyMatch(s -> s.endsWith("style-light.css"));
                boolean temLightNaScene = scene.getStylesheets().stream().anyMatch(s -> s.endsWith("style-light.css"));
                assertTrue(temLightNoRoot || temLightNaScene, "Deve conter style-light.css no tema claro");

                boolean temDarkAinda = root.getStylesheets().stream().anyMatch(s -> s.endsWith("style.css"))
                        || scene.getStylesheets().stream().anyMatch(s -> s.endsWith("style.css"));
                assertFalse(temDarkAinda, "style.css antigo não deve permanecer na cena ao mudar para light");

                // 3. Limpa e remove listener
                ThemeManager.desregistrarCena(scene);
                ThemeManager.removeListener(listener);
                ThemeManager.setTemaDark(true); // Restaura default escuro
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout ao executar teste de tema");
        if (errorRef.get() != null) {
            fail("Erro no teste de tema: " + errorRef.get().getMessage());
        }
    }

    @Test
    void testScreenManagerControleDeJanelas() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Stage stagePrincipal = new Stage();
                ScreenManager.setTelaPrincipal(stagePrincipal);
                assertEquals(stagePrincipal, ScreenManager.getTelaPrincipal());

                // Abre tela secundária A
                AtomicBoolean callbackExecutado = new AtomicBoolean(false);
                Stage subStageA = ScreenManager.abrirTelaSecundaria(
                        "/com/eletronica/view/FrmCliente.fxml",
                        "Tela Teste A",
                        () -> callbackExecutado.set(true)
                );

                assertNotNull(subStageA, "Stage A não deve ser nulo");
                assertEquals(subStageA, ScreenManager.getTelaSecundariaAberta(), "Stage A deve ser a janela secundária ativa");
                assertTrue(subStageA.isMaximized(), "Janela secundária deve abrir maximizada por padrão");

                // Abre tela secundária B -> A deve fechar automaticamente
                Stage subStageB = ScreenManager.abrirTelaSecundaria(
                        "/com/eletronica/view/FrmProduto.fxml",
                        "Tela Teste B",
                        null
                );

                assertNotNull(subStageB, "Stage B não deve ser nulo");
                assertEquals(subStageB, ScreenManager.getTelaSecundariaAberta(), "Stage B deve ser a nova janela secundária ativa");
                assertFalse(subStageA.isShowing(), "Janela anterior A deve ter sido fechada automaticamente");
                assertTrue(callbackExecutado.get(), "Callback de fechamento de A deve ter sido executado");
                assertTrue(subStageB.isMaximized(), "Janela B deve abrir maximizada por padrão");

                // Fecha B explicitamente
                ScreenManager.fecharTelaSecundariaAberta();
                assertNull(ScreenManager.getTelaSecundariaAberta(), "Nenhuma janela secundária deve estar aberta");
                assertFalse(subStageB.isShowing(), "Janela B deve estar fechada");

            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout no teste de ScreenManager");
        if (errorRef.get() != null) {
            fail("Erro no teste de ScreenManager: " + errorRef.get().getMessage());
        }
    }
}

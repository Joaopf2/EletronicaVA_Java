package com.eletronica.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validação de integridade dos arquivos FXML.
 * Garante que todos os componentes, fx:id e estilos carregam perfeitamente sem erros de sintaxe ou vinculação.
 */
class FxmlLoadTest {

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

    @ParameterizedTest
    @ValueSource(strings = {
        "/com/eletronica/view/FrmLogin.fxml",
        "/com/eletronica/view/FrmMenuPrincipal.fxml",
        "/com/eletronica/view/FrmOrdemServico.fxml",
        "/com/eletronica/view/FrmCliente.fxml",
        "/com/eletronica/view/FrmProduto.fxml",
        "/com/eletronica/view/FrmServico.fxml",
        "/com/eletronica/view/FrmUsuario.fxml",
        "/com/eletronica/view/FrmGrupoUsuario.fxml",
        "/com/eletronica/view/FrmGerarOS.fxml",
        "/com/eletronica/view/FrmDetalhesOS.fxml"
    })
    void testCarregarFxml(String fxmlPath) throws Exception {
        URL resource = getClass().getResource(fxmlPath);
        assertNotNull(resource, "Arquivo FXML não encontrado no classpath: " + fxmlPath);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicReference<Parent> rootRef = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                Parent root = FXMLLoader.load(resource);
                rootRef.set(root);
            } catch (Throwable t) {
                errorRef.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Timeout ao carregar " + fxmlPath);
        if (errorRef.get() != null) {
            errorRef.get().printStackTrace();
            fail("Erro ao carregar FXML " + fxmlPath + ": " + errorRef.get().getMessage());
        }
        assertNotNull(rootRef.get(), "Nó raiz FXML nulo para " + fxmlPath);
    }
}

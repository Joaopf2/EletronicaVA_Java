package com.eletronica.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GERENCIADOR DE TEMAS (DARK / LIGHT)
 * 
 * Permite alternar dinamicamente entre Tema Escuro e Tema Claro em tempo real,
 * propagando as alterações para todas as janelas e cenas ativas da aplicação.
 */
public class ThemeManager {

    private static boolean darkTheme = true;
    private static final List<Scene> cenasAtivas = new ArrayList<>();
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();

    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDark);
    }

    public static boolean isDark() {
        return darkTheme;
    }

    public static void registrarCena(Scene scene) {
        if (scene != null && !cenasAtivas.contains(scene)) {
            cenasAtivas.add(scene);
        }
        aplicarTema(scene);
    }

    public static void desregistrarCena(Scene scene) {
        if (scene != null) {
            cenasAtivas.remove(scene);
        }
    }

    public static void addListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(ThemeChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public static void alternarTema() {
        setTemaDark(!darkTheme);
    }

    public static void setTemaDark(boolean dark) {
        darkTheme = dark;
        for (Iterator<Scene> it = cenasAtivas.iterator(); it.hasNext(); ) {
            Scene scene = it.next();
            if (scene.getWindow() == null || scene.getWindow().isShowing()) {
                aplicarTema(scene);
            }
        }
        for (ThemeChangeListener l : listeners) {
            try {
                l.onThemeChanged(darkTheme);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Aplica o tema ativo à cena especificada
     */
    public static void aplicarTema(Scene scene) {
        if (scene == null) return;

        String darkCss = ThemeManager.class.getResource("/com/eletronica/view/style.css") != null
                ? ThemeManager.class.getResource("/com/eletronica/view/style.css").toExternalForm() : null;
        String lightCss = ThemeManager.class.getResource("/com/eletronica/view/style-light.css") != null
                ? ThemeManager.class.getResource("/com/eletronica/view/style-light.css").toExternalForm() : null;

        String cssDesejado = darkTheme ? darkCss : lightCss;
        if (cssDesejado == null) return;

        Parent root = scene.getRoot();
        if (root != null) {
            root.getStylesheets().removeIf(s -> s.endsWith("style.css") || s.endsWith("style-light.css"));
            if (!root.getStylesheets().contains(cssDesejado)) {
                root.getStylesheets().add(cssDesejado);
            }
        }

        scene.getStylesheets().removeIf(s -> s.endsWith("style.css") || s.endsWith("style-light.css"));
        if (!scene.getStylesheets().contains(cssDesejado)) {
            scene.getStylesheets().add(cssDesejado);
        }
    }
}

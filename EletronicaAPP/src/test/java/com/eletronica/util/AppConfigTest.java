package com.eletronica.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AppConfigTest {

    @Test
    @DisplayName("Deve carregar propriedades do arquivo de configuração")
    void deveCarregarPropriedades() {
        String dbUrl = AppConfig.get("db.url");
        assertNotNull(dbUrl, "db.url não deve ser nulo");
        assertTrue(dbUrl.startsWith("jdbc:postgresql://"), "db.url deve iniciar com jdbc:postgresql://");

        String dbUser = AppConfig.get("db.user");
        assertNotNull(dbUser, "db.user não deve ser nulo");
        assertEquals("postgres", dbUser);
    }

    @Test
    @DisplayName("Deve respeitar valores padrão quando chave não existir")
    void deveRetornarValorPadrao() {
        String valor = AppConfig.get("chave.inexistente", "padrao");
        assertEquals("padrao", valor);

        int numero = AppConfig.getInt("chave.inexistente.int", 42);
        assertEquals(42, numero);
    }

    @Test
    @DisplayName("Deve carregar senha do banco sem ser nula")
    void deveCarregarSenhaBanco() {
        String senha = AppConfig.getSenhaBanco();
        assertNotNull(senha, "getSenhaBanco() não deve retornar null");
    }
}

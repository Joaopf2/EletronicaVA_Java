package com.eletronica.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SenhaUtilTest {

    @Test
    @DisplayName("Deve gerar hash BCrypt válido com prefixo $2a$")
    void deveGerarHashBCryptValido() {
        String senha = "minhaSenhaSuperSecreta123";
        String hash = SenhaUtil.hash(senha);

        assertNotNull(hash);
        assertEquals(60, hash.length(), "Hash BCrypt deve possuir 60 caracteres");
        assertTrue(hash.startsWith("$2a$"), "Hash gerado deve iniciar com $2a$");
        assertTrue(SenhaUtil.isHash(hash));
        assertFalse(SenhaUtil.precisaRehash(hash));
    }

    @Test
    @DisplayName("Deve verificar corretamente a senha que bate com o hash")
    void deveVerificarSenhaCorreta() {
        String senha = "admin123";
        String hash = SenhaUtil.hash(senha);

        assertTrue(SenhaUtil.verificar("admin123", hash));
        assertFalse(SenhaUtil.verificar("senhaErrada", hash));
        assertFalse(SenhaUtil.verificar("", hash));
        assertFalse(SenhaUtil.verificar(null, hash));
    }

    @Test
    @DisplayName("Deve aceitar senha legada em texto puro e sinalizar necessidade de rehash")
    void deveVerificarSenhaLegadaTextoPuro() {
        String senhaLegada = "senhaPuraAntiga";

        assertFalse(SenhaUtil.isHash(senhaLegada));
        assertTrue(SenhaUtil.precisaRehash(senhaLegada));

        // Deve autenticar normalmente mesmo em texto puro
        assertTrue(SenhaUtil.verificar("senhaPuraAntiga", senhaLegada));
        assertFalse(SenhaUtil.verificar("outraSenha", senhaLegada));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar hash de senha vazia ou nula")
    void deveLancarExcecaoSenhaVazia() {
        assertThrows(IllegalArgumentException.class, () -> SenhaUtil.hash(""));
        assertThrows(IllegalArgumentException.class, () -> SenhaUtil.hash(null));
    }
}

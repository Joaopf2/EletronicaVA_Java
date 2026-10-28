package com.eletronica.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    @AfterAll
    static void tearDown() {
        Database.closeConnection();
    }

    @Test
    @DisplayName("Deve obter conexão ativa do pool HikariCP e devolver ao fechar")
    void deveObterEDevolverConexaoDoPool() throws SQLException {
        try (Connection conn = Database.getConnection()) {
            assertNotNull(conn, "A conexão não deve ser nula");
            assertFalse(conn.isClosed(), "A conexão deve estar aberta");
            assertTrue(conn.isValid(2), "A conexão deve ser válida");
        }
        // Ao sair do bloco try, a conexão foi devolvida ao pool,
        // mas o pool continua vivo e pode entregar outra conexão
        try (Connection conn2 = Database.getConnection()) {
            assertNotNull(conn2);
            assertFalse(conn2.isClosed());
        }
    }
}

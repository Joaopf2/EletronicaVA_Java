package com.eletronica.dao;

import com.eletronica.model.UsuarioEntity;
import com.eletronica.util.Database;
import com.eletronica.util.SenhaUtil;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioDAOTest {

    private static UsuarioDAO usuarioDAO;

    @BeforeAll
    static void setup() {
        usuarioDAO = new UsuarioDAO();
    }

    @AfterAll
    static void tearDown() {
        Database.closeConnection();
    }

    @Test
    @Order(1)
    @DisplayName("Deve executar a migração de senhas em texto puro para BCrypt com sucesso")
    void deveMigrarSenhasEmTextoPuro() throws SQLException {
        int migradas = usuarioDAO.migrarSenhasTextoPuro();
        System.out.println("Senhas migradas no teste: " + migradas);

        // Se executada novamente, nenhuma senha deve precisar de migração (idempotente)
        int reexecucao = usuarioDAO.migrarSenhasTextoPuro();
        assertEquals(0, reexecucao, "Segunda execução da migração deve retornar 0 senhas a migrar");

        // Verifica no banco se todas as senhas cadastradas agora são hashes BCrypt válidos
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT senha FROM usuarios");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String senhaBanco = rs.getString("senha");
                assertTrue(SenhaUtil.isHash(senhaBanco), "Todas as senhas no banco devem ser hash BCrypt: " + senhaBanco);
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("Deve autenticar o usuário Administrador com a senha correta (agora em BCrypt)")
    void deveAutenticarUsuarioComSucesso() throws SQLException {
        UsuarioEntity admin = usuarioDAO.autenticar("admin@eletronica.com", "admin123");
        assertNotNull(admin, "Administrador deve ser autenticado com sucesso");
        assertEquals("admin@eletronica.com", admin.getEmail());
        assertNotNull(admin.getGrupo(), "Grupo do usuário deve ser carregado");
        assertNull(admin.getSenha(), "Hash da senha não deve ser mantido em memória após autenticação");

        // Autenticação com senha errada deve falhar retornando null
        UsuarioEntity falha = usuarioDAO.autenticar("admin@eletronica.com", "senhaIncorreta");
        assertNull(falha, "Autenticação com senha incorreta deve retornar null");

        // Autenticação com email inexistente deve falhar retornando null
        UsuarioEntity inex = usuarioDAO.autenticar("inexistente@eletronica.com", "1234");
        assertNull(inex, "Autenticação com email inexistente deve retornar null");
    }

    @Test
    @Order(3)
    @DisplayName("Deve inserir novo usuário com senha em hash BCrypt, atualizar sem trocar senha e depois deletar")
    void deveFazerCrudUsuarioComHash() throws SQLException {
        String emailTeste = "teste_seguranca_" + System.currentTimeMillis() + "@eletronica.com";
        String senhaPura = "Segredo123!";

        UsuarioEntity novo = new UsuarioEntity();
        novo.setNome("Usuario Teste Seguranca");
        novo.setEmail(emailTeste);
        novo.setSenha(senhaPura);
        novo.setIdGrupoUsuario(1); // Administrador

        usuarioDAO.inserir(novo);
        assertTrue(novo.getId() > 0, "ID deve ser gerado pelo banco");

        try {
            // Verifica no banco se a senha foi salva diretamente como hash BCrypt
            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT senha FROM usuarios WHERE id = ?")) {
                stmt.setInt(1, novo.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    String hashNoBanco = rs.getString("senha");
                    assertNotEquals(senhaPura, hashNoBanco, "Senha gravada não pode ser texto puro");
                    assertTrue(SenhaUtil.isHash(hashNoBanco), "Senha gravada deve ser um hash BCrypt");
                }
            }

            // Testa autenticação com o novo usuário
            UsuarioEntity autenticado = usuarioDAO.autenticar(emailTeste, senhaPura);
            assertNotNull(autenticado, "Novo usuário deve autenticar com a senha original");

            // Atualiza usuário mantendo a senha (senha = null ou vazia)
            autenticado.setNome("Usuario Teste Alterado");
            autenticado.setSenha(""); // Sem alteração de senha
            usuarioDAO.atualizar(autenticado);

            // Confirma que a senha continuou funcionando
            UsuarioEntity autenticadoAposUpdate = usuarioDAO.autenticar(emailTeste, senhaPura);
            assertNotNull(autenticadoAposUpdate, "Senha deve continuar válida após atualização sem troca de senha");
            assertEquals("Usuario Teste Alterado", autenticadoAposUpdate.getNome());

        } finally {
            // Limpa o usuário de teste
            usuarioDAO.deletar(novo.getId());
            assertNull(usuarioDAO.buscarPorId(novo.getId()), "Usuário deve ser removido após teste");
        }
    }
}

package com.eletronica.dao;

import com.eletronica.model.*;
import com.eletronica.util.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OutrosDAOsTest {

    @AfterAll
    static void tearDown() {
        Database.closeConnection();
    }

    @Test
    @DisplayName("Deve listar grupos de usuários via GrupoUsuarioDAO")
    void deveListarGrupos() throws SQLException {
        GrupoUsuarioDAO dao = new GrupoUsuarioDAO();
        List<GrupoUsuarioEntity> grupos = dao.listarTodos();
        assertNotNull(grupos);
        assertFalse(grupos.isEmpty(), "Deve conter grupos cadastrados (ex: Administrador)");
    }

    @Test
    @DisplayName("Deve realizar operações CRUD no ClienteDAO com sucesso")
    void deveOperarClienteDAO() throws SQLException {
        ClienteDAO dao = new ClienteDAO();
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste Unitario");
        cliente.setEmail("cliente.teste@exemplo.com");
        cliente.setTelefone("(11) 99999-9999");
        cliente.setCnpjCpf("123.456.789-00");
        cliente.setRg("12.345.678-9");
        cliente.setIe("ISENTO");

        dao.inserir(cliente);
        assertTrue(cliente.getId() > 0);

        try {
            ClienteEntity buscado = dao.buscarPorId(cliente.getId());
            assertNotNull(buscado);
            assertEquals("Cliente Teste Unitario", buscado.getNome());

            buscado.setNome("Cliente Teste Atualizado");
            dao.atualizar(buscado);

            ClienteEntity atualizado = dao.buscarPorId(cliente.getId());
            assertEquals("Cliente Teste Atualizado", atualizado.getNome());
        } finally {
            dao.deletar(cliente.getId());
            assertNull(dao.buscarPorId(cliente.getId()));
        }
    }

    @Test
    @DisplayName("Deve realizar operações CRUD no ServicoDAO com sucesso")
    void deveOperarServicoDAO() throws SQLException {
        ServicoDAO dao = new ServicoDAO();
        ServicoEntity servico = new ServicoEntity("Troca de Conector de Carga");

        dao.inserir(servico);
        assertTrue(servico.getId() > 0);

        try {
            ServicoEntity buscado = dao.buscarPorId(servico.getId());
            assertNotNull(buscado);
            assertEquals("Troca de Conector de Carga", buscado.getDescricao());

            buscado.setDescricao("Troca de Conector Type-C");
            dao.atualizar(buscado);

            ServicoEntity atualizado = dao.buscarPorId(servico.getId());
            assertEquals("Troca de Conector Type-C", atualizado.getDescricao());
        } finally {
            dao.deletar(servico.getId());
            assertNull(dao.buscarPorId(servico.getId()));
        }
    }

    @Test
    @DisplayName("Deve realizar operações CRUD no ProdutoDAO e OrdemServicoDAO com sucesso")
    void deveOperarProdutoEOrdemServicoDAO() throws SQLException {
        ClienteDAO clienteDAO = new ClienteDAO();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        OrdemServicoDAO osDAO = new OrdemServicoDAO();

        // 1. Cria cliente
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Para OS Teste");
        cliente.setEmail("cliente.os@exemplo.com");
        cliente.setTelefone("(11) 98888-8888");
        cliente.setCnpjCpf("987.654.321-00");
        cliente.setRg("98.765.432-1");
        clienteDAO.inserir(cliente);

        // 2. Cria produto
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Smartphone Teste");
        produto.setTipo("Celular");
        produto.setMarca("Samsung");
        produto.setModelo("Galaxy A54");
        produto.setCategoria("Smartphones");
        produto.setDefeito("Tela trincada");
        produtoDAO.inserir(produto);

        // 3. Cria OS
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setDescricao("Trocar tela e bateria");
        os.setOrcamento("350.00");
        os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
        os.setIdCliente(cliente.getId());
        os.setIdProduto(produto.getId());
        osDAO.inserir(os);
        assertTrue(os.getId() > 0);

        try {
            OrdemServicoEntity osBuscada = osDAO.buscarPorId(os.getId());
            assertNotNull(osBuscada);
            assertEquals("Trocar tela e bateria", osBuscada.getDescricao());
            assertEquals("Cliente Para OS Teste", osBuscada.getNomeCliente());
            assertEquals("Smartphone Teste", osBuscada.getNomeProduto());

            // Atualiza status
            osDAO.atualizarStatus(os.getId(), OrdemServicoEntity.TipoStatus.PRONTO);
            OrdemServicoEntity osAtualizada = osDAO.buscarPorId(os.getId());
            assertEquals(OrdemServicoEntity.TipoStatus.PRONTO, osAtualizada.getStatus());

        } finally {
            // Remove na ordem correta devido às foreign keys
            osDAO.deletar(os.getId());
            produtoDAO.deletar(produto.getId());
            clienteDAO.deletar(cliente.getId());
        }
    }
}

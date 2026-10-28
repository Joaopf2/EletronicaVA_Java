package com.eletronica.dao;

import com.eletronica.model.ClienteEntity;
import com.eletronica.model.OrdemServicoEntity;
import com.eletronica.model.ProdutoEntity;
import com.eletronica.util.Database;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração do fluxo de Ordens de Serviço:
 * - UC05: Gerar Ordem de Serviço (Abertura / Triagem)
 * - Consulta e Detalhes da Ordem de Serviço (Acompanhamento e Encerramento)
 */
public class OrdemServicoFluxoTest {

    private ClienteDAO clienteDAO;
    private ProdutoDAO produtoDAO;
    private OrdemServicoDAO osDAO;

    @BeforeEach
    void setUp() {
        clienteDAO = new ClienteDAO();
        produtoDAO = new ProdutoDAO();
        osDAO = new OrdemServicoDAO();
    }

    @AfterAll
    static void tearDown() {
        Database.closeConnection();
    }

    @Test
    @DisplayName("UC05: Deve gerar ordem de serviço inicial vinculando cliente, produto e status EM_ESPERA")
    void testUC05GerarOrdemServico() throws SQLException {
        // 1. Cliente (UC03)
        ClienteEntity cli = new ClienteEntity();
        cli.setNome("Carlos Silva - Fluxo UC05");
        cli.setEmail("carlos.uc05@teste.com");
        cli.setTelefone("(11) 97777-1111");
        cli.setCnpjCpf("111.222.333-44");
        cli.setRg("11.222.333-4");
        cli.setIe("ISENTO");
        clienteDAO.inserir(cli);
        assertTrue(cli.getId() > 0);

        // 2. Produto/Equipamento (UC04)
        ProdutoEntity prod = new ProdutoEntity();
        prod.setNome("Notebook Dell Vostro");
        prod.setTipo("Notebook");
        prod.setMarca("Dell");
        prod.setModelo("Vostro 3510");
        prod.setCategoria("Computadores");
        prod.setDefeito("Não liga, led piscando");
        produtoDAO.inserir(prod);
        assertTrue(prod.getId() > 0);

        // 3. Gerar OS (UC05)
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setIdCliente(cli.getId());
        os.setIdProduto(prod.getId());
        os.setData(LocalDate.now());
        os.setOrcamento("280.00");
        os.setDescricao("Diagnóstico inicial: curto na linha de 19V");
        os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);

        osDAO.inserir(os);
        assertTrue(os.getId() > 0, "OS deve ter sido gerada com ID positivo");

        try {
            OrdemServicoEntity cadastrada = osDAO.buscarPorId(os.getId());
            assertNotNull(cadastrada);
            assertEquals("EM_ESPERA", cadastrada.getStatus().name());
            assertEquals("Carlos Silva - Fluxo UC05", cadastrada.getNomeCliente());
            assertEquals("Notebook Dell Vostro", cadastrada.getNomeProduto());
            assertEquals("280.00", cadastrada.getOrcamento());
        } finally {
            osDAO.deletar(os.getId());
            produtoDAO.deletar(prod.getId());
            clienteDAO.deletar(cli.getId());
        }
    }

    @Test
    @DisplayName("Consulta/Detalhes: Deve consultar ficha da OS e permitir ciclo completo de transição de status")
    void testCicloStatusEDetalhesOS() throws SQLException {
        // 1. Prepara dados
        ClienteEntity cli = new ClienteEntity();
        cli.setNome("Mariana Souza - Ciclo OS");
        cli.setEmail("mariana.ciclo@teste.com");
        cli.setTelefone("(11) 96666-2222");
        cli.setCnpjCpf("222.333.444-55");
        cli.setRg("22.333.444-5");
        cli.setIe("ISENTO");
        clienteDAO.inserir(cli);

        ProdutoEntity prod = new ProdutoEntity();
        prod.setNome("Smart TV LG 50");
        prod.setTipo("Televisor");
        prod.setMarca("LG");
        prod.setModelo("UR7800");
        prod.setDefeito("Sem imagem, som normal");
        produtoDAO.inserir(prod);

        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setIdCliente(cli.getId());
        os.setIdProduto(prod.getId());
        os.setData(LocalDate.now());
        os.setOrcamento("450.00");
        os.setDescricao("Troca de barra de LEDs backlight");
        os.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
        osDAO.inserir(os);

        try {
            // Consulta detalhes
            OrdemServicoEntity detalhe = osDAO.buscarPorId(os.getId());
            assertNotNull(detalhe);
            assertEquals(OrdemServicoEntity.TipoStatus.EM_ESPERA, detalhe.getStatus());

            // Transição 1: Bancada / Em Andamento
            osDAO.atualizarStatus(os.getId(), OrdemServicoEntity.TipoStatus.EM_ANDAMENTO);
            OrdemServicoEntity andamento = osDAO.buscarPorId(os.getId());
            assertEquals(OrdemServicoEntity.TipoStatus.EM_ANDAMENTO, andamento.getStatus());

            // Transição 2: Conclusão / Pronto para entrega
            osDAO.atualizarStatus(os.getId(), OrdemServicoEntity.TipoStatus.PRONTO);
            OrdemServicoEntity pronto = osDAO.buscarPorId(os.getId());
            assertEquals(OrdemServicoEntity.TipoStatus.PRONTO, pronto.getStatus());

            // Atualização de detalhes adicionais (orçamento final e diagnóstico completo)
            pronto.setOrcamento("480.00");
            pronto.setDescricao("Troca de barra de LEDs e revisão da fonte concluída com sucesso.");
            osDAO.atualizar(pronto);

            OrdemServicoEntity atualizada = osDAO.buscarPorId(os.getId());
            assertEquals("480.00", atualizada.getOrcamento());
            assertTrue(atualizada.getDescricao().contains("revisão da fonte"));

        } finally {
            osDAO.deletar(os.getId());
            produtoDAO.deletar(prod.getId());
            clienteDAO.deletar(cli.getId());
        }
    }
}

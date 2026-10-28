package com.eletronica.controller;

import com.eletronica.dao.ClienteDAO;
import com.eletronica.dao.OrdemServicoDAO;
import com.eletronica.dao.ProdutoDAO;
import com.eletronica.model.ClienteEntity;
import com.eletronica.model.OrdemServicoEntity;
import com.eletronica.model.ProdutoEntity;
import com.eletronica.util.ScreenManager;
import com.eletronica.util.UsuarioSessao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CONTROLADOR PARA GERAR ORDEM DE SERVIÇO (UC05)
 * 
 * Tela focada exclusivamente no cadastro de um novo atendimento / triagem inicial,
 * sem tabelas redundantes, vinculando Cliente, Equipamento, Diagnóstico,
 * Orçamento e Usuário Responsável conforme os diagramas de caso de uso e de classe.
 */
public class GerarOSController {

    @FXML private ComboBox<ClienteEntity> cbCliente;
    @FXML private ComboBox<ProdutoEntity> cbProduto;
    @FXML private TextField txtOrcamento;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblDataAtual;
    @FXML private Label lblAtendente;
    @FXML private Label lblStatusPadrao;
    @FXML private TextArea txtResultado;

    @FXML private Button btnSalvar;
    @FXML private Button btnLimpar;
    @FXML private Button btnCancelar;
    @FXML private Button btnNovoCliente;
    @FXML private Button btnNovoProduto;

    private OrdemServicoDAO osDAO;
    private ClienteDAO clienteDAO;
    private ProdutoDAO produtoDAO;

    private ObservableList<ClienteEntity> listaClientes;
    private ObservableList<ProdutoEntity> listaProdutos;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        osDAO = new OrdemServicoDAO();
        clienteDAO = new ClienteDAO();
        produtoDAO = new ProdutoDAO();

        listaClientes = FXCollections.observableArrayList();
        listaProdutos = FXCollections.observableArrayList();

        // 1. Data do sistema e atendente
        if (lblDataAtual != null) {
            lblDataAtual.setText(LocalDate.now().format(formatter));
        }

        if (lblAtendente != null && UsuarioSessao.isLogado()) {
            lblAtendente.setText(UsuarioSessao.getUsuarioLogado().getNome() + " (" + UsuarioSessao.getGrupoLogado().getDescricao() + ")");
        }

        if (lblStatusPadrao != null) {
            lblStatusPadrao.setText("EM ESPERA (Triagem)");
        }

        // 2. Carregar dados de Clientes e Produtos
        carregarClientes();
        carregarProdutos();

        // 3. Ações dos botões
        if (btnSalvar != null) btnSalvar.setOnAction(e -> gerarOrdemServico());
        if (btnLimpar != null) btnLimpar.setOnAction(e -> limparFormulario());
        if (btnCancelar != null) btnCancelar.setOnAction(e -> fecharJanela());
        if (btnNovoCliente != null) btnNovoCliente.setOnAction(e -> abrirNovoCliente());
        if (btnNovoProduto != null) btnNovoProduto.setOnAction(e -> abrirNovoProduto());

        // 4. Permissões
        if (!UsuarioSessao.podeCriarOS()) {
            if (btnSalvar != null) btnSalvar.setDisable(true);
            if (txtResultado != null) {
                txtResultado.setText("Aviso: Seu perfil não possui permissão para gerar Ordens de Serviço!");
            }
        }
    }

    public void carregarClientes() {
        try {
            listaClientes.clear();
            List<ClienteEntity> clientes = clienteDAO.listarTodos();
            listaClientes.addAll(clientes);
            if (cbCliente != null) cbCliente.setItems(listaClientes);
        } catch (Exception e) {
            mostrarMensagem("Erro ao carregar clientes: " + e.getMessage(), true);
        }
    }

    public void carregarProdutos() {
        try {
            listaProdutos.clear();
            List<ProdutoEntity> produtos = produtoDAO.listarTodos();
            listaProdutos.addAll(produtos);
            if (cbProduto != null) cbProduto.setItems(listaProdutos);
        } catch (Exception e) {
            mostrarMensagem("Erro ao carregar equipamentos: " + e.getMessage(), true);
        }
    }

    private void gerarOrdemServico() {
        if (!UsuarioSessao.podeCriarOS()) {
            mostrarMensagem("Você não tem permissão para gerar OS!", true);
            return;
        }

        ClienteEntity cliente = cbCliente != null ? cbCliente.getValue() : null;
        ProdutoEntity produto = cbProduto != null ? cbProduto.getValue() : null;
        String descricao = txtDescricao != null ? txtDescricao.getText() : "";
        String orcamento = txtOrcamento != null ? txtOrcamento.getText() : "";

        // Validações
        if (cliente == null) {
            mostrarMensagem("Selecione um cliente para a ordem de serviço!", true);
            if (cbCliente != null) cbCliente.requestFocus();
            return;
        }

        if (produto == null) {
            mostrarMensagem("Selecione o equipamento/produto do cliente!", true);
            if (cbProduto != null) cbProduto.requestFocus();
            return;
        }

        if (descricao == null || descricao.trim().isEmpty()) {
            mostrarMensagem("Informe o defeito relatado ou diagnóstico do serviço!", true);
            if (txtDescricao != null) txtDescricao.requestFocus();
            return;
        }

        try {
            OrdemServicoEntity novaOS = new OrdemServicoEntity();
            novaOS.setIdCliente(cliente.getId());
            novaOS.setIdProduto(produto.getId());
            novaOS.setDescricao(descricao.trim());
            novaOS.setOrcamento(orcamento != null ? orcamento.trim() : "0.00");
            novaOS.setStatus(OrdemServicoEntity.TipoStatus.EM_ESPERA);
            novaOS.setData(LocalDate.now());

            osDAO.inserir(novaOS);

            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Ordem de Serviço Gerada");
            sucesso.setHeaderText("OS #" + novaOS.getId() + " cadastrada com sucesso!");
            sucesso.setContentText("Cliente: " + cliente.getNome() + "\nEquipamento: " + produto.getNome() + "\nStatus inicial: EM ESPERA");
            sucesso.showAndWait();

            fecharJanela();

        } catch (Exception e) {
            mostrarMensagem("Erro ao salvar OS no banco de dados: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void limparFormulario() {
        if (cbCliente != null) cbCliente.setValue(null);
        if (cbProduto != null) cbProduto.setValue(null);
        if (txtOrcamento != null) txtOrcamento.clear();
        if (txtDescricao != null) txtDescricao.clear();
        if (txtResultado != null) txtResultado.clear();
    }

    private void fecharJanela() {
        if (btnCancelar != null && btnCancelar.getScene() != null) {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        }
    }

    private void abrirNovoCliente() {
        abrirSubTela("/com/eletronica/view/FrmCliente.fxml", "Cadastro Rápido de Cliente");
    }

    private void abrirNovoProduto() {
        abrirSubTela("/com/eletronica/view/FrmProduto.fxml", "Cadastro Rápido de Equipamento");
    }

    private void abrirSubTela(String fxml, String titulo) {
        ScreenManager.abrirTelaSecundaria(fxml, titulo, () -> {
            carregarClientes();
            carregarProdutos();
        });
    }

    private void mostrarMensagem(String msg, boolean erro) {
        if (txtResultado != null) {
            txtResultado.setText(msg);
        }
        if (erro) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validação");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }
}

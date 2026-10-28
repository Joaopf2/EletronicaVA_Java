package com.eletronica.controller;

import com.eletronica.dao.ClienteDAO;
import com.eletronica.dao.OrdemServicoDAO;
import com.eletronica.dao.ProdutoDAO;
import com.eletronica.model.ClienteEntity;
import com.eletronica.model.OrdemServicoEntity;
import com.eletronica.model.ProdutoEntity;
import com.eletronica.util.UsuarioSessao;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

/**
 * CONTROLADOR DA CONSULTA E DETALHES DA ORDEM DE SERVIÇO
 * 
 * Tela acionada pelo botão de consulta (Detalhes 🔍 no Preview do Dashboard).
 * Exibe a ficha completa do atendimento, cliente, equipamento, orçamento e diagnóstico,
 * permitindo atualização de status e detalhes por usuários autorizados.
 */
public class DetalhesOSController {

    // Componentes de Cabeçalho
    @FXML private Label lblNumeroOS;
    @FXML private Label lblDataAbertura;
    @FXML private Label lblStatusBadge;
    @FXML private Label lblResponsavel;

    // Componentes de Cliente
    @FXML private Label lblClienteNome;
    @FXML private Label lblClienteCpf;
    @FXML private Label lblClienteTelefone;
    @FXML private Label lblClienteEmail;

    // Componentes de Equipamento
    @FXML private Label lblProdutoNome;
    @FXML private Label lblProdutoMarcaModelo;
    @FXML private Label lblProdutoTipoCategoria;
    @FXML private Label lblProdutoDefeitoOriginal;

    // Edição e Reparo
    @FXML private TextArea txtDescricaoReparo;
    @FXML private TextField txtOrcamento;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextArea txtResultado;

    // Botões
    @FXML private Button btnSalvarAlteracoes;
    @FXML private Button btnAvancarStatus;
    @FXML private Button btnExcluir;
    @FXML private Button btnFechar;

    private static OrdemServicoEntity ordemSelecionadaEstatica;
    private OrdemServicoEntity ordemAtual;

    private OrdemServicoDAO osDAO;
    private ClienteDAO clienteDAO;
    private ProdutoDAO produtoDAO;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void setOrdemParaExibir(OrdemServicoEntity os) {
        ordemSelecionadaEstatica = os;
    }

    @FXML
    public void initialize() {
        osDAO = new OrdemServicoDAO();
        clienteDAO = new ClienteDAO();
        produtoDAO = new ProdutoDAO();

        if (cbStatus != null) {
            cbStatus.getItems().setAll("EM ESPERA", "EM ANDAMENTO", "PRONTO");
        }

        // Configuração de eventos
        if (btnSalvarAlteracoes != null) btnSalvarAlteracoes.setOnAction(e -> salvarAlteracoes());
        if (btnAvancarStatus != null) btnAvancarStatus.setOnAction(e -> avancarStatus());
        if (btnExcluir != null) btnExcluir.setOnAction(e -> excluirOS());
        if (btnFechar != null) btnFechar.setOnAction(e -> fecharJanela());

        // Permissões
        if (!UsuarioSessao.podeEditarOS()) {
            if (btnSalvarAlteracoes != null) btnSalvarAlteracoes.setDisable(true);
            if (btnAvancarStatus != null) btnAvancarStatus.setDisable(true);
            if (txtDescricaoReparo != null) txtDescricaoReparo.setEditable(false);
            if (txtOrcamento != null) txtOrcamento.setEditable(false);
            if (cbStatus != null) cbStatus.setDisable(true);
        }

        if (!UsuarioSessao.podeDeletarOS()) {
            if (btnExcluir != null) {
                btnExcluir.setVisible(false);
                btnExcluir.setManaged(false);
            }
        }

        // Carregar a ordem selecionada
        if (ordemSelecionadaEstatica != null) {
            carregarDadosOrdem(ordemSelecionadaEstatica);
        }
    }

    public void carregarDadosOrdem(OrdemServicoEntity os) {
        this.ordemAtual = os;
        if (os == null) return;

        // Cabeçalho
        if (lblNumeroOS != null) lblNumeroOS.setText("Ordem de Serviço #" + os.getId());
        if (lblDataAbertura != null && os.getData() != null) {
            lblDataAbertura.setText("Data de Abertura: " + os.getData().format(formatter));
        }

        atualizarBadgeStatus(os.getStatus());

        if (lblResponsavel != null) {
            lblResponsavel.setText(UsuarioSessao.isLogado() ? UsuarioSessao.getUsuarioLogado().getNome() : "Técnico Responsável");
        }

        // Campos de edição
        if (txtDescricaoReparo != null) txtDescricaoReparo.setText(os.getDescricao() != null ? os.getDescricao() : "");
        if (txtOrcamento != null) txtOrcamento.setText(os.getOrcamento() != null ? os.getOrcamento() : "0.00");
        if (cbStatus != null && os.getStatus() != null) {
            cbStatus.setValue(os.getStatusString());
        }

        // Buscar dados completos do Cliente
        try {
            ClienteEntity c = clienteDAO.buscarPorId(os.getIdCliente());
            if (c != null) {
                if (lblClienteNome != null) lblClienteNome.setText(c.getNome());
                if (lblClienteCpf != null) lblClienteCpf.setText(c.getCnpjCpf() != null ? c.getCnpjCpf() : "Não informado");
                if (lblClienteTelefone != null) lblClienteTelefone.setText(c.getTelefone() != null ? c.getTelefone() : "Não informado");
                if (lblClienteEmail != null) lblClienteEmail.setText(c.getEmail() != null ? c.getEmail() : "Não informado");
            } else {
                if (lblClienteNome != null) lblClienteNome.setText(os.getNomeCliente() != null ? os.getNomeCliente() : "Cliente ID " + os.getIdCliente());
            }
        } catch (Exception e) {
            if (lblClienteNome != null) lblClienteNome.setText(os.getNomeCliente());
        }

        // Buscar dados completos do Produto
        try {
            ProdutoEntity p = produtoDAO.buscarPorId(os.getIdProduto());
            if (p != null) {
                if (lblProdutoNome != null) lblProdutoNome.setText(p.getNome());
                if (lblProdutoMarcaModelo != null) lblProdutoMarcaModelo.setText((p.getMarca() != null ? p.getMarca() : "") + " - " + (p.getModelo() != null ? p.getModelo() : ""));
                if (lblProdutoTipoCategoria != null) lblProdutoTipoCategoria.setText((p.getTipo() != null ? p.getTipo() : "") + " (" + (p.getCategoria() != null ? p.getCategoria() : "") + ")");
                if (lblProdutoDefeitoOriginal != null) lblProdutoDefeitoOriginal.setText(p.getDefeito() != null ? p.getDefeito() : "Não informado");
            } else {
                if (lblProdutoNome != null) lblProdutoNome.setText(os.getNomeProduto() != null ? os.getNomeProduto() : "Produto ID " + os.getIdProduto());
            }
        } catch (Exception e) {
            if (lblProdutoNome != null) lblProdutoNome.setText(os.getNomeProduto());
        }
    }

    private void atualizarBadgeStatus(OrdemServicoEntity.TipoStatus status) {
        if (lblStatusBadge == null) return;
        lblStatusBadge.getStyleClass().removeAll("badge-espera", "badge-andamento", "badge-pronto");

        if (status == null) {
            lblStatusBadge.setText("EM ESPERA");
            lblStatusBadge.getStyleClass().add("badge-espera");
            return;
        }

        switch (status) {
            case EM_ESPERA -> {
                lblStatusBadge.setText("EM ESPERA");
                lblStatusBadge.getStyleClass().add("badge-espera");
            }
            case EM_ANDAMENTO -> {
                lblStatusBadge.setText("EM ANDAMENTO");
                lblStatusBadge.getStyleClass().add("badge-andamento");
            }
            case PRONTO -> {
                lblStatusBadge.setText("PRONTO");
                lblStatusBadge.getStyleClass().add("badge-pronto");
            }
        }
    }

    private void salvarAlteracoes() {
        if (!UsuarioSessao.podeEditarOS()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sem Permissão", "Você não tem permissão para editar Ordens de Serviço.");
            return;
        }

        if (ordemAtual == null) return;

        try {
            String desc = txtDescricaoReparo != null ? txtDescricaoReparo.getText().trim() : "";
            String orc = txtOrcamento != null ? txtOrcamento.getText().trim() : "";
            String statusStr = cbStatus != null ? cbStatus.getValue() : "EM ESPERA";

            if (desc.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campo Obrigatório", "A descrição do serviço não pode ficar vazia.");
                return;
            }

            ordemAtual.setDescricao(desc);
            ordemAtual.setOrcamento(orc.isEmpty() ? "0.00" : orc);

            OrdemServicoEntity.TipoStatus novoStatus = OrdemServicoEntity.TipoStatus.EM_ESPERA;
            if ("EM ANDAMENTO".equals(statusStr)) novoStatus = OrdemServicoEntity.TipoStatus.EM_ANDAMENTO;
            else if ("PRONTO".equals(statusStr)) novoStatus = OrdemServicoEntity.TipoStatus.PRONTO;
            ordemAtual.setStatus(novoStatus);

            osDAO.atualizar(ordemAtual);
            atualizarBadgeStatus(novoStatus);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Ordem de Serviço #" + ordemAtual.getId() + " atualizada com sucesso!");

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao salvar alterações: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void avancarStatus() {
        if (!UsuarioSessao.podeEditarOS() || ordemAtual == null) return;

        OrdemServicoEntity.TipoStatus proximo = OrdemServicoEntity.TipoStatus.EM_ANDAMENTO;
        if (ordemAtual.getStatus() == OrdemServicoEntity.TipoStatus.EM_ANDAMENTO) {
            proximo = OrdemServicoEntity.TipoStatus.PRONTO;
        } else if (ordemAtual.getStatus() == OrdemServicoEntity.TipoStatus.PRONTO) {
            proximo = OrdemServicoEntity.TipoStatus.EM_ESPERA;
        }

        try {
            osDAO.atualizarStatus(ordemAtual.getId(), proximo);
            ordemAtual.setStatus(proximo);
            if (cbStatus != null) cbStatus.setValue(ordemAtual.getStatusString());
            atualizarBadgeStatus(proximo);
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível avançar status: " + e.getMessage());
        }
    }

    private void excluirOS() {
        if (!UsuarioSessao.podeDeletarOS()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sem Permissão", "Apenas Administradores podem excluir OS.");
            return;
        }

        if (ordemAtual == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Excluir definitivamente a OS #" + ordemAtual.getId() + "?");
        confirm.setContentText("Esta ação é irreversível.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                osDAO.deletar(ordemAtual.getId());
                fecharJanela();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao excluir", e.getMessage());
            }
        }
    }

    private void fecharJanela() {
        if (btnFechar != null && btnFechar.getScene() != null) {
            Stage stage = (Stage) btnFechar.getScene().getWindow();
            stage.close();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        if (txtResultado != null) {
            txtResultado.setText(msg);
        }
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

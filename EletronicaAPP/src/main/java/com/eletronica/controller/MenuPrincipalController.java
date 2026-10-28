/**
 * CONTROLADOR DA TELA DE MENU PRINCIPAL (DASHBOARD MODERNO)
 * 
 * Layout inspirado no modelo de referência de 3 colunas:
 * - Sidebar Esquerda: Marca, Status da Loja, Navegação e Ação Rápida (+ Nova OS)
 * - Painel Central: Saudação, Barra de Busca, Cards com Destaque (Amarelo, Azul, Vermelho)
 *   e Tabela de Atendimentos Recentes
 * - Painel Direito: Card de Preview em Destaque, Resumo do Reparo e Ações Rápidas
 * 
 * @author joao
 */
package com.eletronica.controller;

import com.eletronica.dao.OrdemServicoDAO;
import com.eletronica.model.OrdemServicoEntity;
import com.eletronica.util.ScreenManager;
import com.eletronica.util.ThemeManager;
import com.eletronica.util.UsuarioSessao;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MenuPrincipalController {

    // ==================== COMPONENTES DA SIDEBAR ====================
    @FXML private Button btnHome;
    @FXML private Button btnOrdens;
    @FXML private Button btnClientes;
    @FXML private Button btnProdutos;
    @FXML private Button btnServicos;
    @FXML private Button btnUsuarios;
    @FXML private Button btnGrupoUsuario;
    @FXML private Button btnNovaOSRapida;
    @FXML private Label lblUsuarioLogado;
    @FXML private Label lblPerfil;
    @FXML private Label lblTotalBadge;
    @FXML private Label lblCapacidadeOS;
    @FXML private ProgressBar progressoCapacidade;

    // ==================== COMPONENTES DO PAINEL CENTRAL ====================
    @FXML private Label lblSaudacao;
    @FXML private TextField txtBuscaDashboard;
    @FXML private Button btnAlternarTema;
    @FXML private SVGPath svgIconeTema;
    @FXML private Label lblQtdEspera;
    @FXML private Label lblQtdAndamento;
    @FXML private Label lblQtdPronto;
    @FXML private Label lblQtdTotal;

    // Tabela de OS Recentes
    @FXML private TableView<OrdemServicoEntity> tblOrdensDashboard;
    @FXML private TableColumn<OrdemServicoEntity, Integer> colId;
    @FXML private TableColumn<OrdemServicoEntity, String> colCliente;
    @FXML private TableColumn<OrdemServicoEntity, String> colProduto;
    @FXML private TableColumn<OrdemServicoEntity, String> colDescricao;
    @FXML private TableColumn<OrdemServicoEntity, String> colStatus;
    @FXML private TableColumn<OrdemServicoEntity, String> colOrcamento;
    @FXML private TableColumn<OrdemServicoEntity, String> colData;

    // ==================== COMPONENTES DO PAINEL DIREITO (PREVIEW) ====================
    @FXML private Label lblPreviewTitulo;
    @FXML private Label lblPreviewSubtitulo;
    @FXML private Label lblPreviewStatus;
    @FXML private Label lblPreviewOrcamento;
    @FXML private Label lblPreviewCliente;
    @FXML private Label lblPreviewProduto;
    @FXML private TextArea txtPreviewDescricao;
    @FXML private Button btnPreviewAbrirOS;
    @FXML private Button btnPreviewStatus;
    @FXML private Button btnPreviewDeletarOS;

    // ==================== ATRIBUTOS PRIVADOS ====================
    private OrdemServicoDAO osDAO;
    private ObservableList<OrdemServicoEntity> listaOrdens;
    private FilteredList<OrdemServicoEntity> listaFiltrada;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==================== INICIALIZADOR ====================
    @FXML
    public void initialize() {
        System.out.println("=== INICIALIZANDO MENU (DARK DASHBOARD) ===");

        if (!UsuarioSessao.isLogado()) {
            System.out.println("ERRO: Nenhum usuário logado!");
            return;
        }

        osDAO = new OrdemServicoDAO();
        listaOrdens = FXCollections.observableArrayList();

        // 1. Exibir dados do usuário logado
        String nome = UsuarioSessao.getUsuarioLogado().getNome();
        String perfil = UsuarioSessao.getGrupoLogado().getDescricao();

        if (lblUsuarioLogado != null) lblUsuarioLogado.setText("Usuário: " + nome);
        if (lblPerfil != null) lblPerfil.setText("Perfil: " + perfil);
        if (lblSaudacao != null) lblSaudacao.setText("Ola, " + nome + "! Bem-vindo de volta.");

        // 2. Controle de permissões na Sidebar
        if (!UsuarioSessao.podeManterUsuario()) {
            if (btnUsuarios != null) {
                btnUsuarios.setVisible(false);
                btnUsuarios.setManaged(false);
            }
            if (btnGrupoUsuario != null) {
                btnGrupoUsuario.setVisible(false);
                btnGrupoUsuario.setManaged(false);
            }
        }

        // 3. Configuração dos botões da Sidebar
        if (btnHome != null) btnHome.setOnAction(e -> recarregarDashboard());
        if (btnClientes != null) btnClientes.setOnAction(e -> abrirTelaClientes());
        if (btnProdutos != null) btnProdutos.setOnAction(e -> abrirTelaProdutos());
        if (btnServicos != null) btnServicos.setOnAction(e -> abrirTelaServicos());
        if (btnOrdens != null) btnOrdens.setOnAction(e -> abrirTelaOrdens());
        if (btnUsuarios != null) btnUsuarios.setOnAction(e -> abrirTelaUsuarios());
        if (btnGrupoUsuario != null) btnGrupoUsuario.setOnAction(e -> abrirTelaGrupoUsuarios());
        if (btnNovaOSRapida != null) btnNovaOSRapida.setOnAction(e -> abrirTelaGerarOS());

        // 4. Configuração das colunas da tabela
        configurarTabela();

        // 5. Configurar ações dos botões do Preview
        configurarBotoesPreview();

        // 6. Configurar alternância de tema (Claro / Escuro)
        if (btnAlternarTema != null) {
            btnAlternarTema.setOnAction(e -> alternarTema());
            ThemeManager.addListener(this::atualizarBotaoTema);
            atualizarBotaoTema(ThemeManager.isDark());
        }

        Platform.runLater(() -> {
            if (btnHome != null && btnHome.getScene() != null && btnHome.getScene().getWindow() instanceof Stage stage) {
                ScreenManager.setTelaPrincipal(stage);
            }
        });

        // 7. Carregar dados das ordens e cards
        recarregarDashboard();
    }

    private void configurarTabela() {
        if (tblOrdensDashboard == null) return;

        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colCliente != null) colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        if (colProduto != null) colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        if (colDescricao != null) colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        if (colOrcamento != null) colOrcamento.setCellValueFactory(cellData -> {
            String orc = cellData.getValue().getOrcamento();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> (orc == null || orc.isEmpty()) ? "R$ 0,00" : "R$ " + orc
            );
        });

        if (colData != null) {
            colData.setCellValueFactory(cellData -> {
                LocalDate d = cellData.getValue().getData();
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> d != null ? d.format(formatter) : ""
                );
            });
        }

        // Listener de seleção para atualizar o Preview à direita
        tblOrdensDashboard.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                atualizarPreview(novo);
            }
        });

        // Configura busca em tempo real
        listaFiltrada = new FilteredList<>(listaOrdens, p -> true);
        if (txtBuscaDashboard != null) {
            txtBuscaDashboard.textProperty().addListener((obs, antigo, termo) -> {
                listaFiltrada.setPredicate(os -> {
                    if (termo == null || termo.isBlank()) return true;
                    String filtro = termo.toLowerCase().trim();
                    boolean matchCliente = os.getNomeCliente() != null && os.getNomeCliente().toLowerCase().contains(filtro);
                    boolean matchProduto = os.getNomeProduto() != null && os.getNomeProduto().toLowerCase().contains(filtro);
                    boolean matchDescricao = os.getDescricao() != null && os.getDescricao().toLowerCase().contains(filtro);
                    boolean matchStatus = os.getStatusString() != null && os.getStatusString().toLowerCase().contains(filtro);
                    return matchCliente || matchProduto || matchDescricao || matchStatus;
                });
            });
        }

        // Duplo clique na linha da tabela abre os detalhes da OS
        tblOrdensDashboard.setRowFactory(tv -> {
            TableRow<OrdemServicoEntity> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    abrirDetalhesOSSelecionada();
                }
            });
            return row;
        });

        tblOrdensDashboard.setItems(listaFiltrada);
    }

    private void configurarBotoesPreview() {
        if (btnPreviewAbrirOS != null) {
            btnPreviewAbrirOS.setOnAction(e -> abrirDetalhesOSSelecionada());
        }

        // Botão de Avançar Status rápido
        if (btnPreviewStatus != null) {
            btnPreviewStatus.setOnAction(e -> alternarStatusRapido());
        }

        // Botão de Deletar com confirmação
        if (btnPreviewDeletarOS != null) {
            if (!UsuarioSessao.podeDeletarOS()) {
                btnPreviewDeletarOS.setVisible(false);
                btnPreviewDeletarOS.setManaged(false);
            } else {
                btnPreviewDeletarOS.setOnAction(e -> deletarOSSelecionada());
            }
        }
    }

    /**
     * Recarrega os dados do dashboard (cards e tabela) a partir do banco
     */
    public void recarregarDashboard() {
        try {
            List<OrdemServicoEntity> todas = osDAO.listarTodas();
            listaOrdens.clear();
            listaOrdens.addAll(todas);

            // Contadores
            int espera = 0;
            int andamento = 0;
            int pronto = 0;

            for (OrdemServicoEntity os : todas) {
                if (os.getStatus() != null) {
                    switch (os.getStatus()) {
                        case EM_ESPERA -> espera++;
                        case EM_ANDAMENTO -> andamento++;
                        case PRONTO -> pronto++;
                    }
                }
            }

            if (lblQtdEspera != null) lblQtdEspera.setText(String.valueOf(espera));
            if (lblQtdAndamento != null) lblQtdAndamento.setText(String.valueOf(andamento));
            if (lblQtdPronto != null) lblQtdPronto.setText(String.valueOf(pronto));
            if (lblQtdTotal != null) lblQtdTotal.setText(String.valueOf(todas.size()));

            if (lblCapacidadeOS != null) lblCapacidadeOS.setText((espera + andamento) + " Ativas");
            if (lblTotalBadge != null) lblTotalBadge.setText(todas.size() + " total");
            if (progressoCapacidade != null) {
                double total = todas.size() == 0 ? 1.0 : todas.size();
                double ativas = (espera + andamento) / total;
                progressoCapacidade.setProgress(Math.min(1.0, Math.max(0.05, ativas)));
            }

            // Seleciona a primeira linha por padrão no preview
            if (!listaOrdens.isEmpty() && tblOrdensDashboard != null) {
                tblOrdensDashboard.getSelectionModel().selectFirst();
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Atualiza o painel lateral direito com os dados da OS selecionada
     */
    private void atualizarPreview(OrdemServicoEntity os) {
        if (os == null) return;

        if (lblPreviewTitulo != null) {
            String equip = (os.getNomeProduto() != null && !os.getNomeProduto().isBlank()) ? os.getNomeProduto() : "Aparelho";
            lblPreviewTitulo.setText("OS #" + os.getId() + " - " + equip);
        }

        if (lblPreviewSubtitulo != null) {
            String dataStr = os.getData() != null ? os.getData().format(formatter) : "";
            lblPreviewSubtitulo.setText("Abertura: " + dataStr);
        }

        if (lblPreviewCliente != null) {
            lblPreviewCliente.setText(os.getNomeCliente() != null ? os.getNomeCliente() : "Não informado");
        }

        if (lblPreviewProduto != null) {
            lblPreviewProduto.setText(os.getNomeProduto() != null ? os.getNomeProduto() : "Não informado");
        }

        if (lblPreviewOrcamento != null) {
            String orc = os.getOrcamento();
            lblPreviewOrcamento.setText((orc == null || orc.isEmpty()) ? "R$ 0,00" : "R$ " + orc);
        }

        if (txtPreviewDescricao != null) {
            txtPreviewDescricao.setText(os.getDescricao() != null ? os.getDescricao() : "");
        }

        if (lblPreviewStatus != null) {
            lblPreviewStatus.setText(os.getStatusString());
            lblPreviewStatus.getStyleClass().removeAll("badge-espera", "badge-andamento", "badge-pronto");
            if (os.getStatus() != null) {
                switch (os.getStatus()) {
                    case EM_ESPERA -> lblPreviewStatus.getStyleClass().add("badge-espera");
                    case EM_ANDAMENTO -> lblPreviewStatus.getStyleClass().add("badge-andamento");
                    case PRONTO -> lblPreviewStatus.getStyleClass().add("badge-pronto");
                }
            } else {
                lblPreviewStatus.getStyleClass().add("badge-espera");
            }
        }
    }

    /**
     * Altera o status da OS selecionada no preview (EM ESPERA -> EM ANDAMENTO -> PRONTO -> EM ESPERA)
     */
    private void alternarStatusRapido() {
        if (!UsuarioSessao.podeEditarOS()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sem Permissão", "Você não tem permissão para alterar o status da OS.");
            return;
        }

        OrdemServicoEntity selecionada = tblOrdensDashboard != null ? tblOrdensDashboard.getSelectionModel().getSelectedItem() : null;
        if (selecionada == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Seleção necessária", "Selecione uma ordem de serviço na tabela.");
            return;
        }

        OrdemServicoEntity.TipoStatus novoStatus = OrdemServicoEntity.TipoStatus.EM_ANDAMENTO;
        if (selecionada.getStatus() == OrdemServicoEntity.TipoStatus.EM_ANDAMENTO) {
            novoStatus = OrdemServicoEntity.TipoStatus.PRONTO;
        } else if (selecionada.getStatus() == OrdemServicoEntity.TipoStatus.PRONTO) {
            novoStatus = OrdemServicoEntity.TipoStatus.EM_ESPERA;
        }

        try {
            osDAO.atualizarStatus(selecionada.getId(), novoStatus);
            selecionada.setStatus(novoStatus);
            recarregarDashboard();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar o status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deleta a OS selecionada após confirmação
     */
    private void deletarOSSelecionada() {
        if (!UsuarioSessao.podeDeletarOS()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sem Permissão", "Apenas Administradores podem excluir Ordens de Serviço.");
            return;
        }

        OrdemServicoEntity selecionada = tblOrdensDashboard != null ? tblOrdensDashboard.getSelectionModel().getSelectedItem() : null;
        if (selecionada == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Seleção necessária", "Selecione uma ordem de serviço para excluir.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Deseja realmente excluir a OS #" + selecionada.getId() + "?");
        confirm.setContentText("Cliente: " + selecionada.getNomeCliente() + "\nEquipamento: " + selecionada.getNomeProduto());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                osDAO.deletar(selecionada.getId());
                recarregarDashboard();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao excluir", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ==================== MÉTODOS DE ABERTURA DE TELAS ====================
    private void abrirTela(String fxmlPath, String titulo) {
        ScreenManager.abrirTelaSecundaria(fxmlPath, titulo, this::recarregarDashboard);
    }

    private void abrirTelaClientes() {
        abrirTela("/com/eletronica/view/FrmCliente.fxml", "Cadastro de Clientes");
    }

    private void abrirTelaProdutos() {
        abrirTela("/com/eletronica/view/FrmProduto.fxml", "Cadastro de Produtos / Equipamentos");
    }

    private void abrirTelaServicos() {
        abrirTela("/com/eletronica/view/FrmServico.fxml", "Catálogo de Serviços");
    }

    private void abrirTelaGerarOS() {
        if (!UsuarioSessao.podeCriarOS()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sem Permissão", "Você não tem permissão para cadastrar novas Ordens de Serviço.");
            return;
        }
        abrirTela("/com/eletronica/view/FrmGerarOS.fxml", "Gerar Nova Ordem de Serviço (UC05)");
    }

    private void abrirDetalhesOSSelecionada() {
        OrdemServicoEntity selecionada = tblOrdensDashboard != null ? tblOrdensDashboard.getSelectionModel().getSelectedItem() : null;
        if (selecionada == null) {
            if (!listaOrdens.isEmpty() && tblOrdensDashboard != null) {
                tblOrdensDashboard.getSelectionModel().selectFirst();
                selecionada = tblOrdensDashboard.getSelectionModel().getSelectedItem();
            }
        }

        if (selecionada == null) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Seleção necessária", "Selecione uma ordem de serviço na lista para ver os detalhes.");
            return;
        }

        DetalhesOSController.setOrdemParaExibir(selecionada);
        abrirTela("/com/eletronica/view/FrmDetalhesOS.fxml", "Consulta e Detalhes da Ordem de Serviço #" + selecionada.getId());
    }

    private void abrirTelaOrdens() {
        abrirTela("/com/eletronica/view/FrmOrdemServico.fxml", "Gerenciamento de Ordens de Serviço (OS)");
    }

    private void abrirTelaUsuarios() {
        if (!UsuarioSessao.podeManterUsuario()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acesso Negado", "Você não tem permissão para acessar esta funcionalidade!");
            return;
        }
        abrirTela("/com/eletronica/view/FrmUsuario.fxml", "Gerenciamento de Usuários");
    }

    private void abrirTelaGrupoUsuarios() {
        if (!UsuarioSessao.podeManterUsuario()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Acesso Negado", "Você não tem permissão para acessar esta funcionalidade!");
            return;
        }
        abrirTela("/com/eletronica/view/FrmGrupoUsuario.fxml", "Cadastro de Grupos e Permissões");
    }

    @FXML
    private void alternarTema() {
        ThemeManager.alternarTema();
    }

    private void atualizarBotaoTema(boolean isDark) {
        if (btnAlternarTema == null) return;
        if (isDark) {
            btnAlternarTema.setText("Tema Claro");
            if (svgIconeTema != null) {
                svgIconeTema.setContent("M8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM8 0a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 0zm0 13a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 13zm8-5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2a.5.5 0 0 1 .5.5zM3 8a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2A.5.5 0 0 1 3 8zm10.657-5.657a.5.5 0 0 1 0 .707l-1.414 1.415a.5.5 0 1 1-.707-.708l1.414-1.414a.5.5 0 0 1 .707 0zm-9.193 9.193a.5.5 0 0 1 0 .707L3.05 13.657a.5.5 0 0 1-.707-.707l1.414-1.414a.5.5 0 0 1 .707 0zm9.193 2.121a.5.5 0 0 1-.707 0l-1.414-1.414a.5.5 0 0 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .707zM4.464 4.465a.5.5 0 0 1-.707 0L2.343 3.05a.5.5 0 1 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .708z");
            }
        } else {
            btnAlternarTema.setText("Tema Escuro");
            if (svgIconeTema != null) {
                svgIconeTema.setContent("M6 .278a.768.768 0 0 1 .08.858 7.208 7.208 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277.527 0 1.04-.055 1.533-.16a.787.787 0 0 1 .81.316.733.733 0 0 1-.031.893A8.349 8.349 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.752.752 0 0 1 6 .278z");
            }
        }
    }

    @FXML
    private void sair() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Sair");
        confirm.setHeaderText("Deseja realmente sair?");
        confirm.setContentText("Você será desconectado do sistema.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            ScreenManager.fecharTelaSecundariaAberta();
            UsuarioSessao.logout();
            com.eletronica.util.Database.closeConnection();
            System.exit(0);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
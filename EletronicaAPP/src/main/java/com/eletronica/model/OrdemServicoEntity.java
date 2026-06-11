/**
 * ENTIDADE/ORDEM DE SERVIÇO - MODELO DE DADOS
 * 
 * Esta classe representa a tabela 'ordens_servico' no banco de dados PostgreSQL.
 * É um POJO (Plain Old Java Object) que contém atributos, construtores,
 * getters e setters, além de um Enum para o status.
 * 
 * Funcionalidade:
 * - Armazenar os dados de uma Ordem de Serviço
 * - Gerenciar o status da OS (EM ESPERA, EM ANDAMENTO, PRONTO)
 * - Armazenar relacionamentos com Cliente e Produto
 * - Transferir dados entre as camadas da aplicação (Controller ↔ DAO)
 * 
 * Relacionamentos (FKs):
 * - idCliente → referencia a tabela 'cliente'
 * - idProduto → referencia a tabela 'produtos'
 * 
 * A tabela corresponde a: CREATE TABLE ordens_servico (...)
 * 
 * @author joao
 */
package com.eletronica.model;

import java.time.LocalDate;  // Para trabalhar com datas (ao invés de int como no diagrama)

public class OrdemServicoEntity {
    
    // ==================== ATRIBUTOS PRINCIPAIS ====================
    // Correspondem diretamente às colunas da tabela 'ordens_servico' no banco
    
    private int id;                 // Identificador único (PK - SERIAL no PostgreSQL)
    private String orcamento;       // Valor orçado para o serviço (String conforme diagrama)
    private LocalDate data;         // Data de criação da OS (LocalDate - melhor que int)
    private String descricao;       // Descrição detalhada do serviço a ser realizado
    private TipoStatus status;      // Status atual da OS (Enum)
    
    // ==================== ENUM STATUS (conforme diagrama) ====================
    
    /**
     * ENUM PARA OS STATUS DA ORDEM DE SERVIÇO
     * 
     * Conforme o diagrama de classes, os status possíveis são:
     * - EM ESPERA: OS criada, aguardando início do serviço
     * - EM ANDAMENTO: Serviço em execução
     * - PRONTO: Serviço concluído, aguardando retirada
     * 
     * O uso de Enum garante que apenas estes valores sejam usados,
     * evitando erros de digitação.
     */
    public enum TipoStatus {
        EM_ESPERA,      // OS aguardando início (padrão)
        EM_ANDAMENTO,   // Serviço em andamento
        PRONTO          // Serviço concluído
    }
    
    // ==================== CONSTRUTORES ====================
    
    /**
     * CONSTRUTOR PADRÃO
     * 
     * Inicializa a data com a data atual do sistema e
     * o status como EM_ESPERA (valor padrão).
     */
    public OrdemServicoEntity() {
        this.data = LocalDate.now();           // Data atual do sistema
        this.status = TipoStatus.EM_ESPERA;    // Status inicial padrão
    }
    
    /**
     * CONSTRUTOR COM PARÂMETROS PRINCIPAIS
     * 
     * @param descricao Descrição do serviço
     * @param orcamento Valor orçado
     * @param idCliente ID do cliente (FK)
     * @param idProduto ID do produto (FK)
     */
    public OrdemServicoEntity(String descricao, String orcamento, int idCliente, int idProduto) {
        this();  // Chama o construtor padrão (define data e status)
        this.descricao = descricao;
        this.orcamento = orcamento;
        this.idCliente = idCliente;
        this.idProduto = idProduto;
    }
    
    // ==================== ATRIBUTOS DE RELACIONAMENTO (FKs) ====================
    
    // Relacionamento com CLIENTE
    private int idCliente;        // Chave estrangeira para a tabela cliente
    private String nomeCliente;   // Nome do cliente (vem do JOIN, NÃO está no banco)
    
    // Relacionamento com PRODUTO
    private int idProduto;        // Chave estrangeira para a tabela produtos
    private String nomeProduto;   // Nome do produto (vem do JOIN, NÃO está no banco)
    
    // ==================== GETTERS E SETTERS ====================
    
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getOrcamento() { 
        return orcamento; 
    }
    
    public void setOrcamento(String orcamento) { 
        this.orcamento = orcamento; 
    }
    
    public LocalDate getData() { 
        return data; 
    }
    
    public void setData(LocalDate data) { 
        this.data = data; 
    }
    
    public String getDescricao() { 
        return descricao; 
    }
    
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }
    
    public TipoStatus getStatus() { 
        return status; 
    }
    
    public void setStatus(TipoStatus status) { 
        this.status = status; 
    }
    
    // ==================== MÉTODO AUXILIAR PARA STATUS ====================
    
    /**
     * RETORNA O STATUS COMO STRING FORMATADA PARA EXIBIÇÃO
     * 
     * Converte o Enum para uma string amigável ao usuário.
     * Exemplo: EM_ESPERA → "EM ESPERA" (com espaço)
     * 
     * Este método é usado no ComboBox e na TableView.
     * 
     * @return String formatada do status
     */
    public String getStatusString() {
        switch (status) {
            case EM_ESPERA: 
                return "EM ESPERA";
            case EM_ANDAMENTO: 
                return "EM ANDAMENTO";
            case PRONTO: 
                return "PRONTO";
            default: 
                return "";
        }
    }
    
    // ==================== GETTERS E SETTERS DOS RELACIONAMENTOS ====================
    
    public int getIdCliente() { 
        return idCliente; 
    }
    
    public void setIdCliente(int idCliente) { 
        this.idCliente = idCliente; 
    }
    
    /**
     * GETTER DO NOME DO CLIENTE
     * @return Nome do cliente (vem do JOIN no DAO)
     */
    public String getNomeCliente() { 
        return nomeCliente; 
    }
    
    /**
     * SETTER DO NOME DO CLIENTE
     * Geralmente chamado pelo DAO ao fazer JOIN com a tabela cliente
     * @param nomeCliente Nome do cliente
     */
    public void setNomeCliente(String nomeCliente) { 
        this.nomeCliente = nomeCliente; 
    }
    
    public int getIdProduto() { 
        return idProduto; 
    }
    
    public void setIdProduto(int idProduto) { 
        this.idProduto = idProduto; 
    }
    
    /**
     * GETTER DO NOME DO PRODUTO
     * @return Nome do produto (vem do JOIN no DAO)
     */
    public String getNomeProduto() { 
        return nomeProduto; 
    }
    
    /**
     * SETTER DO NOME DO PRODUTO
     * Geralmente chamado pelo DAO ao fazer JOIN com a tabela produtos
     * @param nomeProduto Nome do produto
     */
    public void setNomeProduto(String nomeProduto) { 
        this.nomeProduto = nomeProduto; 
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.eletronica.model;

import java.time.LocalDate;

/**
 *
 * @author joao
 */

public class OrdemServicoEntity {
    private int id;
    private String orcamento;  // Mantido como String conforme seu diagrama
    private LocalDate data;    // LocalDate (não int como no diagrama, mas é o correto)
    private String descricao;
    private TipoStatus status;
    
    // Enum conforme o diagrama
    public enum TipoStatus {
        EM_ESPERA,
        EM_ANDAMENTO,
        PRONTO
    }
    
    // Construtores
    public OrdemServicoEntity() {
        this.data = LocalDate.now();
        this.status = TipoStatus.EM_ESPERA;
    }
    
    public OrdemServicoEntity(String descricao, String orcamento, int idCliente, int idProduto) {
        this();
        this.descricao = descricao;
        this.orcamento = orcamento;
        this.idCliente = idCliente;
        this.idProduto = idProduto;
    }
    
    // Relacionamentos (adicionar conforme diagrama)
    private int idCliente;
    private String nomeCliente;  // Para exibição
    
    private int idProduto;
    private String nomeProduto;  // Para exibição
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getOrcamento() { return orcamento; }
    public void setOrcamento(String orcamento) { this.orcamento = orcamento; }
    
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public TipoStatus getStatus() { return status; }
    public void setStatus(TipoStatus status) { this.status = status; }
    
    // Para usar no ComboBox
    public String getStatusString() {
        switch (status) {
            case EM_ESPERA: return "EM ESPERA";
            case EM_ANDAMENTO: return "EM ANDAMENTO";
            case PRONTO: return "PRONTO";
            default: return "";
        }
    }
    
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    
    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }
    
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
}
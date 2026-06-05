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
    private String orcamento;
    private LocalDate data;
    private String descricao;
    private TipoStatus Status;
    
    private enum TipoStatus{
        EmEspra,
        EmAmdamento,
        Pronto
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the orcamento
     */
    public String getOrcamento() {
        return orcamento;
    }

    /**
     * @param orcamento the orcamento to set
     */
    public void setOrcamento(String orcamento) {
        this.orcamento = orcamento;
    }

    /**
     * @return the data
     */
    public LocalDate getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(LocalDate data) {
        this.data = data;
    }

    /**
     * @return the descricao
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * @param descricao the descricao to set
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return the Status
     */
    public TipoStatus getStatus() {
        return Status;
    }

    /**
     * @param Status the Status to set
     */
    public void setStatus(TipoStatus Status) {
        this.Status = Status;
    }
    
    
    
    
    
}

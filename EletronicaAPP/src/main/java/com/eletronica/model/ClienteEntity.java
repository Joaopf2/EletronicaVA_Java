/**
 * ENTIDADE/CLIENTE - MODELO DE DADOS
 * 
 * Esta classe representa a tabela 'cliente' no banco de dados PostgreSQL.
 * É um POJO (Plain Old Java Object) que contém apenas atributos, construtores,
 * getters e setters.
 * 
 * Funcionalidade:
 * - Armazenar os dados de um cliente
 * - Transferir dados entre as camadas da aplicação (Controller ↔ DAO)
 * - Ser usada como tipo na TableView do JavaFX
 * 
 * A tabela corresponde a: CREATE TABLE cliente (...)
 * 
 * @author joao
 */
package com.eletronica.model;

public class ClienteEntity {
    
    // ==================== ATRIBUTOS ====================
    // Correspondem diretamente às colunas da tabela 'cliente' no banco
    
    private int id;           // Identificador único (PK - SERIAL no PostgreSQL)
    private String nome;      // Nome completo do cliente (obrigatório)
    private String email;     // Email do cliente (obrigatório)
    private String telefone;  // Telefone para contato (obrigatório)
    private String cnpjCpf;   // CPF (pessoa física) ou CNPJ (pessoa jurídica) - obrigatório
    private String rg;        // Registro Geral (RG) - obrigatório
    private String ie;        // Inscrição Estadual (IE) - OPCIONAL (pode ser null)
    
    // ==================== CONSTRUTORES ====================
    
    /**
     * CONSTRUTOR PADRÃO (sem parâmetros)
     * 
     * Necessário para o JavaFX e para o框架 PropertyValueFactory.
     * Permite criar um objeto vazio e preencher via setters.
     */
    public ClienteEntity() {}
    
    /**
     * CONSTRUTOR COM PARÂMETROS
     * 
     * Útil para criar um cliente já com todos os dados preenchidos.
     * O ID NÃO é passado pois é gerado automaticamente pelo banco.
     * 
     * @param nome Nome completo do cliente
     * @param email Email do cliente
     * @param telefone Telefone para contato
     * @param cnpjCpf CPF ou CNPJ
     * @param rg Registro Geral (RG)
     * @param ie Inscrição Estadual (opcional)
     */
    public ClienteEntity(String nome, String email, String telefone, 
                         String cnpjCpf, String rg, String ie) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cnpjCpf = cnpjCpf;
        this.rg = rg;
        this.ie = ie;
        // id não é definido aqui - será gerado pelo banco
    }
    
    // ==================== GETTERS E SETTERS ====================
    // Permitem acesso controlado aos atributos privados
    
    /**
     * GETTER DO ID
     * @return Identificador único do cliente
     */
    public int getId() { 
        return id; 
    }
    
    /**
     * SETTER DO ID
     * Geralmente chamado pelo DAO após inserir no banco, para
     * atribuir o ID gerado automaticamente (SERIAL) ao objeto.
     * 
     * @param id ID gerado pelo banco de dados
     */
    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getNome() { 
        return nome; 
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getTelefone() { 
        return telefone; 
    }
    
    public void setTelefone(String telefone) { 
        this.telefone = telefone; 
    }
    
    public String getCnpjCpf() { 
        return cnpjCpf; 
    }
    
    public void setCnpjCpf(String cnpjCpf) { 
        this.cnpjCpf = cnpjCpf; 
    }
    
    public String getRg() { 
        return rg; 
    }
    
    public void setRg(String rg) { 
        this.rg = rg; 
    }
    
    /**
     * GETTER DA INSCRIÇÃO ESTADUAL
     * Pode retornar null (campo opcional no banco)
     * @return IE ou null
     */
    public String getIe() { 
        return ie; 
    }
    
    /**
     * SETTER DA INSCRIÇÃO ESTADUAL
     * @param ie Pode ser null ou string vazia
     */
    public void setIe(String ie) { 
        this.ie = ie; 
    }
}
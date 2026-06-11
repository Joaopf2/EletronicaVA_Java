/**
 * ENTIDADE/GRUPO DE USUÁRIO - MODELO DE DADOS
 * 
 * Esta classe representa a tabela 'grupos_usuarios' no banco de dados PostgreSQL.
 * É um POJO (Plain Old Java Object) que contém apenas atributos, construtores,
 * getters e setters.
 * 
 * Funcionalidade:
 * - Armazenar os dados de um grupo de usuário (perfis de acesso)
 * - Definir quais permissões os usuários terão no sistema
 * - Transferir dados entre as camadas da aplicação (Controller ↔ DAO)
 * - Ser usada como tipo na TableView e ComboBox do JavaFX
 * 
 * Permissões controladas:
 * - permissao: Acesso básico ao sistema (true = pode logar)
 * - manterUsuario: Pode cadastrar/editar/deletar USUÁRIOS e GRUPOS
 * - manterServico: Pode cadastrar/editar/deletar SERVIÇOS
 * 
 * A tabela corresponde a: CREATE TABLE grupos_usuarios (...)
 * 
 * @author joao
 */
package com.eletronica.model;

public class GrupoUsuarioEntity {
    
    // ==================== ATRIBUTOS ====================
    // Correspondem diretamente às colunas da tabela 'grupos_usuarios' no banco
    
    private int id;                 // Identificador único (PK - SERIAL no PostgreSQL)
    private String descricao;       // Nome do grupo (ex: "Administrador", "Técnico")
    private int grupo;              // Código numérico do grupo (1, 2, 3...)
    private boolean permissao;      // Permissão básica de acesso ao sistema
    private boolean manterUsuario;  // Permissão para gerenciar usuários e grupos
    private boolean manterServico;  // Permissão para gerenciar serviços
    
    // ==================== CONSTRUTOR ====================
    
    /**
     * CONSTRUTOR PADRÃO (sem parâmetros)
     * 
     * Necessário para o JavaFX e para o框架 PropertyValueFactory.
     * Permite criar um objeto vazio e preencher via setters.
     */
    public GrupoUsuarioEntity() {}
    
    // ==================== GETTERS E SETTERS ====================
    // Permitem acesso controlado aos atributos privados
    
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
    
    public String getDescricao() { 
        return descricao; 
    }
    
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }
    
    public int getGrupo() { 
        return grupo; 
    }
    
    /**
     * SETTER DO CÓDIGO DO GRUPO
     * @param grupo Código numérico (ex: 1, 2, 3...)
     */
    public void setGrupo(int grupo) { 
        this.grupo = grupo; 
    }
    
    /**
     * GETTER DA PERMISSÃO BÁSICA
     * @return true se o usuário tem acesso ao sistema
     */
    public boolean isPermissao() { 
        return permissao; 
    }
    
    public void setPermissao(boolean permissao) { 
        this.permissao = permissao; 
    }
    
    /**
     * GETTER DA PERMISSÃO DE MANTER USUÁRIOS
     * @return true se pode gerenciar usuários e grupos
     */
    public boolean isManterUsuario() { 
        return manterUsuario; 
    }
    
    public void setManterUsuario(boolean manterUsuario) { 
        this.manterUsuario = manterUsuario; 
    }
    
    /**
     * GETTER DA PERMISSÃO DE MANTER SERVIÇOS
     * @return true se pode gerenciar serviços
     */
    public boolean isManterServico() { 
        return manterServico; 
    }
    
    public void setManterServico(boolean manterServico) { 
        this.manterServico = manterServico; 
    }
    
    // ==================== MÉTODO toString() ====================
    
    /**
     * SOBRESCREVE O MÉTODO toString()
     * 
     * Retorna a descrição do grupo para exibição no ComboBox.
     * 
     * Exemplo: Se o grupo é "Administrador", o ComboBox mostrará "Administrador"
     * em vez do endereço de memória do objeto.
     * 
     * @return Descrição do grupo
     */
    @Override
    public String toString() {
        return descricao;  // Exibe a descrição (ex: "Administrador", "Técnico")
    }
}
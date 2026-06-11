/**
 * ENTIDADE/USUÁRIO - MODELO DE DADOS
 * 
 * Esta classe representa a tabela 'usuarios' no banco de dados PostgreSQL.
 * É um POJO (Plain Old Java Object) que contém atributos, construtores,
 * getters e setters.
 * 
 * Funcionalidade:
 * - Armazenar os dados de um usuário do sistema
 * - Armazenar o relacionamento com GrupoUsuarioEntity
 * - Transferir dados entre as camadas da aplicação (Controller ↔ DAO)
 * - Ser usada como tipo na TableView do JavaFX
 * 
 * Relacionamentos:
 * - Um usuário pertence a UM grupo (FK: idGrupoUsuario)
 * - As permissões do usuário vêm do grupo ao qual ele pertence
 * 
 * A tabela corresponde a: CREATE TABLE usuarios (...)
 * 
 * @author joao
 */
package com.eletronica.model;

public class UsuarioEntity {
    
    private int id;                 // Identificador único (PK - SERIAL no PostgreSQL)
    private String nome;            // Nome completo do usuário (obrigatório)
    private String email;           // Email do usuário (usado para login, obrigatório)
    private String senha;           // Senha de acesso (obrigatória)
    private int idGrupoUsuario;     // Chave estrangeira para a tabela grupos_usuarios
    
    // ATRIBUTOS DE RELACIONAMENTO
    // Estes atributos NÃO estão no banco - são preenchidos via JOIN
    
    private String nomeGrupo;              // Nome do grupo (vem do JOIN, para exibição)
    private GrupoUsuarioEntity grupo;      // Objeto completo do grupo (com permissões)
   
    public UsuarioEntity() {}
    
    public UsuarioEntity(String nome, String email, String senha, int idGrupoUsuario) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.idGrupoUsuario = idGrupoUsuario;
        // id é gerado pelo banco, grupo e nomeGrupo serão preenchidos via JOIN
    }
    
    public int getId() { 
        return id; 
    }
    
    /**
     * SETTER DO ID
     * Geralmente chamado pelo DAO após inserir no banco, para
     * atribuir o ID gerado automaticamente (SERIAL) ao objeto.
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
    
   
    public String getSenha() { 
        return senha; 
    }
    
    public void setSenha(String senha) { 
        this.senha = senha; 
    }
    
   
    public int getIdGrupoUsuario() { 
        return idGrupoUsuario; 
    }
    
    public void setIdGrupoUsuario(int idGrupoUsuario) { 
        this.idGrupoUsuario = idGrupoUsuario; 
    }
    
    // ==================== GETTERS E SETTERS DE RELACIONAMENTO ====================
    
    /**
     * GETTER DO NOME DO GRUPO
     * @return Nome do grupo (ex: "Administrador", "Técnico")
     * Este valor vem do JOIN com a tabela grupos_usuarios,
     * NÃO está armazenado diretamente na tabela usuarios.
     */
    public String getNomeGrupo() { 
        return nomeGrupo; 
    }
    
    public void setNomeGrupo(String nomeGrupo) { 
        this.nomeGrupo = nomeGrupo; 
    }
    
    /**
     * GETTER DO OBJETO GRUPO COMPLETO
     * @return Objeto GrupoUsuarioEntity com todas as permissões
     * 
     * Este atributo é preenchido pelo método buscarPorEmailComGrupo()
     * durante o login, contendo todas as permissões do usuário.
     */
    public GrupoUsuarioEntity getGrupo() { 
        return grupo; 
    }
    
    /**
     * SETTER DO OBJETO GRUPO COMPLETO
     * @param grupo Objeto GrupoUsuarioEntity com permissões
     */
    public void setGrupo(GrupoUsuarioEntity grupo) { 
        this.grupo = grupo; 
    }
    
    // ==================== MÉTODO toString() ====================
    
    /**
     * SOBRESCREVE O MÉTODO toString()
     * 
     * Retorna o nome do usuário para exibição no ComboBox/TableView.
     * 
     * @return Nome do usuário
     */
    @Override
    public String toString() {
        return nome;  // Exibe o nome (ex: "João Silva")
    }
}
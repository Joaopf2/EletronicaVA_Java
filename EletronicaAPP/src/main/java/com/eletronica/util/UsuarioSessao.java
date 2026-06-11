/**
 * CLASSE DE GERENCIAMENTO DE SESSÃO DO USUÁRIO (SINGLETON)
 * 
 * Esta classe é responsável por manter os dados do usuário logado
 * durante toda a execução da aplicação.
 * 
 * Funcionalidades:
 * - Armazenar quem está logado no sistema
 * - Fornecer acesso rápido às informações do usuário atual
 * - Centralizar a lógica de permissões (acessível de qualquer lugar)
 * - Controlar o logout (limpar a sessão)
 * 
 * Características:
 * - Utiliza padrão Singleton (apenas uma instância em memória)
 * - Todos os métodos são estáticos (acessíveis globalmente)
 * - A sessão é mantida enquanto a aplicação estiver rodando
 * 
 * @author joao
 */
package com.eletronica.util;

// Imports dos modelos
import com.eletronica.model.GrupoUsuarioEntity; // Dados do grupo/permissões do usuário
import com.eletronica.model.UsuarioEntity;      // Dados básicos do usuário

public class UsuarioSessao {
    
    // ==================== ATRIBUTOS DA SESSÃO ====================
    
    /**
     * USUÁRIO LOGADO ATUALMENTE
     * 
     * Armazena o objeto completo do usuário que fez login.
     * Contém informações como nome, email, ID, etc.
     * 
     * É static para que todas as partes da aplicação
     * possam acessar o mesmo usuário logado.
     */
    private static UsuarioEntity usuarioLogado;
    
    /**
     * GRUPO DO USUÁRIO LOGADO
     * 
     * Armazena o objeto completo do grupo ao qual o usuário pertence.
     * Contém todas as permissões do usuário (manterUsuario, manterServico, etc.)
     * 
     * É armazenado separadamente para acesso rápido às permissões,
     * evitando ter que navegar por usuarioLogado.getGrupo().
     */
    private static GrupoUsuarioEntity grupoLogado;
    
    // ==================== MÉTODOS DE CONTROLE DE SESSÃO ====================
    
    /**
     * REALIZA O LOGIN DO USUÁRIO (INICIA A SESSÃO)
     * 
     * Este método deve ser chamado APÓS a autenticação bem-sucedida.
     * Armazena os dados do usuário e do grupo para uso global.
     * 
     * @param usuario Objeto UsuarioEntity do usuário autenticado
     * @param grupo Objeto GrupoUsuarioEntity com as permissões do usuário
     */
    public static void login(UsuarioEntity usuario, GrupoUsuarioEntity grupo) {
        usuarioLogado = usuario;  // Guarda o usuário
        grupoLogado = grupo;      // Guarda as permissões
    }
    
    /**
     * REALIZA O LOGOUT (ENCERRA A SESSÃO)
     * 
     * Limpa os dados da sessão, efetivamente "deslogando" o usuário.
     * Deve ser chamado ao sair do sistema ou ao trocar de usuário.
     * 
     * Após o logout, isLogado() retornará false.
     */
    public static void logout() {
        usuarioLogado = null;  // Remove o usuário
        grupoLogado = null;    // Remove as permissões
    }
    
    // ==================== MÉTODOS DE ACESSO ====================
    
    /**
     * OBTÉM O USUÁRIO LOGADO ATUALMENTE
     * 
     * @return UsuarioEntity do usuário logado, ou null se não houver login
     */
    public static UsuarioEntity getUsuarioLogado() {
        return usuarioLogado;
    }
    
    /**
     * OBTÉM O GRUPO DO USUÁRIO LOGADO
     * 
     * @return GrupoUsuarioEntity do usuário logado, ou null se não houver login
     */
    public static GrupoUsuarioEntity getGrupoLogado() {
        return grupoLogado;
    }
    
    /**
     * VERIFICA SE EXISTE UM USUÁRIO LOGADO
     * 
     * @return true se há um usuário logado, false caso contrário
     */
    public static boolean isLogado() {
        return usuarioLogado != null;  // Se existe usuário, está logado
    }
    
    // ==================== MÉTODOS DE VERIFICAÇÃO DE PERMISSÃO ====================
    // Estes métodos centralizam toda a lógica de autorização do sistema.
    // Qualquer parte do sistema pode perguntar "o usuário logado pode fazer X?"
    
    /**
     * VERIFICA SE O USUÁRIO PODE GERENCIAR USUÁRIOS E GRUPOS
     * 
     * Retorna true apenas se:
     * - Há um usuário logado (grupoLogado != null)
     * - O grupo do usuário tem a permissão 'manterUsuario' ativa
     * 
     * @return true se pode gerenciar usuários/grupos, false caso contrário
     */
    public static boolean podeManterUsuario() {
        return grupoLogado != null && grupoLogado.isManterUsuario();
    }
    
    /**
     * VERIFICA SE O USUÁRIO PODE GERENCIAR SERVIÇOS
     * 
     * Retorna true apenas se:
     * - Há um usuário logado (grupoLogado != null)
     * - O grupo do usuário tem a permissão 'manterServico' ativa
     * 
     * @return true se pode gerenciar serviços, false caso contrário
     */
    public static boolean podeManterServico() {
        return grupoLogado != null && grupoLogado.isManterServico();
    }
    
    /**
     * VERIFICA SE O USUÁRIO PODE DELETAR ORDEM DE SERVIÇO
     * 
     * Regra de negócio: APENAS ADMINISTRADOR pode deletar OS.
     * O Administrador é identificado pelo código do grupo = 1.
     * 
     * @return true se é administrador (grupo == 1), false caso contrário
     */
    public static boolean podeDeletarOS() {
        return grupoLogado != null && grupoLogado.getGrupo() == 1;
    }
    
    /**
     * VERIFICA SE O USUÁRIO PODE EDITAR ORDEM DE SERVIÇO
     * 
     * Regra de negócio:
     * - Administrador (grupo 1) pode editar
     * - Técnico (grupo 2) pode editar
     * 
     * @return true se é administrador ou técnico, false caso contrário
     */
    public static boolean podeEditarOS() {
        return grupoLogado != null && (grupoLogado.getGrupo() == 1 || grupoLogado.getGrupo() == 2);
    }
    
    /**
     * VERIFICA SE O USUÁRIO PODE CRIAR ORDEM DE SERVIÇO
     * 
     * Regra de negócio: qualquer usuário com permissão básica (permissao = true)
     * pode criar OS. Isso inclui Administrador, Técnico e Recepcionista.
     * Visitantes (permissao = false) NÃO podem criar OS.
     * 
     * @return true se o grupo tem permissão básica ativa
     */
    public static boolean podeCriarOS() {
        return grupoLogado != null && grupoLogado.isPermissao();
    }
    
    /**
     * VERIFICA SE O BOTÃO DELETAR DEVE SER EXIBIDO
     * 
     * Este método é usado na interface gráfica para decidir
     * se o botão "Deletar" deve ser mostrado ou escondido.
     * 
     * @return true se pode deletar OS
     */
    public static boolean podeVerBotaoDeletar() {
        return podeDeletarOS();
    }
    
    /**
     * VERIFICA SE O BOTÃO EDITAR DEVE SER EXIBIDO
     * 
     * Este método é usado na interface gráfica para decidir
     * se o botão "Editar" deve ser mostrado ou escondido.
     * 
     * @return true se pode editar OS
     */
    public static boolean podeVerBotaoEditar() {
        return podeEditarOS();
    }
}
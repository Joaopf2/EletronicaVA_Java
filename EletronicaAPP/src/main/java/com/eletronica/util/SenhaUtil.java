/**
 * UTILITÁRIO DE SEGURANÇA DE SENHAS (BCrypt)
 *
 * Centraliza a geração e a verificação de hashes de senha.
 * As senhas NUNCA são armazenadas em texto puro no banco: apenas o hash BCrypt
 * (60 caracteres, formato "$2a$10$...") que já embute o salt aleatório.
 *
 * Compatibilidade com dados legados:
 * - Bancos antigos possuem senhas em texto puro. verificar() aceita esse formato
 *   para permitir o login, e precisaRehash() indica que a senha deve ser
 *   convertida para BCrypt (o que é feito no login e na migração de inicialização).
 *
 * @author joao
 */
package com.eletronica.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

public final class SenhaUtil {

    /** Formato de um hash BCrypt: $2a$ / $2b$ / $2y$ + custo + 53 caracteres (salt + hash). */
    private static final Pattern FORMATO_BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private static final int CUSTO_MIN = 4;
    private static final int CUSTO_MAX = 31;

    private SenhaUtil() {
        // Classe utilitária - não deve ser instanciada
    }

    /**
     * GERA O HASH BCRYPT DE UMA SENHA EM TEXTO PURO
     *
     * @param senhaPura Senha digitada pelo usuário
     * @return Hash BCrypt pronto para ser gravado no banco
     */
    public static String hash(String senhaPura) {
        if (senhaPura == null || senhaPura.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser vazia.");
        }
        int custo = Math.max(CUSTO_MIN, Math.min(CUSTO_MAX, AppConfig.getInt("security.bcrypt.cost", 10)));
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(custo));
    }

    /**
     * VERIFICA SE A SENHA DIGITADA CORRESPONDE À SENHA ARMAZENADA
     *
     * @param senhaPura Senha digitada no login
     * @param armazenada Valor gravado no banco (hash BCrypt ou texto puro legado)
     * @return true se a senha confere
     */
    public static boolean verificar(String senhaPura, String armazenada) {
        if (senhaPura == null || armazenada == null || armazenada.isEmpty()) {
            return false;
        }
        if (isHash(armazenada)) {
            // jBCrypt só reconhece o prefixo $2a$; $2b$/$2y$ usam o mesmo algoritmo
            String normalizado = "$2a$" + armazenada.substring(4);
            try {
                return BCrypt.checkpw(senhaPura, normalizado);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        // Senha legada em texto puro: comparação em tempo constante
        return MessageDigest.isEqual(
            senhaPura.getBytes(StandardCharsets.UTF_8),
            armazenada.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Indica se o valor armazenado já é um hash BCrypt.
     */
    public static boolean isHash(String valor) {
        return valor != null && FORMATO_BCRYPT.matcher(valor).matches();
    }

    /**
     * Indica se a senha armazenada ainda está em texto puro e deve ser convertida.
     */
    public static boolean precisaRehash(String armazenada) {
        return armazenada != null && !armazenada.isEmpty() && !isHash(armazenada);
    }
}

/**
 * CONFIGURAÇÃO EXTERNALIZADA DA APLICAÇÃO
 *
 * Centraliza a leitura das configurações (credenciais do banco, pool de conexões,
 * custo do BCrypt...) para que nenhuma credencial fique fixa no código-fonte.
 *
 * Ordem de prioridade (a última fonte encontrada sobrescreve as anteriores):
 *   1. src/main/resources/application.properties   (valores padrão, versionado no Git)
 *   2. Arquivo externo local (NÃO versionado):
 *        - caminho informado em -Dapp.config=... ou na variável ELETRONICA_CONFIG; ou
 *        - ./application-local.properties (diretório de execução do app)
 *   3. Variáveis de ambiente: DB_URL, DB_USER, DB_PASSWORD
 *   4. Propriedades de sistema da JVM: -Ddb.url=... -Ddb.user=... -Ddb.password=...
 *
 * @author joao
 */
package com.eletronica.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public final class AppConfig {

    /** Arquivo padrão dentro do classpath (resources). */
    private static final String ARQUIVO_PADRAO = "/application.properties";

    /** Arquivo local opcional, fora do Git, com as credenciais reais. */
    private static final String ARQUIVO_LOCAL = "application-local.properties";

    /** Mapeamento: variável de ambiente → chave de configuração. */
    private static final Map<String, String> VARIAVEIS_AMBIENTE = Map.of(
        "DB_URL", "db.url",
        "DB_USER", "db.user",
        "DB_PASSWORD", "db.password"
    );

    private static final Properties PROPS = carregar();

    private AppConfig() {
        // Classe utilitária - não deve ser instanciada
    }

    // ==================== CARREGAMENTO ====================

    private static Properties carregar() {
        Properties props = new Properties();

        // 1. Valores padrão do classpath
        try (InputStream in = AppConfig.class.getResourceAsStream(ARQUIVO_PADRAO)) {
            if (in != null) {
                try (Reader reader = new java.io.InputStreamReader(in, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
            }
        } catch (IOException e) {
            System.err.println("[AppConfig] Falha ao ler " + ARQUIVO_PADRAO + ": " + e.getMessage());
        }

        // 2. Arquivo externo local
        Path externo = localizarArquivoExterno();
        if (externo != null) {
            try (Reader reader = Files.newBufferedReader(externo, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                System.err.println("[AppConfig] Falha ao ler " + externo + ": " + e.getMessage());
            }
        }

        // 3. Variáveis de ambiente
        VARIAVEIS_AMBIENTE.forEach((env, chave) -> {
            String valor = System.getenv(env);
            if (valor != null && !valor.isBlank()) {
                props.setProperty(chave, valor);
            }
        });

        // 4. Propriedades de sistema (-Dchave=valor) para chaves conhecidas
        for (String chave : props.stringPropertyNames()) {
            String valor = System.getProperty(chave);
            if (valor != null) {
                props.setProperty(chave, valor);
            }
        }
        for (String chave : VARIAVEIS_AMBIENTE.values()) {
            String valor = System.getProperty(chave);
            if (valor != null) {
                props.setProperty(chave, valor);
            }
        }

        return props;
    }

    private static Path localizarArquivoExterno() {
        String caminho = System.getProperty("app.config");
        if (caminho == null || caminho.isBlank()) {
            caminho = System.getenv("ELETRONICA_CONFIG");
        }
        if (caminho != null && !caminho.isBlank()) {
            Path p = Paths.get(caminho);
            if (Files.isReadable(p)) {
                return p;
            }
            System.err.println("[AppConfig] Arquivo de configuração não encontrado: " + p.toAbsolutePath());
            return null;
        }
        Path local = Paths.get(ARQUIVO_LOCAL);
        return Files.isReadable(local) ? local : null;
    }

    // ==================== ACESSO ====================

    public static String get(String chave, String padrao) {
        String valor = PROPS.getProperty(chave);
        return (valor == null || valor.isBlank()) ? padrao : valor.trim();
    }

    public static String get(String chave) {
        return get(chave, null);
    }

    public static int getInt(String chave, int padrao) {
        String valor = get(chave);
        if (valor == null) {
            return padrao;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            System.err.println("[AppConfig] Valor inválido para " + chave + ": '" + valor + "'. Usando " + padrao);
            return padrao;
        }
    }

    /**
     * Retorna a senha sem aplicar trim (senhas podem conter espaços).
     */
    public static String getSenhaBanco() {
        String valor = PROPS.getProperty("db.password");
        return valor == null ? "" : valor;
    }
}

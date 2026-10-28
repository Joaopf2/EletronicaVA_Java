/**
 * CLASSE DE CONEXÃO COM O BANCO DE DADOS
 * 
 * Esta classe é responsável por gerenciar o acesso ao PostgreSQL através
 * de um POOL DE CONEXÕES (HikariCP).
 * 
 * Funcionalidades:
 * - Criar (sob demanda) um pool de conexões reutilizáveis com o PostgreSQL
 * - Entregar conexões do pool para os DAOs
 * - Encerrar o pool quando o sistema for fechado
 * 
 * IMPORTANTE - USO CORRETO:
 * Toda conexão obtida com getConnection() DEVE ser fechada pelo chamador
 * (de preferência com try-with-resources). Fechar uma conexão do pool NÃO
 * encerra o vínculo com o banco: apenas devolve a conexão ao pool para reuso.
 * 
 *     try (Connection conn = Database.getConnection();
 *          PreparedStatement stmt = conn.prepareStatement(sql)) { ... }
 * 
 * Configurações do banco:
 * - NÃO ficam mais fixas no código. São lidas pela classe AppConfig a partir de
 *   application.properties / application-local.properties / variáveis de
 *   ambiente (DB_URL, DB_USER, DB_PASSWORD).
 * 
 * @author joao
 */
package com.eletronica.util;

// Imports Java padrão para conexão com banco de dados
import java.sql.Connection;          // Representa a conexão com o banco
import java.sql.SQLException;       // Exceção específica para erros de banco

// Pool de conexões
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Database {
    
    // ==================== ATRIBUTO SINGLETON ====================
    
    /**
     * POOL DE CONEXÕES ÚNICO (SINGLETON)
     * 
     * Mantém a única instância do pool durante toda a execução do programa.
     * É criado de forma preguiçosa (lazy) na primeira chamada a getConnection().
     */
    private static HikariDataSource dataSource = null;
    
    private Database() {
        // Classe utilitária - não deve ser instanciada
    }
    
    // ==================== MÉTODO PARA OBTER CONEXÃO ====================
    
    /**
     * OBTÉM UMA CONEXÃO DO POOL
     * 
     * - Se o pool ainda não existe (ou foi encerrado), cria um novo
     * - Retorna uma conexão livre do pool
     * 
     * O chamador é responsável por fechar a conexão (try-with-resources),
     * devolvendo-a ao pool.
     * 
     * @return Connection ativa com o PostgreSQL
     * @throws SQLException Se não conseguir conectar ao banco
     *         (ex: banco desligado, credenciais erradas, URL inválida)
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    /**
     * Cria o pool sob demanda (thread-safe).
     */
    private static synchronized HikariDataSource getDataSource() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setPoolName("EletronicaPool");
            config.setJdbcUrl(AppConfig.get("db.url", "jdbc:postgresql://localhost:5432/eletronicadb"));
            config.setUsername(AppConfig.get("db.user", "postgres"));
            config.setPassword(AppConfig.getSenhaBanco());
            config.setMaximumPoolSize(AppConfig.getInt("db.pool.maximumPoolSize", 5));
            config.setMinimumIdle(AppConfig.getInt("db.pool.minimumIdle", 1));
            config.setConnectionTimeout(AppConfig.getInt("db.pool.connectionTimeoutMs", 5000));
            config.setIdleTimeout(AppConfig.getInt("db.pool.idleTimeoutMs", 300000));
            config.setMaxLifetime(AppConfig.getInt("db.pool.maxLifetimeMs", 1800000));
            
            try {
                dataSource = new HikariDataSource(config);
            } catch (RuntimeException e) {
                // HikariCP lança PoolInitializationException (RuntimeException) quando
                // o banco está inacessível. Convertemos para SQLException para que as
                // telas tratem o erro da mesma forma que qualquer outro erro de banco.
                Throwable causa = e.getCause() != null ? e.getCause() : e;
                throw new SQLException("Não foi possível conectar ao banco de dados: " + causa.getMessage(), causa);
            }
        }
        return dataSource;
    }
    
    // ==================== MÉTODO PARA FECHAR CONEXÃO ====================
    
    /**
     * ENCERRA O POOL DE CONEXÕES COM O BANCO DE DADOS
     * 
     * Este método deve ser chamado quando a aplicação for encerrada
     * para liberar recursos do banco (fecha todas as conexões do pool).
     * 
     * O nome foi mantido por compatibilidade com o restante do sistema.
     * Caso getConnection() seja chamado depois, um novo pool é criado.
     */
    public static synchronized void closeConnection() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (RuntimeException e) {
                // Não lança exceção para não interromper o fechamento da aplicação
                e.printStackTrace();
            } finally {
                dataSource = null;
            }
        }
    }
}
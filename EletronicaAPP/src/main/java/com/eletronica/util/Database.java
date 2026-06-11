/**
 * CLASSE DE CONEXÃO COM O BANCO DE DADOS
 * 
 * Esta classe é responsável por gerenciar a conexão com o PostgreSQL.
 * Utiliza o padrão Singleton para garantir que apenas uma conexão
 * seja criada durante toda a execução do sistema.
 * 
 * Funcionalidades:
 * - Estabelecer conexão com o banco de dados PostgreSQL
 * - Reutilizar a mesma conexão em toda a aplicação (Singleton)
 * - Fornecer um método para fechar a conexão quando o sistema encerrar
 * 
 * Configurações do banco:
 * - Host: localhost (mesma máquina)
 * - Porta: 5432 (padrão do PostgreSQL)
 * - Database: eletronicadb
 * - Usuário: postgres
 * - Senha: 1234
 * 
 * @author joao
 */
package com.eletronica.util;

// Imports Java padrão para conexão com banco de dados
import java.sql.Connection;          // Representa a conexão com o banco
import java.sql.DriverManager;      // Gerencia drivers JDBC e cria conexões
import java.sql.SQLException;       // Exceção específica para erros de banco

public class Database {
    
    // ==================== ATRIBUTO SINGLETON ====================
    
    /**
     * CONEXÃO ÚNICA (SINGLETON)
     * 
     * Atributo estático que mantém a única instância da conexão
     * durante toda a execução do programa.
     * 
     * O modificador 'static' significa que esta variável pertence à classe,
     * não a uma instância específica. Assim, todas as partes do sistema
     * compartilham a MESMA conexão.
     */
    private static Connection connection = null;
    
    // ==================== MÉTODO PARA OBTER CONEXÃO ====================
    
    /**
     * OBTÉM A CONEXÃO COM O BANCO DE DADOS (SINGLETON)
     * 
     * Este método implementa o padrão Singleton Lazy Loading:
     * - Se não há conexão ou ela está fechada, cria uma nova
     * - Se já existe uma conexão aberta, reutiliza a mesma
     * 
     * Vantagens:
     * - Evita criar múltiplas conexões desnecessárias
     * - Melhora a performance (menos overhead de conexão)
     * - Centraliza a configuração do banco em um único lugar
     * 
     * @return Connection ativa com o PostgreSQL
     * @throws SQLException Se não conseguir conectar ao banco
     *         (ex: banco desligado, credenciais erradas, URL inválida)
     */
    public static Connection getConnection() throws SQLException {
        // Verifica se a conexão não existe OU está fechada/inválida
        if (connection == null || connection.isClosed()) {
            // Cria uma NOVA conexão usando os parâmetros do PostgreSQL
            // Formato da URL: jdbc:postgresql://host:porta/nome_do_banco
            connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/eletronicadb",  // URL do banco
                "postgres",                                       // Usuário
                "1234"                                            // Senha
            );
        }
        return connection;  // Retorna a conexão (nova ou existente)
    }
    
    // ==================== MÉTODO PARA FECHAR CONEXÃO ====================
    
    /**
     * FECHA A CONEXÃO COM O BANCO DE DADOS
     * 
     * Este método deve ser chamado quando a aplicação for encerrada
     * para liberar recursos do banco.
     * 
     * É uma boa prática fechar a conexão para:
     * - Liberar recursos do servidor PostgreSQL
     * - Evitar vazamento de memória
     * - Manter a performance do sistema
     * 
     * O método verifica se a conexão existe antes de tentar fechá-la
     * para evitar NullPointerException.
     */
    public static void closeConnection() {
        // Verifica se existe uma conexão ativa
        if (connection != null) {
            try {
                connection.close();      // Fecha a conexão
                connection = null;       // Limpa a referência (importante!)
            } catch (SQLException e) {
                // Em caso de erro, imprime o stack trace para debug
                // Mas não lança exceção para não interromper o fechamento da aplicação
                e.printStackTrace();
            }
        }
    }
}
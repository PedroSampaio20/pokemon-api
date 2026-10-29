package br.com.fiap.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexões com o banco de dados Oracle (aula 40).
 *
 * Segue os padrões de projeto Factory (centraliza a criação da conexão) e
 * Singleton (reaproveita uma única conexão enquanto ela estiver aberta).
 *
 * As credenciais NÃO ficam no código (aula 44): são lidas das variáveis de ambiente
 * DB_URL, DB_USER e DB_PASSWORD. Localmente elas são definidas no terminal/IntelliJ e,
 * no deploy, no painel "Environment" do Render.
 */
public class ConnectionFactory {

    private static Connection connection;

    public static Connection getConnection() {
        try {
            // se já existe uma conexão aberta, reaproveita (Singleton)
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Busca dados de conexão nas variáveis de ambiente
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("Variáveis de ambiente do banco não configuradas!");
            }

            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Erro nome da classe: " + e.getMessage());
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }
}

package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;

    private static final String dbName = "search_engine";
    private static final String dbUser = "root";
    private static final String dbPass = "PasswordforMySQL.2022";

    public static void initDb() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, dbUser, dbPass);
                createPageTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            throw new IllegalArgumentException("Call initDb() method before getConnection()");
        }
        return connection;
    }

    private static void createPageTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS page");
        connection.createStatement().execute("CREATE TABLE page(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "path TEXT NOT NULL, " +
                "code INT NOT NULL, " +
                "content MEDIUMTEXT NOT NULL, " +
                "PRIMARY KEY(id))");
                //UNIQUE KEY path_key (path(50))
        connection.createStatement().execute("CREATE INDEX page_index ON page (path(50));");
    }


}

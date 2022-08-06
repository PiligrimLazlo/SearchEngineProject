package main.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBCreator {

    private static Connection connection;

    private static final String dbName = "search_engine";
    private static final String dbUser = "root";
    private static final String dbPass = "PasswordforMySQL.2022";

    public static void initDb() {

        try {
            if (connection == null) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, dbUser, dbPass);
            }
            dropAllTables();

            createSiteTable();

            createPageTable();
            createFieldTable();

            insertInFieldInitialValues();

            createLemmaTable();
            createIndexTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static Connection getConnection() {
        if (connection == null) {
            throw new IllegalArgumentException("Call initDb() method before getConnection()");
        }
        return connection;
    }

    private static void createPageTable() throws SQLException {
        boolean result = connection.createStatement().execute("CREATE TABLE IF NOT EXISTS page(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "path TEXT NOT NULL, " +
                "code INT NOT NULL, " +
                "content MEDIUMTEXT NOT NULL, " +
                "site_id INT NOT NULL, " +
                "FOREIGN KEY (site_id) REFERENCES site(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "PRIMARY KEY(id))");
        if (result)
            connection.createStatement().execute("CREATE INDEX page_index ON page (path(50));");
    }

    private static void createFieldTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS field");
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS field(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "name VARCHAR(255) NOT NULL, " +
                "selector VARCHAR(255) NOT NULL, " +
                "weight FLOAT NOT NULL, " +
                "PRIMARY KEY(id))");
    }

    private static void insertInFieldInitialValues() throws SQLException {
        connection.createStatement().execute(
                "INSERT INTO field(name, selector, weight) VALUES " +
                        "('title', 'title', 1.0), " +
                        "('body', 'body', 0.8)");
    }

    private static void createLemmaTable() throws SQLException {
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS lemma(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "lemma VARCHAR(255) NOT NULL, " +
                "frequency INT NOT NULL, " +
                "site_id INT NOT NULL, " +
                "FOREIGN KEY (site_id) REFERENCES site(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "PRIMARY KEY(id))");
    }

    private static void createIndexTable() throws SQLException {
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `index`(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "page_id INT NOT NULL, " +
                "lemma_id INT NOT NULL, " +
                "`rank` FLOAT NOT NULL, " +
                "PRIMARY KEY(id), " +
                "FOREIGN KEY (page_id) REFERENCES page(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (lemma_id) REFERENCES lemma(id) ON UPDATE CASCADE ON DELETE CASCADE)");
    }

    private static void createSiteTable() throws SQLException {
        connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `site` (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "`status` ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL, " +
                "status_time DATETIME NOT NULL, " +
                "last_error TEXT, " +
                "url VARCHAR(255) NOT NULL, " +
                "`name` VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY(id))");
    }

    public static void dropAllTables() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS `index`, lemma, field, page, `site`");
    }

    public static void removeFromPageTable(int siteId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM `page` WHERE site_id=?");
        statement.setInt(1, siteId);
        statement.execute();
    }

    public static void removeFromLemmaTable(int siteId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM `lemma` WHERE site_id=?");
        statement.setInt(1, siteId);
        statement.execute();
    }


}

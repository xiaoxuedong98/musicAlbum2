package com.cs6650.album.server;

import com.cs6650.album.server.bean.AlbumInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;

import java.sql.*;

public class AlbumsDatabase {

    private static final Logger logger = Logger.getLogger(AlbumsDatabase.class);
    private static HikariDataSource dataSource;
    static {
        initDatabasePool();
        try {
            initDatabase();
        } catch (SQLException e) {
            logger.error("Failed to initialize the database!", e);
        }
    }

    public AlbumInfo getAlbumInfoById(int albumId) throws SQLException {
        try (Connection con = getRemoteConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM album_info WHERE album_id = ?")) {

            stmt.setInt(1, albumId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String artist = rs.getString("artist");
                    String title = rs.getString("title");
                    String year = rs.getString("year");
                    return new AlbumInfo(artist, title, year);
                } else {
                    return null;
                }
            }
        }
    }

    public int insertNewAlbum(AlbumInfo albumInfo, long imageSize) throws SQLException {
        try (Connection con = getRemoteConnection()) {

            // Step 1: Insert the album
            String insertSQL = "INSERT INTO album_info (artist, title, year, image_size) VALUES (?,?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(insertSQL)) {
                stmt.setString(1, albumInfo.getArtist());
                stmt.setString(2, albumInfo.getTitle());
                stmt.setString(3, albumInfo.getYear());
                stmt.setLong(3, imageSize);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Failed to insert album info.");
                }
            }

            // Step 2: Retrieve the ID of the inserted album
            String selectLastInsertId = "SELECT LAST_INSERT_ID()";
            try (PreparedStatement stmt = con.prepareStatement(selectLastInsertId);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to retrieve last inserted album ID.");
                }
            }
        }
    }




    private static void initDatabasePool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + System.getProperty("RDS_HOSTNAME") + ":" + System.getProperty("RDS_PORT") + "/" + System.getProperty("RDS_DB_NAME"));
        config.setUsername(System.getProperty("RDS_USERNAME"));
        config.setPassword(System.getProperty("RDS_PASSWORD"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(100); // set this based on your needs
        config.setMinimumIdle(50); // optional: set this based on your needs
        config.setPoolName("AlbumsDBPool");
        dataSource = new HikariDataSource(config);
    }

    private static void initDatabase() throws SQLException {
        Connection con = getRemoteConnection();
        if (con != null) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "album_info", null);
            if (tables.next()) {
                PreparedStatement dropStmt = con.prepareStatement("DROP TABLE album_info;");
                dropStmt.execute();
                logger.info("Dropped existing table album_info.");
            }
            PreparedStatement stmt = con.prepareStatement(
                "CREATE TABLE album_info (" +
                    "album_id INT NOT NULL AUTO_INCREMENT, " +
                    "artist VARCHAR(255) NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "year VARCHAR(4) NOT NULL, " +
                    "image_size BIGINT NOT NULL, " +
                    "PRIMARY KEY (album_id));"
            );
            stmt.execute();
            logger.info("Created table album_info.");

        }
        }

    private static Connection getRemoteConnection() {
//        if (System.getProperty("RDS_HOSTNAME") != null) {
//            try {
//                Class.forName("com.mysql.cj.jdbc.Driver");
//                String dbName = System.getProperty("RDS_DB_NAME");
//                String userName = System.getProperty("RDS_USERNAME");
//                String password = System.getProperty("RDS_PASSWORD");
//                String hostname = System.getProperty("RDS_HOSTNAME");
//                String port = System.getProperty("RDS_PORT");
//                String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
//                logger.trace("Getting remote connection with connection string from environment variables.");
//                Connection con = DriverManager.getConnection(jdbcUrl);
//                logger.info("Remote connection successful.");
//                return con;
//            }
//            catch (ClassNotFoundException e) { logger.warn(e.toString());}
//            catch (SQLException e) { logger.warn(e.toString());}
//        }
//        return null;
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            logger.warn("Failed to get connection from pool", e);
            return null;
        }
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }


}

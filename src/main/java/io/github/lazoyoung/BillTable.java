package io.github.lazoyoung;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BillTable extends Table {
    
    public BillTable(Connector connector) {
        super(connector);
    }
    
    @Override
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS " + Config.DATABASE.get().getString("mysql.tables.bill") +
                " (" +
                "id binary(16) PRIMARY KEY, " +
                "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                "unit INT NOT NULL, " +
                "birth TIMESTAMP, " +
                "origin VARCHAR(30) COMMENT 'Can be a player or the server.'" +
                ");";
        executeUpdate((result, thrown) -> {}, sql);
        execute("SET NAMES 'utf8';", null);
        
        Connection con = null;
        Statement stmt = null;
        try {
            con = getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(");
            con.createStatement().execute("SET NAMES 'utf8';");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null)
                    con.close();
                if (stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
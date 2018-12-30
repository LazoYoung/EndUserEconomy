package io.github.lazoyoung.database;

import io.github.lazoyoung.Config;
import io.github.lazoyoung.Connector;
import io.github.lazoyoung.Table;
import io.github.lazoyoung.bill.BillFactory;

public class BillTable extends Table {

    private String tableName;

    public BillTable(Connector connector) {
        super(connector);
    }
    
    @Override
    protected void init() {
        this.tableName = Config.DATABASE.get().getString("mysql.tables.bill");
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" +
                "id binary(16) PRIMARY KEY, " +
                "economy VARCHAR(30) NOT NULL COMMENT 'Identical to enums in Economy.java.', " +
                "currency VARCHAR(15) COMMENT 'This is null for those single-currency economies.', " +
                "unit INT NOT NULL, " +
                "birth TIMESTAMP, " +
                "origin VARCHAR(30) COMMENT 'Can be a player or the server.'" +
                ");";
        executeUpdate(null, sql);
        execute(null, "SET NAMES 'utf8';");
    }

    public void addRecord(BillFactory factory, String origin) {
        String economy = factory.getCurrency().getEconomy().toString();
        String currency = factory.getCurrency().getName();
        // TODO convert to new method : executeUpdate();

        /*
        Connection con = null;
        PreparedStatement preStmt = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
            con = getConnector().getConnection();
            con.createStatement().execute("SET @uuid = UUID()");
            preStmt = con.prepareStatement("INSERT INTO " + tableName + " (id, economy, currency, unit, birth, origin)" +
                    " VALUES (UUID_TO_BIN(@uuid, 1), ?, ?, ?, CURRENT_TIMESTAMP, ?);");
            preStmt.setString(1, economy);
            preStmt.setString(2, currency);
            preStmt.setInt(3, unit);
            preStmt.setString(4, origin);

            if (preStmt.executeUpdate() > 0) {
                stmt = con.createStatement();
                resultSet = stmt.executeQuery("SELECT @uuid;");
                if (resultSet.next()) {
                    uniqueId = UUID.fromString(resultSet.getString(1));
                    return this;
                }
                throw new SQLException("Unable to select the generated ID.");
            }
            throw new SQLException("Unable to insert a row.");
        }
        */
    }

}
package com.clickhouse.examples.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Properties;

public class Basic {
    static final String TABLE_NAME = "jdbc_example_basic";

    private static Connection getConnection(String url) throws SQLException {
        return getConnection(url, new Properties());
    }

    private static Connection getConnection(String url, Properties properties) throws SQLException {
        final Connection conn;
        // Driver driver = new ClickHouseDriver();
        // conn = driver.connect(url, properties);

        // ClickHouseDataSource dataSource = new ClickHouseDataSource(url, properties);
        // conn = dataSource.getConnection();

        conn = DriverManager.getConnection(url, properties);
        System.out.println("Connected to: " + conn.getMetaData().getURL());
        return conn;
    }

    static int dropAndCreateTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // multi-statement query is supported by default
            // session will be created automatically during execution
            stmt.execute(String.format(
                    "drop table if exists %1$s; create table %1$s(a String, b Nullable(String)) engine=Memory",
                    TABLE_NAME));
            return stmt.getUpdateCount();
        }
    }

    static int batchInsert(Connection conn) throws SQLException {
        // 1. NOT recommended when inserting lots of rows, because it's based on a large statement
        String sql = String.format("insert into %s values(? || ' - 1', ?)", TABLE_NAME);
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "a");
            ps.setString(2, "b");
            ps.addBatch();
            ps.setString(1, "c");
            // ps.setNull(2, Types.VARCHAR);
            // ps.setObject(2, null);
            ps.setString(2, null);
            ps.addBatch();
            // same as below query:
            // insert into <table> values ('a' || ' - 1', 'b'), ('c' || ' - 1', null)
            for (int i : ps.executeBatch()) {
                if (i > 0) {
                    count += i;
                }
            }
        }

        // 2. faster and ease of use, with additional query for getting table structure
        // sql = String.format("insert into %s (a)", TABLE_NAME);
        // sql = String.format("insert into %s (a) values (?)", TABLE_NAME);
        sql = String.format("insert into %s (* except b)", TABLE_NAME);
        // Note: below query will be issued to get table structure:
        // select * except b from <table> where 0
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // implicit type conversion: int -> String
            ps.setInt(1, 1);
            ps.addBatch();
            // implicit type conversion: LocalDateTime -> string
            ps.setObject(1, LocalDateTime.now());
            ps.addBatch();
            // same as below query:
            // insert into <table> format RowBinary <binary data>
            for (int i : ps.executeBatch()) {
                if (i > 0) {
                    count += i;
                }
            }
        }

        // 3. fastest but inconvenient and NOT portable(as it's limited to ClickHouse)
        // see https://clickhouse.com/docs/en/sql-reference/table-functions/input/
        sql = String.format("insert into %s select a, b from input('a String, b Nullable(String)')", TABLE_NAME);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "a");
            ps.setString(2, "b");
            ps.addBatch();
            ps.setString(1, "c");
            ps.setString(2, null);
            ps.addBatch();
            // same as below query:
            // insert into <table> format RowBinary <binary data>
            for (int i : ps.executeBatch()) {
                if (i > 0) {
                    count += i;
                }
            }
        }

        return count;
    }

    static int connectWithCustomSettings(String url) throws SQLException {
        // comma separated settings
        String customSettings = "session_check=0,max_query_size=100";
        Properties properties = new Properties();
        // properties.setProperty(ClickHouseHttpClientOption.CUSTOM_PARAMS.getKey(),
        // customSettings);
        properties.setProperty("custom_http_params", customSettings); // limited to http protocol
        try (Connection conn = getConnection(url, properties);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select 5")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    static int query(Connection conn) throws SQLException {
        String sql = "select * from " + TABLE_NAME;
        try (Statement stmt = conn.createStatement()) {
            // set max_result_rows = 3, result_overflow_mode = 'break'
            // or simply discard rows after the first 3 in read-only mode
            stmt.setMaxRows(3);
            int count = 0;
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    count++;
                }
            }
            return count;
        }
    }

    public static void main(String[] args) {
        // jdbc:ch:https://explorer@play.clickhouse.com:443
        // jdbc:ch:https://demo:demo@github.demo.trial.altinity.cloud
        String url = System.getProperty("chUrl", "jdbc:ch://localhost");

        try (Connection conn = getConnection(url)) {
            connectWithCustomSettings(url);

            System.out.println("Update Count: " + dropAndCreateTable(conn));
            System.out.println("Inserted Rows: " + batchInsert(conn));
            System.out.println("Result Rows: " + query(conn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

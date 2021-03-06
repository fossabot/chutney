package com.chutneytesting.task.sql.core;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlClient {

    private final HikariDataSource dataSource;

    public SqlClient(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Records execute(String query) throws SQLException {
        final Records records;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            try (final Statement statement = connection.createStatement()) {
                statement.execute(query);
                records = StatementConverter.createRecords(statement);
            }
        } finally {
            silentClose(connection);
        }

        return records;
    }

    public void closeDatasource() {
        this.dataSource.close();
    }

    public Records emptyRecords() {
        return new Records(0, Collections.emptyList(), Collections.emptyList());
    }

    private void silentClose(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                //Silent close
            }
        }
    }

    private static class StatementConverter {

        private static Records createRecords(Statement statement) throws SQLException {
            final int affectedRows = statement.getUpdateCount();
            List<String> headers = Collections.emptyList();
            List<List<Object>> rows = Collections.emptyList();

            if (isSelectQuery(affectedRows)) {
                try (final ResultSet rs = statement.getResultSet()) {
                    final ResultSetMetaData md = rs.getMetaData();

                    headers = createHeaders(md, md.getColumnCount());
                    rows = createRows(rs, md.getColumnCount());
                }
            }

            return new Records(affectedRows, headers, rows);
        }

        private static boolean isSelectQuery(int affectedRows) {
            return affectedRows == -1;
        }

        private static List<String> createHeaders(ResultSetMetaData md, int columnCount) throws SQLException {
            final List<String> headers = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                headers.add(md.getColumnName(i));
            }
            return headers;
        }

        private static List<List<Object>> createRows(ResultSet rs, int columnCount) throws SQLException {
            final List<List<Object>> rows = new ArrayList<>();
            while (rs.next()) {
                final List<Object> row = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        }
    }
}

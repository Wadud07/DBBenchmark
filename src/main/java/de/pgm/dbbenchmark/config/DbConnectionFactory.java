package de.pgm.dbbenchmark.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionFactory {

    private static final String H2_URL =
            "jdbc:h2:tcp://localhost/~/h2db/test";

    private static final String HSQL_URL =
            "jdbc:hsqldb:hsql://localhost/testdb";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection(DbType dbType) throws SQLException {

        return switch (dbType) {

            case H2 ->
                DriverManager.getConnection(
                        H2_URL,
                        USER,
                        PASSWORD
                );

            case HSQLDB ->
                DriverManager.getConnection(
                        HSQL_URL,
                        USER,
                        PASSWORD
                );
        };
    }

}
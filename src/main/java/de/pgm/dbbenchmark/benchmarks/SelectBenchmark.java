package de.pgm.dbbenchmark.benchmarks;

import de.pgm.dbbenchmark.benchmark.Benchmark;
import de.pgm.dbbenchmark.benchmark.BenchmarkResult;
import de.pgm.dbbenchmark.config.DbType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class SelectBenchmark implements Benchmark {

    private static final int ROW_COUNT = 1000;

    private final DbType database;

    public SelectBenchmark(DbType database) {
        this.database = database;
    }

    @Override
    public String getName() {
        return "SELECT";
    }

    @Override
    public void setup(Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE IF EXISTS benchmark_select
            """);

            statement.execute("""
                CREATE TABLE benchmark_select(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(100)
                )
            """);
        }

        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO benchmark_select(id, name)
                VALUES (?, ?)
                """)) {

            for (int i = 1; i <= ROW_COUNT; i++) {

                ps.setInt(1, i);
                ps.setString(2, "User " + i);

                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    @Override
    public BenchmarkResult run(Connection connection) throws Exception {

        long start = System.nanoTime();

        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT *
                FROM benchmark_select
                WHERE id = ?
                """)) {

            ps.setInt(1, 500);

            ps.executeQuery();
        }

        long end = System.nanoTime();

        return new BenchmarkResult(
                database,
                getName(),
                end - start
        );
    }

    @Override
    public void cleanup(Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE IF EXISTS benchmark_select
            """);
        }
    }
}
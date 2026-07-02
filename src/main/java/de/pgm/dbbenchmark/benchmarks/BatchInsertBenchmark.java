package de.pgm.dbbenchmark.benchmarks;

import de.pgm.dbbenchmark.benchmark.Benchmark;
import de.pgm.dbbenchmark.benchmark.BenchmarkResult;
import de.pgm.dbbenchmark.config.DbType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class BatchInsertBenchmark implements Benchmark {

    private static final int ROW_COUNT = 1000;

    private final DbType database;
    private int nextId = 1;

    public BatchInsertBenchmark(DbType database) {
        this.database = database;
    }

    @Override
    public String getName() {
        return "BATCH INSERT (" + ROW_COUNT + " rows)";
    }

    @Override
    public void setup(Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE IF EXISTS benchmark_batch
            """);

            statement.execute("""
                CREATE TABLE benchmark_batch(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(100)
                )
            """);

        }

        nextId = 1;
    }

    @Override
    public BenchmarkResult run(Connection connection) throws Exception {

        long start = System.nanoTime();

        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO benchmark_batch(id, name)
                VALUES (?, ?)
                """)) {

            for (int i = 0; i < ROW_COUNT; i++) {

                int id = nextId++;

                ps.setInt(1, id);
                ps.setString(2, "User " + id);

                ps.addBatch();
            }

            ps.executeBatch();
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
                DROP TABLE IF EXISTS benchmark_batch
            """);

        }
    }

}
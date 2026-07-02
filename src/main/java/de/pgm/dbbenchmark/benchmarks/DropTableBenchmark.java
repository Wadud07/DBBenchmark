package de.pgm.dbbenchmark.benchmarks;

import de.pgm.dbbenchmark.benchmark.Benchmark;
import de.pgm.dbbenchmark.benchmark.BenchmarkResult;
import de.pgm.dbbenchmark.config.DbType;

import java.sql.Connection;
import java.sql.Statement;

public class DropTableBenchmark implements Benchmark {

    private final DbType database;

    public DropTableBenchmark(DbType database) {
        this.database = database;
    }

    @Override
    public String getName() {
        return "DROP TABLE";
    }

    @Override
    public void setup(Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE IF EXISTS benchmark_drop
            """);

            statement.execute("""
                CREATE TABLE benchmark_drop(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(100)
                )
            """);
        }
    }

    @Override
    public BenchmarkResult run(Connection connection) throws Exception {

        long start = System.nanoTime();

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE benchmark_drop
            """);
        }

        long end = System.nanoTime();

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE benchmark_drop(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(100)
                )
            """);
        }

        return new BenchmarkResult(
                database,
                getName(),
                end - start
        );
    }

    @Override
    public void cleanup(Connection connection) throws Exception {
        // Nothing to clean up.
        // The table has already been dropped during the benchmark.
    }
}
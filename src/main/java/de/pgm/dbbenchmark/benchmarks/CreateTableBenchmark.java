package de.pgm.dbbenchmark.benchmarks;

import de.pgm.dbbenchmark.benchmark.Benchmark;
import de.pgm.dbbenchmark.benchmark.BenchmarkResult;
import de.pgm.dbbenchmark.config.DbType;

import java.sql.Connection;
import java.sql.Statement;

public class CreateTableBenchmark implements Benchmark {

    private final DbType database;

    public CreateTableBenchmark(DbType database) {
        this.database = database;
    }

    @Override
    public String getName() {
        return "CREATE TABLE";
    }

    @Override
    public BenchmarkResult run(Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE IF EXISTS benchmark_test
            """);

            long start = System.nanoTime();

            statement.execute("""
                CREATE TABLE benchmark_test(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(100)
                )
            """);

            long end = System.nanoTime();

            return new BenchmarkResult(
                    database,
                    getName(),
                    end - start
            );
        }
    }
}
package de.pgm.dbbenchmark.benchmarks;

import de.pgm.dbbenchmark.benchmark.Benchmark;
import de.pgm.dbbenchmark.benchmark.BenchmarkResult;
import de.pgm.dbbenchmark.config.DbType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class InsertBenchmark implements Benchmark {

    private final DbType database;
    private int nextId = 1;

    public InsertBenchmark(DbType database) {
        this.database = database;
    }

    @Override
    public String getName() {
        return "INSERT";
    }
    
    @Override
    public void setup(Connection connection) throws Exception {

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                DROP TABLE IF EXISTS benchmark_insert
            """);

            statement.execute("""
                CREATE TABLE benchmark_insert(
                    id INTEGER PRIMARY KEY,
                    name VARCHAR(100)
                )
            """);
            
            nextId = 1;
        }
        
    }

    @Override
    public BenchmarkResult run(Connection connection) throws Exception {

        long start = System.nanoTime();

        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO benchmark_insert(id, name)
                VALUES (?, ?)
                """)) {

            ps.setInt(1, nextId++);
            ps.setString(2, "Max Mustermann");

            ps.executeUpdate();

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
                DROP TABLE IF EXISTS benchmark_insert
            """);

        }
    }

}
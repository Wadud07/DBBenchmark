package de.pgm.dbbenchmark.benchmark;

import de.pgm.dbbenchmark.config.DbType;

public class BenchmarkResult {

    private final DbType database;
    private final String benchmarkName;
    private final long executionTimeNanos;

    public BenchmarkResult(
            DbType database,
            String benchmarkName,
            long executionTimeNanos) {

        this.database = database;
        this.benchmarkName = benchmarkName;
        this.executionTimeNanos = executionTimeNanos;
    }

    public DbType getDatabase() {
        return database;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

}
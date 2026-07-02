package de.pgm.dbbenchmark.benchmark;

import java.sql.Connection;

public interface Benchmark {

    String getName();

    default void setup(Connection connection) throws Exception {
        // Default: nothing to do
    }

    BenchmarkResult run(Connection connection) throws Exception;

    default void cleanup(Connection connection) throws Exception {
        // Default: nothing to do
    }

}
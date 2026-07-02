package de.pgm.dbbenchmark.export;

import de.pgm.dbbenchmark.benchmark.BenchmarkResult;
import de.pgm.dbbenchmark.benchmark.StatisticsAnalyzer;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvExporter {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String SUMMARY_FILE_NAME =
        "benchmark_summary_"
        + LocalDateTime.now().format(FORMATTER)
        + ".csv";

    private static final String RAW_FILE_NAME =
            "benchmark_raw_"
            + LocalDateTime.now().format(FORMATTER)
            + ".csv";

    public static void writeHeaders() throws IOException {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(SUMMARY_FILE_NAME))) {

            writer.println(
                    "Database,Benchmark,Average(ns),Minimum(ns),Maximum(ns),Median(ns),StdDev(ns),Variance"
            );
        }

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(RAW_FILE_NAME))) {

            writer.println(
                    "Database,Benchmark,Run,ExecutionTime(ns)"
            );
        }
    }

    public static void appendResults(List<BenchmarkResult> results)
            throws IOException {

        if (results.isEmpty()) {
            return;
        }

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(SUMMARY_FILE_NAME, true))) {

            writer.printf(
                    "%s,%s,%.0f,%d,%d,%.0f,%.0f,%.0f%n",
                    results.get(0).getDatabase(),
                    results.get(0).getBenchmarkName(),
                    StatisticsAnalyzer.average(results),
                    StatisticsAnalyzer.minimum(results),
                    StatisticsAnalyzer.maximum(results),
                    StatisticsAnalyzer.median(results),
                    StatisticsAnalyzer.standardDeviation(results),
                    StatisticsAnalyzer.variance(results)
            );
        }
    }
    
    public static void appendRawRuns(List<BenchmarkResult> results)
        throws IOException {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(RAW_FILE_NAME, true))) {

            for (int i = 0; i < results.size(); i++) {

                BenchmarkResult result = results.get(i);

                writer.printf(
                        "%s,%s,%d,%d%n",
                        result.getDatabase(),
                        result.getBenchmarkName(),
                        i + 1,
                        result.getExecutionTimeNanos()
                );
            }
        }
    }
    
    public static String getSummaryFileName() {
        return SUMMARY_FILE_NAME;
    }

    public static String getRawFileName() {
        return RAW_FILE_NAME;
    }
}
package de.pgm.dbbenchmark.benchmark;

import de.pgm.dbbenchmark.benchmarks.CreateTableBenchmark;
import de.pgm.dbbenchmark.benchmarks.InsertBenchmark;
import de.pgm.dbbenchmark.benchmarks.BatchInsertBenchmark;
import de.pgm.dbbenchmark.benchmarks.SelectBenchmark;
import de.pgm.dbbenchmark.benchmarks.UpdateBenchmark;
import de.pgm.dbbenchmark.benchmarks.DeleteBenchmark;
import de.pgm.dbbenchmark.benchmarks.DropTableBenchmark;
import de.pgm.dbbenchmark.benchmarks.IndexBenchmark;  
import de.pgm.dbbenchmark.export.CsvExporter;
import de.pgm.dbbenchmark.config.DbConnectionFactory;
import de.pgm.dbbenchmark.config.DbType;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkRunner {

    private static final int NUMBER_OF_RUNS = 50;
    private static final boolean PRINT_EACH_RUN = true;
    private static final int WARMUP_RUNS = 5;

    public static void main(String[] args) {

        try {

            CsvExporter.writeHeaders();

            runBenchmark(DbType.H2);

            System.out.println();

            runBenchmark(DbType.HSQLDB);

            generateCharts();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
    
    private static void generateCharts() {

        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    "analysis/generate_charts.py",
                    CsvExporter.getSummaryFileName(),
                    CsvExporter.getRawFileName()
            );

            pb.inheritIO();

            Process process = pb.start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Charts generated successfully.");
            } else {
                System.out.println("Chart generation failed.");
            }

        } catch (Exception e) {

            System.out.println("Unable to execute Python.");
            e.printStackTrace();

        }
    }

    private static void runBenchmark(DbType dbType) {

        try (Connection connection =
                     DbConnectionFactory.getConnection(dbType)) {

            List<Benchmark> benchmarks = List.of(
                    new CreateTableBenchmark(dbType),
                    new InsertBenchmark(dbType),
                    new BatchInsertBenchmark(dbType),
                    new SelectBenchmark(dbType),
                    new UpdateBenchmark(dbType),
                    new DeleteBenchmark(dbType),
                    new DropTableBenchmark(dbType),
                    new IndexBenchmark(dbType)
            );

            for (Benchmark benchmark : benchmarks) {

                List<BenchmarkResult> results = new ArrayList<>();

                benchmark.setup(connection);
                
                // JVM warm-up (not measured)
                for (int i = 0; i < WARMUP_RUNS; i++) {
                    benchmark.run(connection);
                }

                // Measured runs
                for (int i = 0; i < NUMBER_OF_RUNS; i++) {
                    results.add(benchmark.run(connection));
                }

                benchmark.cleanup(connection);

                System.out.println("=================================");
                System.out.println("Database : " + dbType);
                System.out.println("Benchmark: " + benchmark.getName());
                System.out.println("=================================");

                if (PRINT_EACH_RUN) {

                    for (int i = 0; i < results.size(); i++) {

                        BenchmarkResult result = results.get(i);

                        System.out.printf(
                                "Run %2d: %,d ns (%.3f ms)%n",
                                i + 1,
                                result.getExecutionTimeNanos(),
                                result.getExecutionTimeNanos() / 1_000_000.0
                        );
                    }

                    System.out.println();
                }

                System.out.printf(
                        "Average : %,.0f ns (%.3f ms)%n",
                        StatisticsAnalyzer.average(results),
                        StatisticsAnalyzer.average(results) / 1_000_000.0
                );

                System.out.printf(
                        "Minimum : %,d ns (%.3f ms)%n",
                        StatisticsAnalyzer.minimum(results),
                        StatisticsAnalyzer.minimum(results) / 1_000_000.0
                );

                System.out.printf(
                        "Maximum : %,d ns (%.3f ms)%n",
                        StatisticsAnalyzer.maximum(results),
                        StatisticsAnalyzer.maximum(results) / 1_000_000.0
                );
                
                System.out.printf(
                        "Median  : %,.0f ns (%.3f ms)%n",
                        StatisticsAnalyzer.median(results),
                        StatisticsAnalyzer.median(results) / 1_000_000.0
                );
                
                System.out.printf(
                        "Std Dev : %,.0f ns (%.3f ms)%n",
                        StatisticsAnalyzer.standardDeviation(results),
                        StatisticsAnalyzer.standardDeviation(results)
                                / 1_000_000.0
                );
                
                 System.out.printf(
                        "Variance: %,.0f%n",
                        StatisticsAnalyzer.variance(results)
                );
                 
                CsvExporter.appendResults(results);
                CsvExporter.appendRawRuns(results);
                
                

                System.out.println();
            }

        } catch (Exception e) {

            System.err.println("Benchmark failed for " + dbType);
            e.printStackTrace();

        }

    }

}
package de.pgm.dbbenchmark.benchmark;

import java.util.List;

public class StatisticsAnalyzer {

    public static double average(List<BenchmarkResult> results) {

        return results.stream()
                .mapToLong(BenchmarkResult::getExecutionTimeNanos)
                .average()
                .orElse(0);

    }

    public static long minimum(List<BenchmarkResult> results) {

        return results.stream()
                .mapToLong(BenchmarkResult::getExecutionTimeNanos)
                .min()
                .orElse(0);

    }

    public static long maximum(List<BenchmarkResult> results) {

        return results.stream()
                .mapToLong(BenchmarkResult::getExecutionTimeNanos)
                .max()
                .orElse(0);

    }
    
    public static double median(List<BenchmarkResult> results) {

        List<Long> values = results.stream()
                .map(BenchmarkResult::getExecutionTimeNanos)
                .sorted()
                .toList();

        int size = values.size();

        if (size % 2 == 0) {

            return (values.get(size / 2 - 1)
                    + values.get(size / 2)) / 2.0;

        } else {

            return values.get(size / 2);
        }
    }
    
    
    public static double standardDeviation(List<BenchmarkResult> results) {

        double average = average(results);

        double sum = 0;

        for (BenchmarkResult result : results) {

            double difference =
                    result.getExecutionTimeNanos() - average;

            sum += difference * difference;
        }

        return Math.sqrt(sum / results.size());
    }
    
    
    
    public static double variance(List<BenchmarkResult> results) {

        double average = average(results);

        double sum = 0;

        for (BenchmarkResult result : results) {

            double difference =
                    result.getExecutionTimeNanos() - average;

            sum += difference * difference;
        }

        return sum / results.size();
    }
    
   

}
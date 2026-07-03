
# DBBenchmark
School project to compare H2 with HSQLDB test database 
=======
# Database Benchmark Framework

Bachelor Thesis Project

A Java-based database benchmarking framework for comparing the performance of embedded relational databases.

Currently supported databases:

- H2
- HSQLDB

The framework automatically executes benchmark tests, calculates statistical metrics, exports benchmark results to CSV, generates professional PDF reports with Python, and is prepared for Jenkins CI integration.

---

# Features

✔ Generic benchmark framework

✔ Multiple database support

✔ Automatic benchmark execution

✔ JVM warm-up runs

✔ Statistical analysis

- Average
- Minimum
- Maximum
- Median
- Standard Deviation
- Variance

✔ CSV export

- Summary CSV
- Raw execution CSV

✔ Python report generation

- Professional PDF
- Charts
- Winner analysis
- Student's t-test
- Automatic conclusions

---

# Project Structure

```
DBBenchmark/

analysis/
    generate_charts.py
    output/

src/

benchmark/
    Benchmark.java
    BenchmarkRunner.java
    BenchmarkResult.java
    StatisticsAnalyzer.java

benchmarks/
    CreateTableBenchmark.java
    InsertBenchmark.java
    BatchInsertBenchmark.java
    SelectBenchmark.java
    UpdateBenchmark.java
    DeleteBenchmark.java
    DropTableBenchmark.java
    IndexBenchmark.java

config/
    DbConnectionFactory.java
    DbType.java

export/
    CsvExporter.java
```

---

# Implemented Benchmarks

The framework currently measures:

- CREATE TABLE
- INSERT
- BATCH INSERT (1000 rows)
- SELECT
- UPDATE
- DELETE
- DROP TABLE
- CREATE INDEX

---

# Benchmark Execution

For every benchmark:

```
Setup

↓

Warm-up runs
(not measured)

↓

Measured runs

↓

Cleanup
```

Warm-up runs reduce JVM JIT compilation effects.

Current configuration:

```
Warm-up runs: 5

Measured runs: 20
```

---

# Statistics

After every benchmark the following statistics are calculated:

- Average
- Minimum
- Maximum
- Median
- Standard Deviation
- Variance

Results are printed to the console and exported to CSV.

---

# CSV Export

Two CSV files are generated automatically.

Summary

```
benchmark_summary_<timestamp>.csv
```

Contains

- averages
- median
- standard deviation
- variance

Raw Results

```
benchmark_raw_<timestamp>.csv
```

Contains every measured execution time.

Example

```
Database,Benchmark,Run,ExecutionTime(ns)

H2,INSERT,1,435221
H2,INSERT,2,426115
...
```

The raw data is used for statistical testing.

---

# Automatic Report Generation

After all benchmarks finish,

BenchmarkRunner automatically starts

```
analysis/generate_charts.py
```

The script creates:

- Professional PDF report
- Benchmark charts
- Database comparison
- Winner analysis
- Overall score
- Student's t-test
- Automatic conclusion

Output:

```
analysis/output/
```

---

# Student's t-test

The report performs Welch's Student t-test using the raw execution times.

Purpose:

Determine whether the observed performance differences are statistically significant.

Interpretation:

```
p < 0.05

↓

Performance difference is statistically significant.
```

---

# Technologies

Java

- Java 21

Database

- H2
- HSQLDB

Python

- pandas
- matplotlib
- scipy
- reportlab

Build

- Maven

IDE

- Apache NetBeans

---

# Required Python Packages

Install once

```
pip3 install pandas matplotlib scipy reportlab
```

---

# Running

Compile

```
mvn clean compile
```

Run

```
mvn exec:java
```

The framework automatically

- executes benchmarks
- exports CSV
- generates PDF report

---

# Future Improvements

Possible future extensions:

- PostgreSQL
- MariaDB
- SQLite
- MySQL

Additional benchmarks:

- JOIN
- Transactions
- Prepared Statements
- Bulk Updates

CI/CD:

- Jenkins Pipeline


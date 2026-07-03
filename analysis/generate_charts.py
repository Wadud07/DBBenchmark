import os
import sys
import platform
from datetime import datetime

import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import ttest_ind

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.platypus import (
    SimpleDocTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
    Image,
    PageBreak
)

styles = getSampleStyleSheet()


# ----------------------------------------------------------
# Helper Functions
# ----------------------------------------------------------

def load_data(csv_file):
    """Load benchmark CSV."""

    return pd.read_csv(csv_file)


def create_output_directory():
    os.makedirs("analysis/output", exist_ok=True)


def timestamp():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def pdf_filename():
    return (
        "analysis/output/benchmark_report_"
        + datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
        + ".pdf"
    )


# ----------------------------------------------------------
# Cover Page
# ----------------------------------------------------------

def add_cover_page(story, df, measured_runs, warmup_runs):

    story.append(
        Paragraph(
            "<font size=24><b>Database Benchmark Report</b></font>",
            styles["Title"]
        )
    )

    story.append(Spacer(1, 25))

    story.append(
        Paragraph(
            "<b>Automatically generated benchmark report</b>",
            styles["Heading2"]
        )
    )

    story.append(Spacer(1, 20))

    databases = ", ".join(sorted(df["Database"].unique()))

    story.append(
        Paragraph(f"<b>Date:</b> {timestamp()}", styles["Normal"])
    )

    story.append(
        Paragraph(f"<b>Operating System:</b> {platform.system()}",
                  styles["Normal"])
    )

    story.append(
        Paragraph(f"<b>Python:</b> {platform.python_version()}",
                  styles["Normal"])
    )

    story.append(
        Paragraph(f"<b>Databases:</b> {databases}",
                  styles["Normal"])
    )

    story.append(
        Paragraph(
            f"<b>Benchmarks:</b> {df['Benchmark'].nunique()}",
            styles["Normal"]
        )
    )

    story.append(
        Paragraph(
            f"<b>Measured Runs:</b> {measured_runs}",
            styles["Normal"]
        )
    )

    story.append(
        Paragraph(
            f"<b>Warm-up Runs:</b> {warmup_runs}",
            styles["Normal"]
        )
    )

    story.append(PageBreak())


# ----------------------------------------------------------
# Benchmark Summary
# ----------------------------------------------------------

def add_summary_page(story, df):

    story.append(
        Paragraph(
            "Benchmark Summary",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 15))

    total_databases = df["Database"].nunique()
    total_benchmarks = df["Benchmark"].nunique()
    total_results = len(df)

    story.append(
        Paragraph(
            f"Number of databases: <b>{total_databases}</b>",
            styles["Normal"]
        )
    )

    story.append(
        Paragraph(
            f"Benchmark categories: <b>{total_benchmarks}</b>",
            styles["Normal"]
        )
    )

    story.append(
        Paragraph(
            f"Benchmark results: <b>{total_results}</b>",
            styles["Normal"]
        )
    )

    story.append(Spacer(1, 20))

    text = """
    This report summarizes execution-time measurements for
    embedded relational database management systems.
    All measurements were performed after JVM warm-up
    iterations and repeated multiple times to improve
    statistical reliability.
    """

    story.append(
        Paragraph(text, styles["BodyText"])
    )

    story.append(PageBreak())


# ----------------------------------------------------------
# Statistics Table
# ----------------------------------------------------------

def add_statistics_table(story, df):

    story.append(
        Paragraph(
            "Summary Statistics",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 15))

    table_data = [[
        "Database",
        "Benchmark",
        "Average",
        "Median",
        "Std Dev",
        "Minimum",
        "Maximum"
    ]]

    for _, row in df.iterrows():

        table_data.append([

            row["Database"],
            row["Benchmark"],

            f"{row['Average(ns)']:.0f}",
            f"{row['Median(ns)']:.0f}",
            f"{row['StdDev(ns)']:.0f}",
            f"{row['Minimum(ns)']:.0f}",
            f"{row['Maximum(ns)']:.0f}"

        ])

    table = Table(table_data)

    table.setStyle(

        TableStyle([

            ("BACKGROUND", (0, 0), (-1, 0), colors.darkblue),

            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),

            ("GRID", (0, 0), (-1, -1), 0.5, colors.black),

            ("BACKGROUND", (0, 1), (-1, -1), colors.beige),

            ("ALIGN", (0, 0), (-1, -1), "CENTER"),

            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),

            ("BOTTOMPADDING", (0, 0), (-1, 0), 8)

        ])

    )

    story.append(table)

    story.append(PageBreak())

# ----------------------------------------------------------
# Chart Generation
# ----------------------------------------------------------

def create_chart(df, column, title, filename):

    pivot = df.pivot(
        index="Benchmark",
        columns="Database",
        values=column
    )

    plt.figure(figsize=(11, 6))

    ax = pivot.plot(kind="bar")

    ax.set_title(title)
    ax.set_xlabel("Benchmark")
    ax.set_ylabel(column)

    plt.xticks(rotation=30, ha="right")

    plt.tight_layout()

    output = os.path.join("analysis/output", filename)

    plt.savefig(output, dpi=300)

    plt.close()

    return output


# ----------------------------------------------------------
# PDF Page containing one chart
# ----------------------------------------------------------

def add_chart_page(story, image_path, title):

    story.append(
        Paragraph(
            title,
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 15))

    img = Image(image_path)

    img.drawHeight = 330
    img.drawWidth = 500

    story.append(img)

    story.append(PageBreak())


# ----------------------------------------------------------
# Generate every chart
# ----------------------------------------------------------

def generate_all_charts(df):

    charts = []

    charts.append((
        create_chart(
            df,
            "Average(ns)",
            "Average Execution Time",
            "average_execution_time.png"
        ),
        "Average Execution Time"
    ))

    charts.append((
        create_chart(
            df,
            "Median(ns)",
            "Median Execution Time",
            "median_execution_time.png"
        ),
        "Median Execution Time"
    ))

    charts.append((
        create_chart(
            df,
            "StdDev(ns)",
            "Standard Deviation",
            "standard_deviation.png"
        ),
        "Standard Deviation"
    ))

    charts.append((
        create_chart(
            df,
            "Variance",
            "Variance",
            "variance.png"
        ),
        "Variance"
    ))

    return charts

# ----------------------------------------------------------
# Winner Analysis
# ----------------------------------------------------------

def benchmark_winners(df):

    winners = []

    benchmarks = sorted(df["Benchmark"].unique())

    for benchmark in benchmarks:

        subset = df[df["Benchmark"] == benchmark]

        fastest = subset.loc[subset["Average(ns)"].idxmin()]

        winners.append([
            benchmark,
            fastest["Database"],
            fastest["Average(ns)"]
        ])

    return winners


# ----------------------------------------------------------
# Winner Table
# ----------------------------------------------------------

def add_winner_page(story, df):

    story.append(
        Paragraph(
            "Benchmark Winners",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 15))

    winners = benchmark_winners(df)

    table_data = [["Benchmark", "Winner", "Average (ns)"]]

    for benchmark, database, average in winners:

        table_data.append([
            benchmark,
            database,
            f"{average:,.0f}"
        ])

    table = Table(table_data)

    table.setStyle(

        TableStyle([

            ("BACKGROUND", (0, 0), (-1, 0), colors.darkgreen),

            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),

            ("GRID", (0, 0), (-1, -1), 0.5, colors.black),

            ("BACKGROUND", (0, 1), (-1, -1), colors.beige),

            ("ALIGN", (0, 0), (-1, -1), "CENTER"),

            ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),

            ("BOTTOMPADDING", (0, 0), (-1, 0), 8)

        ])

    )

    story.append(table)

    story.append(PageBreak())


# ----------------------------------------------------------
# Overall Score
# ----------------------------------------------------------

def add_score_page(story, df):

    story.append(
        Paragraph(
            "Overall Benchmark Score",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 20))

    winners = benchmark_winners(df)

    score = {}

    for _, database, _ in winners:
        score[database] = score.get(database, 0) + 1

    rows = [["Database", "Benchmarks Won"]]

    for database, wins in score.items():
        rows.append([database, str(wins)])

    table = Table(rows)

    table.setStyle(

        TableStyle([

            ("BACKGROUND", (0,0), (-1,0), colors.darkblue),

            ("TEXTCOLOR", (0,0), (-1,0), colors.white),

            ("GRID", (0,0), (-1,-1), 0.5, colors.black),

            ("BACKGROUND", (0,1), (-1,-1), colors.beige),

            ("ALIGN", (0,0), (-1,-1), "CENTER"),

            ("FONTNAME", (0,0), (-1,0), "Helvetica-Bold")

        ])

    )

    story.append(table)

    story.append(PageBreak())


# ----------------------------------------------------------
# Automatic Conclusion
# ----------------------------------------------------------

def add_conclusion_page(story, df):

    story.append(
        Paragraph(
            "Automatic Conclusion",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1,20))

    winners = benchmark_winners(df)

    score = {}

    for _, database, _ in winners:
        score[database] = score.get(database,0)+1

    overall = max(score, key=score.get)

    fastest = df.loc[df["Average(ns)"].idxmin()]
    slowest = df.loc[df["Average(ns)"].idxmax()]

    text = f"""
    <b>Summary</b><br/><br/>

    The benchmark suite executed
    {df['Benchmark'].nunique()} benchmark categories
    on {df['Database'].nunique()} embedded database systems.

    <br/><br/>

    Based on the average execution time,
    <b>{overall}</b> achieved the best overall performance,
    winning {score[overall]} benchmark categories.

    <br/><br/>

    The fastest benchmark measured was
    <b>{fastest['Benchmark']}</b>
    executed on
    <b>{fastest['Database']}</b>
    with an average execution time of
    <b>{fastest['Average(ns)']:,.0f} ns</b>.

    <br/><br/>

    The slowest benchmark measured was
    <b>{slowest['Benchmark']}</b>
    executed on
    <b>{slowest['Database']}</b>
    with an average execution time of
    <b>{slowest['Average(ns)']:,.0f} ns</b>.

    <br/><br/>

    Overall, the benchmark results indicate that
    execution times remained stable after warm-up
    iterations while demonstrating measurable
    performance differences between the tested
    database systems.

    <br/><br/>

    This report was generated automatically from
    the benchmark CSV produced by the Java benchmark
    framework.
    """

    story.append(
        Paragraph(text, styles["BodyText"])
    )

    story.append(PageBreak())


# ----------------------------------------------------------
# Header / Footer
# ----------------------------------------------------------

from reportlab.lib.units import cm


def add_page_number(canvas, doc):

    canvas.saveState()

    canvas.setFont("Helvetica", 9)

    canvas.drawString(
        1.5 * cm,
        1 * cm,
        "Database Benchmark Report"
    )

    canvas.drawRightString(
        19 * cm,
        1 * cm,
        f"Page {doc.page}"
    )

    canvas.restoreState()


# ----------------------------------------------------------
# Database Comparison Chart
# ----------------------------------------------------------

def create_database_comparison(df):

    pivot = df.pivot(
        index="Benchmark",
        columns="Database",
        values="Average(ns)"
    )

    plt.figure(figsize=(11, 6))

    for database in pivot.columns:

        plt.plot(
            pivot.index,
            pivot[database],
            marker="o",
            linewidth=2,
            label=database
        )

    plt.title("Database Comparison")

    plt.xlabel("Benchmark")

    plt.ylabel("Average Execution Time (ns)")

    plt.xticks(rotation=30, ha="right")

    plt.legend()

    plt.tight_layout()

    output = "analysis/output/database_comparison.png"

    plt.savefig(output, dpi=300)

    plt.close()

    return output


def add_database_comparison_page(story, image):

    story.append(
        Paragraph(
            "Database Comparison",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 15))

    img = Image(image)

    img.drawWidth = 500
    img.drawHeight = 330

    story.append(img)

    story.append(PageBreak())


# ----------------------------------------------------------
# Student's t-test
# ----------------------------------------------------------

def add_t_test(story, raw_csv):

    story.append(PageBreak())

    story.append(
        Paragraph(
            "Student's t-test",
            styles["Heading1"]
        )
    )

    story.append(Spacer(1, 15))

    df = pd.read_csv(raw_csv)

    rows = [[
        "Benchmark",
        "t-statistic",
        "p-value",
        "Significant?"
    ]]

    for benchmark in sorted(df["Benchmark"].unique()):

        benchmark_df = df[
            df["Benchmark"] == benchmark
        ]

        h2 = benchmark_df[
            benchmark_df["Database"] == "H2"
        ]["ExecutionTime(ns)"]

        hsqldb = benchmark_df[
            benchmark_df["Database"] == "HSQLDB"
        ]["ExecutionTime(ns)"]

        t_stat, p_value = ttest_ind(
            h2,
            hsqldb,
            equal_var=False
        )

        significant = (
            "YES"
            if p_value < 0.05
            else "NO"
        )

        rows.append([
            benchmark,
            f"{t_stat:.3f}",
            f"{p_value:.6f}",
            significant
        ])

    table = Table(rows)

    table.setStyle(TableStyle([

        ("BACKGROUND", (0, 0), (-1, 0), colors.darkblue),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),

        ("GRID", (0, 0), (-1, -1), 1, colors.black),

        ("BACKGROUND", (0, 1), (-1, -1), colors.beige),

        ("FONTNAME", (0, 0), (-1, 0), "Helvetica-Bold"),

        ("BOTTOMPADDING", (0, 0), (-1, 0), 10),

        ("ALIGN", (1, 1), (-1, -1), "CENTER")

    ]))

    story.append(table)

    story.append(Spacer(1, 20))

    story.append(
        Paragraph(
            "<b>Interpretation</b><br/>"
            "A p-value below 0.05 indicates that the observed "
            "performance difference between H2 and HSQLDB "
            "is statistically significant.",
            styles["BodyText"]
        )
    )

    story.append(PageBreak())




# ----------------------------------------------------------
# Main (temporary)
# ----------------------------------------------------------

def main():

    if len(sys.argv) != 5:
        print(
            "Usage: python generate_charts.py "
            "<summary.csv> <raw.csv> <measured_runs> <warmup_runs>"
        )
        return

    summary_csv = sys.argv[1]
    raw_csv = sys.argv[2]
    measured_runs = int(sys.argv[3])
    warmup_runs = int(sys.argv[4])

    create_output_directory()

    df = load_data(summary_csv)

    pdf = SimpleDocTemplate(pdf_filename(), pagesize=A4)

    story = []

    add_cover_page(story, df, measured_runs, warmup_runs)
    add_summary_page(story, df)
    add_statistics_table(story, df)

    charts = generate_all_charts(df)

    for chart_file, title in charts:
        add_chart_page(story, chart_file, title)

    comparison = create_database_comparison(df)

    add_database_comparison_page(
        story,
        comparison
    )

    add_winner_page(story, df)

    add_score_page(story, df)

    add_t_test(story, raw_csv)

    add_conclusion_page(story, df)

    pdf.build(
        story,
        onFirstPage=add_page_number,
        onLaterPages=add_page_number
    )

    print("PDF created successfully.")


if __name__ == "__main__":
    main()
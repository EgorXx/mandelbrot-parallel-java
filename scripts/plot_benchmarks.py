import csv
from pathlib import Path

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt


SIZE = 1000
MAX_ITERATIONS = 500
BLOCK_HEIGHT = 32
RESULTS = Path(__file__).resolve().parents[1] / "results"

with (RESULTS / "benchmarks.csv").open(encoding="utf-8", newline="") as source:
    rows = [row for row in csv.DictReader(source)
            if int(row["width"]) == SIZE and int(row["height"]) == SIZE
            and int(row["max_iterations"]) == MAX_ITERATIONS
            and int(row["block_height"]) in (0, BLOCK_HEIGHT)]

threads = sorted({int(row["threads"]) for row in rows})

for column, label, filename in (
        ("time_ms", "Время, мс", "time.png"),
        ("speedup", "Ускорение, Tseq / Tpar", "speedup.png"),
        ("efficiency", "Эффективность, S / P", "efficiency.png")):
    figure, axis = plt.subplots(figsize=(8, 5))
    for algorithm in ("sequential", "static", "dynamic", "hybrid"):
        series = sorted((row for row in rows if row["algorithm"] == algorithm),
                        key=lambda row: int(row["threads"]))
        if not series:
            raise ValueError(f"Нет данных для {algorithm}: {SIZE}x{SIZE}, {MAX_ITERATIONS} итераций")
        if algorithm == "sequential":
            axis.axhline(float(series[0][column]), color="gray", linestyle="--", label="sequential (1 поток)")
        else:
            axis.plot([int(row["threads"]) for row in series],
                      [float(row[column]) for row in series], marker="o", label=algorithm)
    axis.set(title=f"Мандельброт: {SIZE}×{SIZE}, {MAX_ITERATIONS} итераций, блок {BLOCK_HEIGHT}",
             xlabel="Число потоков", ylabel=label, xticks=threads, ylim=(0, None))
    axis.grid(alpha=0.3)
    axis.legend()
    figure.tight_layout()
    figure.savefig(RESULTS / filename, dpi=160)
    plt.close(figure)
    print(RESULTS / filename)

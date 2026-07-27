# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-27T04:39:49Z
- **Commit:** [`9432fdc`](https://github.com/prometheus/client_java/commit/9432fdc1933611d34d944d4640ebca3bf91f8ee4)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 64.31K | ± 1.09K | ops/s | **fastest** |
| prometheusNoLabelsInc | 52.25K | ± 5.54K | ops/s | 1.2x slower |
| prometheusAdd | 51.45K | ± 269.68 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.05K | ± 1.71K | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.58K | ± 37.36 | ops/s | 3.5x slower |
| openTelemetryInc | 15.00K | ± 347.17 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.82K | ± 333.89 | ops/s | 5.0x slower |
| simpleclientInc | 6.59K | ± 8.48 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.44K | ± 141.64 | ops/s | 10.0x slower |
| simpleclientAdd | 6.40K | ± 128.12 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.59K | ± 21.62 | ops/s | **fastest** |
| prometheusClassicSingleThread | 4.59K | ± 18.04 | ops/s | 2.7x slower |
| prometheusClassic | 4.54K | ± 767.69 | ops/s | 2.8x slower |
| simpleclient | 4.50K | ± 26.50 | ops/s | 2.8x slower |
| prometheusNative | 2.78K | ± 235.41 | ops/s | 4.5x slower |
| openTelemetryExponential | 962.43 | ± 8.87 | ops/s | 13x slower |
| openTelemetryClassic | 806.88 | ± 50.64 | ops/s | 16x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.97K | ± 485.57 | ops/s | **fastest** |
| prometheusWriteToNull | 23.57K | ± 199.40 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 516.37K | ± 1.73K | ops/s | **fastest** |
| prometheusWriteToByteArray | 507.11K | ± 6.23K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 496.09K | ± 1.39K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 494.19K | ± 1.79K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48050.579   ± 1705.475  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12818.799    ± 333.885  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14998.910    ± 347.174  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18584.792     ± 37.356  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51447.491    ± 269.681  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64310.289   ± 1091.848  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      52247.097   ± 5535.854  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6401.676    ± 128.122  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6592.129      ± 8.476  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6438.969    ± 141.644  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        806.885     ± 50.641  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        962.431      ± 8.866  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4541.945    ± 767.693  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12592.896     ± 21.623  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4594.654     ± 18.042  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2782.422    ± 235.409  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4503.708     ± 26.503  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23974.571    ± 485.572  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23571.204    ± 199.398  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     494189.213   ± 1789.582  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     496092.796   ± 1386.934  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     507110.699   ± 6226.069  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     516365.141   ± 1733.830  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

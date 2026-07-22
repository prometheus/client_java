# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-22T04:37:57Z
- **Commit:** [`243322c`](https://github.com/prometheus/client_java/commit/243322c8b7012fc88c497d0f1a85cd7e161a6a09)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.94K | ± 413.82 | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.18K | ± 1.57K | ops/s | 1.2x slower |
| prometheusAdd | 51.46K | ± 122.60 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.34K | ± 1.35K | ops/s | 1.4x slower |
| simpleclientInc | 6.53K | ± 35.52 | ops/s | 10x slower |
| simpleclientAdd | 6.47K | ± 78.24 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 56.40 | ops/s | 10x slower |
| openTelemetryInc | 3.31K | ± 178.78 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.31K | ± 266.39 | ops/s | 20x slower |
| openTelemetryAdd | 3.18K | ± 56.71 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.58K | ± 22.33 | ops/s | **fastest** |
| prometheusClassic | 6.40K | ± 1.22K | ops/s | 2.0x slower |
| prometheusClassicSingleThread | 4.59K | ± 10.23 | ops/s | 2.7x slower |
| simpleclient | 4.46K | ± 42.85 | ops/s | 2.8x slower |
| prometheusNative | 2.82K | ± 368.33 | ops/s | 4.5x slower |
| openTelemetryClassic | 772.08 | ± 29.30 | ops/s | 16x slower |
| openTelemetryExponential | 628.73 | ± 53.78 | ops/s | 20x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.76K | ± 743.47 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.66K | ± 679.96 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 501.33K | ± 6.48K | ops/s | **fastest** |
| prometheusWriteToByteArray | 494.60K | ± 6.07K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 486.11K | ± 5.71K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.28K | ± 6.95K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48340.435   ± 1352.450  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3180.122     ± 56.710  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3309.344    ± 178.776  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3306.946    ± 266.391  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51463.984    ± 122.598  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65938.241    ± 413.824  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55184.628   ± 1566.519  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6466.421     ± 78.239  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6531.383     ± 35.518  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6337.274     ± 56.400  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        772.077     ± 29.298  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        628.725     ± 53.778  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6397.280   ± 1219.286  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12584.445     ± 22.328  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4594.417     ± 10.233  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2817.419    ± 368.325  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4456.721     ± 42.847  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23655.450    ± 679.963  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23763.675    ± 743.465  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479284.994   ± 6948.769  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486110.123   ± 5714.360  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     494603.439   ± 6070.004  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     501334.013   ± 6482.831  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-27T04:36:06Z
- **Commit:** [`2a2c73d`](https://github.com/prometheus/client_java/commit/2a2c73d7d23bfa291b10df85056027398e8a868d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.31K | ± 1.39K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.69K | ± 408.29 | ops/s | 1.2x slower |
| prometheusAdd | 51.51K | ± 173.13 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.05K | ± 601.46 | ops/s | 1.4x slower |
| simpleclientInc | 6.59K | ± 7.11 | ops/s | 9.9x slower |
| simpleclientAdd | 6.31K | ± 289.41 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.29K | ± 87.64 | ops/s | 10x slower |
| openTelemetryAdd | 3.58K | ± 278.68 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.33K | ± 409.92 | ops/s | 20x slower |
| openTelemetryInc | 3.28K | ± 73.82 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.61K | ± 846.61 | ops/s | **fastest** |
| simpleclient | 4.44K | ± 28.20 | ops/s | 1.0x slower |
| prometheusNative | 2.97K | ± 396.10 | ops/s | 1.6x slower |
| openTelemetryClassic | 743.50 | ± 4.83 | ops/s | 6.2x slower |
| openTelemetryExponential | 673.16 | ± 4.17 | ops/s | 6.9x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.81K | ± 377.20 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.19K | ± 1.24K | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 513.56K | ± 2.75K | ops/s | **fastest** |
| prometheusWriteToByteArray | 501.48K | ± 7.39K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 491.94K | ± 3.61K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 484.78K | ± 3.31K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48053.873    ± 601.455  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3581.834    ± 278.677  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3282.442     ± 73.822  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3333.170    ± 409.921  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51508.052    ± 173.128  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65307.971   ± 1392.079  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56687.912    ± 408.292  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6307.617    ± 289.411  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6587.856      ± 7.106  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6286.794     ± 87.642  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        743.502      ± 4.826  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        673.159      ± 4.171  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4612.386    ± 846.608  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2973.148    ± 396.097  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4439.789     ± 28.201  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23189.059   ± 1244.265  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24807.174    ± 377.200  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     484779.685   ± 3311.088  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     491941.381   ± 3606.005  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     501483.552   ± 7389.124  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     513555.703   ± 2751.169  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

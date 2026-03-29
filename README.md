# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-29T04:30:52Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.22K | ± 423.46 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.04K | ± 298.39 | ops/s | 1.2x slower |
| prometheusAdd | 51.58K | ± 106.18 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 45.14K | ± 8.72K | ops/s | 1.5x slower |
| simpleclientInc | 6.68K | ± 53.00 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.38K | ± 187.76 | ops/s | 10x slower |
| simpleclientAdd | 6.30K | ± 267.33 | ops/s | 11x slower |
| openTelemetryInc | 1.34K | ± 186.96 | ops/s | 50x slower |
| openTelemetryIncNoLabels | 1.27K | ± 13.55 | ops/s | 52x slower |
| openTelemetryAdd | 1.25K | ± 8.12 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.92K | ± 649.79 | ops/s | **fastest** |
| simpleclient | 4.46K | ± 48.77 | ops/s | 1.1x slower |
| prometheusNative | 2.56K | ± 124.29 | ops/s | 1.9x slower |
| openTelemetryClassic | 722.74 | ± 60.96 | ops/s | 6.8x slower |
| openTelemetryExponential | 572.24 | ± 32.42 | ops/s | 8.6x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.69K | ± 2.36K | ops/s | **fastest** |
| prometheusWriteToByteArray | 489.04K | ± 804.11 | ops/s | 1.0x slower |
| openMetricsWriteToNull | 486.17K | ± 2.91K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.39K | ± 8.88K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      45141.750   ± 8721.446  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1248.446      ± 8.124  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1335.267    ± 186.960  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1272.788     ± 13.549  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51578.590    ± 106.183  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66224.821    ± 423.461  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57044.018    ± 298.393  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6303.424    ± 267.334  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6675.484     ± 53.003  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6376.340    ± 187.759  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        722.738     ± 60.962  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        572.235     ± 32.419  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4921.681    ± 649.787  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2562.307    ± 124.294  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4458.848     ± 48.773  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475386.186   ± 8876.072  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486165.139   ± 2909.445  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489039.760    ± 804.110  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492690.194   ± 2358.165  ops/s
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

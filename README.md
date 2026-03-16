# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-16T04:32:02Z
- **Commit:** [`b81332e`](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.49K | ± 1.60K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.08K | ± 162.39 | ops/s | 1.1x slower |
| prometheusAdd | 51.44K | ± 233.68 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.03K | ± 1.33K | ops/s | 1.3x slower |
| simpleclientInc | 6.49K | ± 69.38 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.48K | ± 153.96 | ops/s | 10.0x slower |
| simpleclientAdd | 6.43K | ± 165.45 | ops/s | 10x slower |
| openTelemetryAdd | 1.30K | ± 43.31 | ops/s | 50x slower |
| openTelemetryInc | 1.24K | ± 21.91 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.20K | ± 34.95 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.37K | ± 1.35K | ops/s | **fastest** |
| simpleclient | 4.58K | ± 46.09 | ops/s | 1.2x slower |
| prometheusNative | 3.18K | ± 107.15 | ops/s | 1.7x slower |
| openTelemetryClassic | 670.41 | ± 3.06 | ops/s | 8.0x slower |
| openTelemetryExponential | 573.36 | ± 34.31 | ops/s | 9.4x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 503.69K | ± 4.65K | ops/s | **fastest** |
| prometheusWriteToByteArray | 489.90K | ± 3.33K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.27K | ± 5.70K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 478.68K | ± 6.27K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48025.010   ± 1327.675  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1298.748     ± 43.307  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1242.016     ± 21.911  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1196.801     ± 34.951  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51440.027    ± 233.684  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64493.734   ± 1599.930  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57080.221    ± 162.393  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6429.593    ± 165.447  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6490.020     ± 69.378  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6479.565    ± 153.964  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        670.408      ± 3.055  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        573.357     ± 34.309  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5373.738   ± 1352.749  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3175.825    ± 107.149  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4576.259     ± 46.091  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478680.052   ± 6273.294  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485272.044   ± 5698.922  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489898.324   ± 3326.668  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     503688.496   ± 4647.228  ops/s
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

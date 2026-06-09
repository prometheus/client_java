# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-09T04:38:15Z
- **Commit:** [`65d57b0`](https://github.com/prometheus/client_java/commit/65d57b020c6893283d5b4b85d76d86dfd7389cc8)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.97K | ± 1.64K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.64K | ± 1.17K | ops/s | 1.1x slower |
| prometheusAdd | 51.42K | ± 295.62 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.05K | ± 670.44 | ops/s | 1.3x slower |
| simpleclientInc | 6.59K | ± 51.38 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.36K | ± 13.46 | ops/s | 10x slower |
| simpleclientAdd | 6.23K | ± 294.89 | ops/s | 10x slower |
| openTelemetryAdd | 3.34K | ± 358.82 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.19K | ± 252.42 | ops/s | 20x slower |
| openTelemetryInc | 3.04K | ± 239.26 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.24K | ± 950.45 | ops/s | **fastest** |
| simpleclient | 4.42K | ± 39.28 | ops/s | 1.2x slower |
| prometheusNative | 3.22K | ± 180.20 | ops/s | 1.6x slower |
| openTelemetryClassic | 777.54 | ± 44.74 | ops/s | 6.7x slower |
| openTelemetryExponential | 678.15 | ± 83.01 | ops/s | 7.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.12K | ± 474.39 | ops/s | **fastest** |
| prometheusWriteToNull | 23.18K | ± 1.20K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 526.74K | ± 3.68K | ops/s | **fastest** |
| prometheusWriteToByteArray | 523.25K | ± 5.94K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 498.42K | ± 1.28K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 496.64K | ± 1.40K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50045.580    ± 670.442  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3342.803    ± 358.823  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3035.140    ± 239.265  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3189.483    ± 252.423  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51415.291    ± 295.620  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64973.172   ± 1638.885  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56643.080   ± 1174.379  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6229.034    ± 294.890  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6593.533     ± 51.384  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6362.431     ± 13.458  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        777.537     ± 44.742  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        678.148     ± 83.011  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5238.387    ± 950.445  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3224.638    ± 180.203  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4424.984     ± 39.285  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24123.692    ± 474.393  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23181.200   ± 1201.885  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     496638.248   ± 1404.021  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     498420.025   ± 1275.502  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     523251.732   ± 5935.178  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     526737.200   ± 3676.414  ops/s
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

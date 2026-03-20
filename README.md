# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-20T04:24:27Z
- **Commit:** [`3524bcf`](https://github.com/prometheus/client_java/commit/3524bcfd17124a9d34e8f4f2aa5d530e0db14fdd)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.53K | ± 2.15K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.43K | ± 1.40K | ops/s | 1.1x slower |
| prometheusAdd | 51.70K | ± 76.51 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 50.20K | ± 417.84 | ops/s | 1.3x slower |
| simpleclientInc | 6.68K | ± 103.98 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.37K | ± 77.40 | ops/s | 10x slower |
| simpleclientAdd | 6.25K | ± 227.22 | ops/s | 10x slower |
| openTelemetryAdd | 1.31K | ± 76.24 | ops/s | 49x slower |
| openTelemetryInc | 1.29K | ± 26.07 | ops/s | 50x slower |
| openTelemetryIncNoLabels | 1.26K | ± 50.07 | ops/s | 51x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.61K | ± 462.15 | ops/s | **fastest** |
| simpleclient | 4.50K | ± 62.14 | ops/s | 1.0x slower |
| prometheusNative | 2.78K | ± 178.11 | ops/s | 1.7x slower |
| openTelemetryClassic | 704.42 | ± 37.50 | ops/s | 6.5x slower |
| openTelemetryExponential | 554.73 | ± 26.42 | ops/s | 8.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.75K | ± 1.98K | ops/s | **fastest** |
| openMetricsWriteToNull | 487.42K | ± 2.89K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 485.03K | ± 4.50K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 478.35K | ± 6.23K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50199.583    ± 417.838  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1309.032     ± 76.237  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1285.656     ± 26.072  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1260.219     ± 50.075  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51703.332     ± 76.512  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64534.593   ± 2150.030  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56428.632   ± 1401.769  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6248.536    ± 227.221  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6682.295    ± 103.982  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6374.641     ± 77.398  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        704.418     ± 37.504  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        554.732     ± 26.416  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4606.878    ± 462.154  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2777.523    ± 178.111  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4502.959     ± 62.142  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478352.396   ± 6226.714  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487423.618   ± 2886.721  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485034.894   ± 4504.510  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492746.602   ± 1975.487  ops/s
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

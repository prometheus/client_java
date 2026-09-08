# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-08T03:52:10Z
- **Commit:** [`39a91dd`](https://github.com/prometheus/client_java/commit/39a91ddb316ebbeebb8740a436109f9b9cca7e17)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.05K | ± 282.51 | ops/s |
| prometheusNoLabelsInc | 57.27K | ± 206.35 | ops/s |
| prometheusAdd | 51.14K | ± 758.30 | ops/s |
| codahaleIncNoLabels | 50.17K | ± 140.78 | ops/s |
| openTelemetryIncNoLabels | 18.47K | ± 157.68 | ops/s |
| openTelemetryInc | 14.97K | ± 210.74 | ops/s |
| openTelemetryAdd | 12.68K | ± 207.70 | ops/s |
| simpleclientInc | 6.56K | ± 42.52 | ops/s |
| simpleclientAdd | 6.50K | ± 48.50 | ops/s |
| simpleclientNoLabelsInc | 6.42K | ± 124.81 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.35K | ± 63.99 | ops/s |
| prometheusClassic | 5.24K | ± 1.75K | ops/s |
| prometheusClassicSingleThread | 4.56K | ± 22.81 | ops/s |
| simpleclient | 4.43K | ± 66.04 | ops/s |
| prometheusNative | 2.82K | ± 306.28 | ops/s |
| openTelemetryExponential | 920.78 | ± 62.06 | ops/s |
| openTelemetryClassic | 799.48 | ± 60.50 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 24.17K | ± 867.59 | ops/s |
| openMetricsWriteToNull | 24.12K | ± 1.05K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 487.05K | ± 6.19K | ops/s |
| prometheusWriteToNull | 482.46K | ± 9.81K | ops/s |
| openMetricsWriteToByteArray | 472.29K | ± 5.42K | ops/s |
| openMetricsWriteToNull | 468.28K | ± 6.38K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50174.280    ± 140.775  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12675.815    ± 207.704  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14965.373    ± 210.736  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18471.697    ± 157.683  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51141.386    ± 758.303  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66045.337    ± 282.513  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57265.184    ± 206.351  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6501.322     ± 48.503  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6556.642     ± 42.516  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6422.636    ± 124.814  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        799.481     ± 60.501  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        920.776     ± 62.059  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5235.321   ± 1753.622  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12354.990     ± 63.994  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4555.825     ± 22.809  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2819.985    ± 306.280  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4434.983     ± 66.044  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24120.616   ± 1045.162  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24170.012    ± 867.594  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     472286.641   ± 5423.916  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     468284.315   ± 6383.363  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     487049.228   ± 6188.753  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     482457.400   ± 9807.339  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

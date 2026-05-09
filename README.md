# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-09T04:35:40Z
- **Commit:** [`11cb921`](https://github.com/prometheus/client_java/commit/11cb921cdea4789cf86ca903867ce9e3e5debe9e)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.00K | ± 1.39K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.95K | ± 402.91 | ops/s | 1.1x slower |
| prometheusAdd | 50.94K | ± 560.77 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.94K | ± 1.72K | ops/s | 1.3x slower |
| simpleclientInc | 6.59K | ± 13.45 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.31K | ± 64.74 | ops/s | 10x slower |
| simpleclientAdd | 6.28K | ± 268.45 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.49K | ± 59.47 | ops/s | 19x slower |
| openTelemetryAdd | 3.33K | ± 231.82 | ops/s | 19x slower |
| openTelemetryInc | 3.24K | ± 103.67 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.12K | ± 2.54K | ops/s | **fastest** |
| simpleclient | 4.41K | ± 12.50 | ops/s | 1.4x slower |
| prometheusNative | 2.74K | ± 350.24 | ops/s | 2.2x slower |
| openTelemetryClassic | 752.39 | ± 11.67 | ops/s | 8.1x slower |
| openTelemetryExponential | 641.65 | ± 64.92 | ops/s | 9.5x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.97K | ± 82.03 | ops/s | **fastest** |
| prometheusWriteToNull | 22.44K | ± 1.26K | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 502.01K | ± 3.50K | ops/s | **fastest** |
| prometheusWriteToByteArray | 494.83K | ± 7.59K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 480.24K | ± 2.12K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 472.01K | ± 3.67K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49941.912   ± 1724.205  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3334.308    ± 231.822  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3244.898    ± 103.665  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3485.657     ± 59.471  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50942.769    ± 560.768  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65002.011   ± 1385.803  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56948.929    ± 402.912  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6278.372    ± 268.449  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6592.810     ± 13.451  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6305.530     ± 64.739  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        752.391     ± 11.666  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        641.651     ± 64.920  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6119.737   ± 2544.609  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2742.068    ± 350.239  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4414.491     ± 12.496  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23971.023     ± 82.026  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22441.230   ± 1262.727  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     472013.606   ± 3666.320  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     480237.155   ± 2117.762  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     494828.404   ± 7586.026  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502005.872   ± 3500.318  ops/s
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

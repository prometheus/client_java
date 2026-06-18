# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-18T04:39:22Z
- **Commit:** [`d242a22`](https://github.com/prometheus/client_java/commit/d242a22d412f3b25a24213366e94308c8ed68027)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 56.44K | ± 202.50 | ops/s | **fastest** |
| prometheusInc | 53.99K | ± 20.03K | ops/s | 1.0x slower |
| prometheusAdd | 50.76K | ± 680.06 | ops/s | 1.1x slower |
| codahaleIncNoLabels | 48.82K | ± 1.45K | ops/s | 1.2x slower |
| simpleclientInc | 6.60K | ± 85.21 | ops/s | 8.5x slower |
| simpleclientNoLabelsInc | 6.41K | ± 156.48 | ops/s | 8.8x slower |
| simpleclientAdd | 6.27K | ± 259.32 | ops/s | 9.0x slower |
| openTelemetryInc | 3.20K | ± 427.63 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.20K | ± 257.66 | ops/s | 18x slower |
| openTelemetryAdd | 3.18K | ± 367.95 | ops/s | 18x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.24K | ± 858.04 | ops/s | **fastest** |
| simpleclient | 4.33K | ± 80.55 | ops/s | 1.4x slower |
| prometheusNative | 2.90K | ± 421.20 | ops/s | 2.2x slower |
| openTelemetryClassic | 745.19 | ± 62.42 | ops/s | 8.4x slower |
| openTelemetryExponential | 637.89 | ± 81.72 | ops/s | 9.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.68K | ± 346.26 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.55K | ± 759.44 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.18K | ± 23.96K | ops/s | **fastest** |
| prometheusWriteToByteArray | 484.68K | ± 11.02K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.80K | ± 5.60K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 465.16K | ± 15.54K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48821.866   ± 1448.034  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3176.593    ± 367.949  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3200.373    ± 427.629  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3198.730    ± 257.662  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50757.235    ± 680.060  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      53990.879  ± 20033.580  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56440.350    ± 202.502  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6267.866    ± 259.324  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6603.996     ± 85.211  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6411.801    ± 156.482  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        745.189     ± 62.418  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        637.894     ± 81.715  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6240.037    ± 858.041  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2900.181    ± 421.198  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4328.198     ± 80.549  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23545.971    ± 759.441  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23676.474    ± 346.264  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475802.077   ± 5601.437  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     465158.117  ± 15542.834  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     484680.193  ± 11022.440  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492183.967  ± 23962.243  ops/s
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

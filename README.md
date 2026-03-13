# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-13T04:23:17Z
- **Commit:** [`b81332e`](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.22K | ± 405.39 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.76K | ± 380.20 | ops/s | 1.2x slower |
| prometheusAdd | 51.56K | ± 197.93 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.37K | ± 7.82K | ops/s | 1.5x slower |
| simpleclientInc | 6.64K | ± 239.72 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.48K | ± 182.64 | ops/s | 10x slower |
| simpleclientAdd | 6.23K | ± 210.88 | ops/s | 11x slower |
| openTelemetryInc | 1.37K | ± 196.54 | ops/s | 48x slower |
| openTelemetryAdd | 1.26K | ± 42.44 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.22K | ± 59.82 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.16K | ± 1.74K | ops/s | **fastest** |
| simpleclient | 4.50K | ± 53.21 | ops/s | 1.1x slower |
| prometheusNative | 2.95K | ± 348.60 | ops/s | 1.8x slower |
| openTelemetryClassic | 695.12 | ± 20.14 | ops/s | 7.4x slower |
| openTelemetryExponential | 540.00 | ± 5.48 | ops/s | 9.6x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 494.51K | ± 3.25K | ops/s | **fastest** |
| prometheusWriteToByteArray | 489.36K | ± 3.95K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 484.74K | ± 3.81K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.18K | ± 7.25K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44367.332   ± 7816.342  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1263.785     ± 42.442  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1367.868    ± 196.535  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1215.438     ± 59.815  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51557.378    ± 197.931  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66219.678    ± 405.391  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56760.727    ± 380.200  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6232.594    ± 210.885  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6640.313    ± 239.721  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6475.856    ± 182.645  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        695.121     ± 20.143  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        539.999      ± 5.477  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5159.508   ± 1741.421  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2946.955    ± 348.598  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4504.862     ± 53.210  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     484740.952   ± 3811.500  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482182.915   ± 7251.710  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489355.272   ± 3952.897  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     494512.035   ± 3251.009  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-29T04:39:10Z
- **Commit:** [`f0a3b2e`](https://github.com/prometheus/client_java/commit/f0a3b2e46296428952756c95c9037982e7e9baa7)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.49K | ± 398.32 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.84K | ± 194.45 | ops/s | 1.1x slower |
| prometheusAdd | 47.88K | ± 331.88 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 42.51K | ± 1.17K | ops/s | 1.4x slower |
| simpleclientInc | 6.23K | ± 45.36 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 5.91K | ± 13.12 | ops/s | 10x slower |
| simpleclientAdd | 5.81K | ± 322.94 | ops/s | 10x slower |
| openTelemetryInc | 4.58K | ± 918.40 | ops/s | 13x slower |
| openTelemetryIncNoLabels | 3.98K | ± 338.58 | ops/s | 15x slower |
| openTelemetryAdd | 3.97K | ± 905.75 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.25K | ± 3.27K | ops/s | **fastest** |
| simpleclient | 4.55K | ± 50.34 | ops/s | 1.6x slower |
| prometheusNative | 2.88K | ± 203.16 | ops/s | 2.5x slower |
| openTelemetryClassic | 718.69 | ± 30.16 | ops/s | 10x slower |
| openTelemetryExponential | 567.20 | ± 24.97 | ops/s | 13x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.55K | ± 295.11 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.31K | ± 172.88 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 581.77K | ± 5.23K | ops/s | **fastest** |
| prometheusWriteToByteArray | 571.53K | ± 5.22K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 545.78K | ± 3.59K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 529.57K | ± 8.40K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42508.444   ± 1170.801  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3965.285    ± 905.749  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4575.025    ± 918.404  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3980.439    ± 338.580  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47883.797    ± 331.884  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59492.713    ± 398.319  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51841.347    ± 194.447  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5813.432    ± 322.944  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6227.617     ± 45.356  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5909.706     ± 13.121  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        718.693     ± 30.164  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        567.196     ± 24.971  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7245.403   ± 3266.145  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2876.040    ± 203.159  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4549.473     ± 50.342  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27305.363    ± 172.879  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27546.909    ± 295.108  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     529571.339   ± 8400.723  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     545784.489   ± 3590.253  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     571526.310   ± 5219.460  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     581768.330   ± 5234.409  ops/s
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

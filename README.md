# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-07T04:37:21Z
- **Commit:** [`edd160a`](https://github.com/prometheus/client_java/commit/edd160ab93254c80250d7cf58a1dcb399fef67a1)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.17K | ± 1.16K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.42K | ± 619.18 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.44K | ± 1.37K | ops/s | 1.3x slower |
| prometheusAdd | 47.90K | ± 5.16K | ops/s | 1.4x slower |
| simpleclientInc | 6.63K | ± 62.84 | ops/s | 9.8x slower |
| simpleclientAdd | 6.45K | ± 16.98 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.37K | ± 40.78 | ops/s | 10x slower |
| openTelemetryInc | 3.44K | ± 664.82 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.31K | ± 613.19 | ops/s | 20x slower |
| openTelemetryAdd | 3.24K | ± 313.49 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.88K | ± 1.50K | ops/s | **fastest** |
| simpleclient | 4.43K | ± 14.36 | ops/s | 1.1x slower |
| prometheusNative | 2.96K | ± 269.24 | ops/s | 1.7x slower |
| openTelemetryClassic | 755.07 | ± 9.45 | ops/s | 6.5x slower |
| openTelemetryExponential | 604.12 | ± 27.90 | ops/s | 8.1x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.86K | ± 1.05K | ops/s | **fastest** |
| openMetricsWriteToNull | 23.45K | ± 279.13 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 532.29K | ± 4.47K | ops/s | **fastest** |
| prometheusWriteToByteArray | 521.91K | ± 6.85K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 502.95K | ± 3.47K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 501.59K | ± 4.90K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48444.578   ± 1373.914  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3239.889    ± 313.491  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3444.910    ± 664.821  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3305.993    ± 613.192  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47900.709   ± 5163.111  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65172.475   ± 1162.417  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56418.306    ± 619.180  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6448.675     ± 16.985  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6632.634     ± 62.839  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6368.710     ± 40.781  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        755.068      ± 9.450  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        604.119     ± 27.899  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4882.150   ± 1500.642  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2957.155    ± 269.243  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4426.314     ± 14.363  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23452.129    ± 279.132  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23860.782   ± 1049.006  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     501590.430   ± 4904.956  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     502945.988   ± 3467.849  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     521907.059   ± 6854.577  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     532294.937   ± 4467.634  ops/s
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

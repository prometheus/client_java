# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-21T04:40:21Z
- **Commit:** [`da14412`](https://github.com/prometheus/client_java/commit/da144125367c0410df66120631f6cae5ead25fcb)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.11K | ± 674.79 | ops/s | **fastest** |
| prometheusNoLabelsInc | 30.59K | ± 1.13K | ops/s | 1.0x slower |
| codahaleIncNoLabels | 28.70K | ± 488.26 | ops/s | 1.1x slower |
| prometheusAdd | 28.43K | ± 53.74 | ops/s | 1.1x slower |
| simpleclientInc | 6.88K | ± 109.34 | ops/s | 4.5x slower |
| simpleclientNoLabelsInc | 6.63K | ± 19.00 | ops/s | 4.7x slower |
| simpleclientAdd | 6.55K | ± 129.36 | ops/s | 4.8x slower |
| openTelemetryIncNoLabels | 2.69K | ± 54.31 | ops/s | 12x slower |
| openTelemetryInc | 2.51K | ± 108.49 | ops/s | 12x slower |
| openTelemetryAdd | 2.37K | ± 162.42 | ops/s | 13x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.37K | ± 160.59 | ops/s | **fastest** |
| prometheusClassic | 2.94K | ± 674.77 | ops/s | 1.5x slower |
| prometheusNative | 1.96K | ± 79.56 | ops/s | 2.2x slower |
| openTelemetryClassic | 606.39 | ± 36.52 | ops/s | 7.2x slower |
| openTelemetryExponential | 450.27 | ± 23.13 | ops/s | 9.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 18.21K | ± 115.40 | ops/s | **fastest** |
| openMetricsWriteToNull | 18.15K | ± 207.05 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 319.78K | ± 1.37K | ops/s | **fastest** |
| prometheusWriteToByteArray | 317.05K | ± 1.68K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 297.08K | ± 764.11 | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 295.44K | ± 1.67K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28703.245    ± 488.261  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2366.376    ± 162.422  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2512.894    ± 108.495  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2690.595     ± 54.308  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28431.435     ± 53.741  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31106.144    ± 674.790  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30585.022   ± 1129.616  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6546.993    ± 129.362  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6877.858    ± 109.336  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6632.199     ± 19.004  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        606.386     ± 36.520  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        450.269     ± 23.130  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2943.281    ± 674.767  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1964.011     ± 79.558  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4369.180    ± 160.586  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18150.416    ± 207.051  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18206.069    ± 115.402  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     295443.015   ± 1671.163  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     297084.035    ± 764.109  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     317052.805   ± 1677.863  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     319776.481   ± 1369.719  ops/s
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

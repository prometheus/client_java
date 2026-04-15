# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-15T04:31:35Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.54K | ± 52.28 | ops/s | **fastest** |
| prometheusNoLabelsInc | 29.77K | ± 2.51K | ops/s | 1.1x slower |
| codahaleIncNoLabels | 29.72K | ± 1.25K | ops/s | 1.1x slower |
| prometheusAdd | 28.53K | ± 148.31 | ops/s | 1.1x slower |
| simpleclientInc | 6.75K | ± 45.29 | ops/s | 4.7x slower |
| simpleclientNoLabelsInc | 6.55K | ± 132.22 | ops/s | 4.8x slower |
| simpleclientAdd | 6.36K | ± 128.78 | ops/s | 5.0x slower |
| openTelemetryInc | 1.46K | ± 89.19 | ops/s | 22x slower |
| openTelemetryIncNoLabels | 1.44K | ± 68.36 | ops/s | 22x slower |
| openTelemetryAdd | 1.40K | ± 78.74 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.50K | ± 67.80 | ops/s | **fastest** |
| prometheusClassic | 2.66K | ± 553.51 | ops/s | 1.7x slower |
| prometheusNative | 1.83K | ± 30.69 | ops/s | 2.5x slower |
| openTelemetryClassic | 490.70 | ± 9.69 | ops/s | 9.2x slower |
| openTelemetryExponential | 403.71 | ± 41.11 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 310.81K | ± 3.49K | ops/s | **fastest** |
| prometheusWriteToByteArray | 307.89K | ± 4.53K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 290.27K | ± 3.93K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 287.40K | ± 4.74K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29723.583   ± 1245.361  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1399.216     ± 78.743  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1462.890     ± 89.189  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1442.423     ± 68.364  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28534.666    ± 148.309  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31539.761     ± 52.283  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      29772.691   ± 2512.117  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6357.557    ± 128.781  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6749.185     ± 45.289  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6547.678    ± 132.215  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        490.700      ± 9.691  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        403.705     ± 41.114  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2657.709    ± 553.513  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1831.550     ± 30.688  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4495.493     ± 67.802  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     287404.189   ± 4739.374  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     290268.494   ± 3928.206  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     307892.798   ± 4527.342  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     310808.360   ± 3493.233  ops/s
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

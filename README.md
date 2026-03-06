# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-06T04:23:09Z
- **Commit:** [`dfdec65`](https://github.com/prometheus/client_java/commit/dfdec650b9fb6d7280d1a9c34d799eae195e76a4)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 30.77K | ± 1.38K | ops/s | **fastest** |
| prometheusInc | 30.35K | ± 1.46K | ops/s | 1.0x slower |
| prometheusAdd | 28.47K | ± 12.38 | ops/s | 1.1x slower |
| codahaleIncNoLabels | 28.28K | ± 712.86 | ops/s | 1.1x slower |
| simpleclientInc | 7.04K | ± 56.16 | ops/s | 4.4x slower |
| simpleclientNoLabelsInc | 6.85K | ± 289.08 | ops/s | 4.5x slower |
| simpleclientAdd | 6.77K | ± 36.77 | ops/s | 4.5x slower |
| openTelemetryIncNoLabels | 1.44K | ± 41.03 | ops/s | 21x slower |
| openTelemetryAdd | 1.40K | ± 106.56 | ops/s | 22x slower |
| openTelemetryInc | 1.29K | ± 76.56 | ops/s | 24x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.55K | ± 54.14 | ops/s | **fastest** |
| prometheusClassic | 3.34K | ± 1.74K | ops/s | 1.4x slower |
| prometheusNative | 2.14K | ± 275.98 | ops/s | 2.1x slower |
| openTelemetryClassic | 525.57 | ± 18.04 | ops/s | 8.7x slower |
| openTelemetryExponential | 425.42 | ± 28.15 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 316.80K | ± 3.01K | ops/s | **fastest** |
| prometheusWriteToByteArray | 312.02K | ± 2.05K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 295.79K | ± 1.77K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 294.11K | ± 2.76K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28283.140    ± 712.864  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1399.064    ± 106.556  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1290.016     ± 76.555  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1439.782     ± 41.026  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28473.839     ± 12.376  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30351.170   ± 1464.955  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30768.323   ± 1378.763  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6771.298     ± 36.767  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7040.165     ± 56.165  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6847.058    ± 289.082  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        525.567     ± 18.035  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        425.420     ± 28.147  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3343.892   ± 1741.388  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2136.518    ± 275.980  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4546.896     ± 54.143  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     294106.043   ± 2763.459  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     295794.951   ± 1774.154  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     312019.462   ± 2046.633  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     316804.776   ± 3014.529  ops/s
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

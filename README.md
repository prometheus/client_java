# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-26T04:30:18Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.51K | ± 19.59 | ops/s | **fastest** |
| prometheusNoLabelsInc | 31.13K | ± 69.95 | ops/s | 1.0x slower |
| codahaleIncNoLabels | 28.45K | ± 1.11K | ops/s | 1.1x slower |
| prometheusAdd | 28.44K | ± 89.64 | ops/s | 1.1x slower |
| simpleclientNoLabelsInc | 6.95K | ± 77.28 | ops/s | 4.5x slower |
| simpleclientInc | 6.89K | ± 88.24 | ops/s | 4.6x slower |
| simpleclientAdd | 6.71K | ± 170.24 | ops/s | 4.7x slower |
| openTelemetryInc | 1.42K | ± 105.44 | ops/s | 22x slower |
| openTelemetryIncNoLabels | 1.41K | ± 71.97 | ops/s | 22x slower |
| openTelemetryAdd | 1.31K | ± 36.48 | ops/s | 24x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.51K | ± 24.45 | ops/s | **fastest** |
| prometheusClassic | 2.79K | ± 306.55 | ops/s | 1.6x slower |
| prometheusNative | 2.02K | ± 228.58 | ops/s | 2.2x slower |
| openTelemetryClassic | 520.55 | ± 33.85 | ops/s | 8.7x slower |
| openTelemetryExponential | 398.86 | ± 12.64 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 311.20K | ± 1.96K | ops/s | **fastest** |
| prometheusWriteToByteArray | 307.86K | ± 2.99K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 295.01K | ± 2.66K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 288.44K | ± 1.96K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28450.475   ± 1114.380  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1311.822     ± 36.483  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1420.716    ± 105.438  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1406.914     ± 71.970  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28441.042     ± 89.641  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31512.059     ± 19.586  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31132.775     ± 69.951  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6706.256    ± 170.237  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6891.719     ± 88.244  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6948.950     ± 77.280  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        520.553     ± 33.853  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        398.862     ± 12.638  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2794.234    ± 306.552  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2024.415    ± 228.576  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4513.416     ± 24.451  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     288443.155   ± 1957.665  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     295014.747   ± 2662.774  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     307857.570   ± 2989.441  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     311203.690   ± 1959.278  ops/s
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

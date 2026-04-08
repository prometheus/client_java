# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-08T04:30:52Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.24K | ± 542.75 | ops/s | **fastest** |
| prometheusNoLabelsInc | 30.53K | ± 1.01K | ops/s | 1.0x slower |
| codahaleIncNoLabels | 30.24K | ± 902.52 | ops/s | 1.0x slower |
| prometheusAdd | 27.81K | ± 1.24K | ops/s | 1.1x slower |
| simpleclientInc | 6.91K | ± 39.86 | ops/s | 4.5x slower |
| simpleclientNoLabelsInc | 6.77K | ± 259.05 | ops/s | 4.6x slower |
| simpleclientAdd | 6.68K | ± 71.24 | ops/s | 4.7x slower |
| openTelemetryAdd | 1.41K | ± 74.34 | ops/s | 22x slower |
| openTelemetryInc | 1.40K | ± 50.00 | ops/s | 22x slower |
| openTelemetryIncNoLabels | 1.40K | ± 45.61 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.47K | ± 43.26 | ops/s | **fastest** |
| prometheusClassic | 3.04K | ± 384.10 | ops/s | 1.5x slower |
| prometheusNative | 2.16K | ± 220.23 | ops/s | 2.1x slower |
| openTelemetryClassic | 510.18 | ± 8.31 | ops/s | 8.8x slower |
| openTelemetryExponential | 398.47 | ± 5.00 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 305.19K | ± 2.49K | ops/s | **fastest** |
| prometheusWriteToByteArray | 303.59K | ± 1.96K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 286.14K | ± 1.77K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 286.09K | ± 1.87K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      30241.809    ± 902.517  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1406.239     ± 74.338  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1404.176     ± 49.998  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1395.009     ± 45.610  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      27813.806   ± 1242.690  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31244.999    ± 542.753  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30531.483   ± 1009.682  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6678.563     ± 71.240  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6913.344     ± 39.863  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6774.633    ± 259.055  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        510.178      ± 8.307  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        398.467      ± 5.000  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3044.338    ± 384.103  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2162.857    ± 220.234  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4469.139     ± 43.257  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     286136.022   ± 1772.521  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     286092.546   ± 1865.939  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     303594.249   ± 1964.299  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     305192.033   ± 2491.708  ops/s
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

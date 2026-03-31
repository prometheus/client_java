# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-31T04:31:05Z
- **Commit:** [`deb782f`](https://github.com/prometheus/client_java/commit/deb782f9fce60ffb1308a98b661c0a1ccb79a82b)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| codahaleIncNoLabels | 31.28K | ± 706.02 | ops/s | **fastest** |
| prometheusInc | 31.08K | ± 837.76 | ops/s | 1.0x slower |
| prometheusNoLabelsInc | 30.41K | ± 918.63 | ops/s | 1.0x slower |
| prometheusAdd | 28.15K | ± 877.33 | ops/s | 1.1x slower |
| simpleclientInc | 7.00K | ± 58.93 | ops/s | 4.5x slower |
| simpleclientNoLabelsInc | 6.63K | ± 119.86 | ops/s | 4.7x slower |
| simpleclientAdd | 6.58K | ± 288.36 | ops/s | 4.8x slower |
| openTelemetryInc | 1.38K | ± 51.93 | ops/s | 23x slower |
| openTelemetryIncNoLabels | 1.37K | ± 127.51 | ops/s | 23x slower |
| openTelemetryAdd | 1.33K | ± 57.75 | ops/s | 24x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.52K | ± 44.37 | ops/s | **fastest** |
| prometheusClassic | 2.59K | ± 241.33 | ops/s | 1.7x slower |
| prometheusNative | 2.11K | ± 224.70 | ops/s | 2.1x slower |
| openTelemetryClassic | 512.32 | ± 25.70 | ops/s | 8.8x slower |
| openTelemetryExponential | 384.56 | ± 7.28 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 315.57K | ± 1.93K | ops/s | **fastest** |
| prometheusWriteToByteArray | 308.80K | ± 2.30K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 293.45K | ± 1.21K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 291.46K | ± 1.57K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      31278.092    ± 706.022  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1325.980     ± 57.746  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1381.829     ± 51.933  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1369.190    ± 127.512  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28146.088    ± 877.329  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31079.600    ± 837.756  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30406.850    ± 918.629  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6576.070    ± 288.365  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7003.134     ± 58.927  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6631.079    ± 119.858  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        512.325     ± 25.701  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        384.558      ± 7.281  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2591.319    ± 241.330  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2108.536    ± 224.697  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4519.381     ± 44.372  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     291459.024   ± 1570.133  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     293453.372   ± 1211.705  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     308802.184   ± 2301.580  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     315567.234   ± 1927.097  ops/s
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

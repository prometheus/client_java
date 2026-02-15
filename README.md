# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-15T04:28:31Z
- **Commit:** [`bcec4c7`](https://github.com/prometheus/client_java/commit/bcec4c72721c03f05b5999e208f51ad6af4c6df7)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.82K | ± 412.74 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.95K | ± 210.07 | ops/s | 1.2x slower |
| prometheusAdd | 51.44K | ± 223.47 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.12K | ± 1.15K | ops/s | 1.4x slower |
| simpleclientInc | 6.67K | ± 130.62 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.54K | ± 216.02 | ops/s | 10x slower |
| simpleclientAdd | 6.51K | ± 25.27 | ops/s | 10x slower |
| openTelemetryInc | 1.55K | ± 244.66 | ops/s | 43x slower |
| openTelemetryIncNoLabels | 1.49K | ± 268.50 | ops/s | 45x slower |
| openTelemetryAdd | 1.45K | ± 318.16 | ops/s | 46x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.29K | ± 893.56 | ops/s | **fastest** |
| simpleclient | 4.56K | ± 26.88 | ops/s | 1.4x slower |
| prometheusNative | 2.94K | ± 254.89 | ops/s | 2.1x slower |
| openTelemetryClassic | 732.12 | ± 11.61 | ops/s | 8.6x slower |
| openTelemetryExponential | 548.70 | ± 19.83 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 484.20K | ± 5.14K | ops/s | **fastest** |
| prometheusWriteToByteArray | 479.46K | ± 1.94K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 473.99K | ± 3.41K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 461.03K | ± 3.37K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49124.306   ± 1145.813  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1450.288    ± 318.156  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1549.209    ± 244.664  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1494.806    ± 268.505  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51436.116    ± 223.472  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66819.044    ± 412.738  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56953.314    ± 210.069  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6507.856     ± 25.272  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6668.187    ± 130.622  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6544.131    ± 216.022  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        732.116     ± 11.608  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        548.697     ± 19.830  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6294.002    ± 893.562  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2936.472    ± 254.892  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4560.142     ± 26.879  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     461032.765   ± 3365.337  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     473989.575   ± 3414.593  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     479464.377   ± 1936.780  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     484199.147   ± 5140.865  ops/s
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

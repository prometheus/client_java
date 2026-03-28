# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-28T04:27:24Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.65K | ± 169.62 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.11K | ± 835.95 | ops/s | 1.2x slower |
| prometheusAdd | 50.16K | ± 1.52K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.42K | ± 256.61 | ops/s | 1.4x slower |
| simpleclientInc | 6.58K | ± 161.34 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.44K | ± 176.01 | ops/s | 10x slower |
| simpleclientAdd | 6.15K | ± 236.74 | ops/s | 11x slower |
| openTelemetryAdd | 1.39K | ± 219.75 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.32K | ± 182.99 | ops/s | 50x slower |
| openTelemetryInc | 1.28K | ± 17.27 | ops/s | 51x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.10K | ± 2.65K | ops/s | **fastest** |
| simpleclient | 4.40K | ± 119.28 | ops/s | 1.4x slower |
| prometheusNative | 3.02K | ± 362.87 | ops/s | 2.0x slower |
| openTelemetryClassic | 696.60 | ± 22.24 | ops/s | 8.8x slower |
| openTelemetryExponential | 567.80 | ± 24.55 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToByteArray | 470.02K | ± 5.10K | ops/s | **fastest** |
| prometheusWriteToNull | 464.83K | ± 2.60K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 462.14K | ± 5.25K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 458.36K | ± 13.55K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47421.861    ± 256.610  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1394.748    ± 219.745  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1275.633     ± 17.265  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1319.346    ± 182.987  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50164.585   ± 1517.525  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65646.875    ± 169.622  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56110.230    ± 835.953  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6154.590    ± 236.738  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6577.505    ± 161.339  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6435.788    ± 176.007  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        696.595     ± 22.238  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        567.797     ± 24.547  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6102.531   ± 2653.521  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3020.602    ± 362.868  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4398.813    ± 119.280  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     470024.630   ± 5102.866  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     458359.370  ± 13550.067  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     462139.132   ± 5252.267  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     464825.316   ± 2597.687  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-24T04:32:43Z
- **Commit:** [`5699469`](https://github.com/prometheus/client_java/commit/5699469d345b9d3aaf3d6c0e5e76de2959477269)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.99K | ± 432.43 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.81K | ± 402.58 | ops/s | 1.2x slower |
| prometheusAdd | 50.71K | ± 816.80 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.50K | ± 833.27 | ops/s | 1.3x slower |
| simpleclientInc | 6.66K | ± 65.27 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.28K | ± 190.55 | ops/s | 11x slower |
| simpleclientAdd | 6.15K | ± 230.09 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 4.05K | ± 463.16 | ops/s | 16x slower |
| openTelemetryAdd | 3.39K | ± 259.00 | ops/s | 19x slower |
| openTelemetryInc | 3.21K | ± 178.84 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.46K | ± 1.03K | ops/s | **fastest** |
| simpleclient | 4.40K | ± 28.35 | ops/s | 1.2x slower |
| prometheusNative | 3.01K | ± 131.61 | ops/s | 1.8x slower |
| openTelemetryClassic | 784.38 | ± 18.38 | ops/s | 7.0x slower |
| openTelemetryExponential | 673.20 | ± 29.54 | ops/s | 8.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 483.00K | ± 5.07K | ops/s | **fastest** |
| prometheusWriteToByteArray | 480.28K | ± 5.42K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 463.96K | ± 5.40K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 460.54K | ± 7.94K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49497.479    ± 833.270  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3389.938    ± 259.001  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3208.598    ± 178.837  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4045.961    ± 463.163  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50707.661    ± 816.804  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65994.078    ± 432.433  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56808.571    ± 402.585  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6154.857    ± 230.086  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6655.674     ± 65.266  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6280.186    ± 190.553  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        784.379     ± 18.384  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        673.198     ± 29.540  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5460.868   ± 1030.614  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3011.900    ± 131.613  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4403.317     ± 28.349  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     460535.292   ± 7944.395  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     463963.141   ± 5403.821  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     480276.555   ± 5421.288  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     482997.581   ± 5067.691  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-09T04:27:37Z
- **Commit:** [`e6eb2f9`](https://github.com/prometheus/client_java/commit/e6eb2f91d6da13485a83c4eab5171f510382f800)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.83K | ± 1.69K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.05K | ± 819.44 | ops/s | 1.2x slower |
| prometheusAdd | 51.31K | ± 543.92 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 46.57K | ± 1.06K | ops/s | 1.4x slower |
| simpleclientInc | 6.78K | ± 15.40 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.49K | ± 170.81 | ops/s | 10x slower |
| simpleclientAdd | 6.14K | ± 37.79 | ops/s | 11x slower |
| openTelemetryAdd | 1.47K | ± 162.10 | ops/s | 45x slower |
| openTelemetryInc | 1.24K | ± 7.47 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.16K | ± 41.16 | ops/s | 57x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.97K | ± 1.85K | ops/s | **fastest** |
| simpleclient | 4.54K | ± 78.65 | ops/s | 1.3x slower |
| prometheusNative | 3.21K | ± 139.54 | ops/s | 1.9x slower |
| openTelemetryClassic | 672.23 | ± 26.76 | ops/s | 8.9x slower |
| openTelemetryExponential | 549.11 | ± 11.40 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 493.14K | ± 2.98K | ops/s | **fastest** |
| prometheusWriteToByteArray | 491.07K | ± 5.45K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.10K | ± 9.34K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 478.17K | ± 7.13K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      46565.440   ± 1058.122  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1466.648    ± 162.105  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1241.587      ± 7.475  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1163.586     ± 41.157  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51305.169    ± 543.917  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65827.243   ± 1688.992  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56050.455    ± 819.442  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6137.557     ± 37.789  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6776.830     ± 15.402  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6493.454    ± 170.814  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        672.226     ± 26.756  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        549.114     ± 11.404  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5965.782   ± 1851.078  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3212.067    ± 139.542  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4535.494     ± 78.646  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478168.274   ± 7131.207  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482102.528   ± 9343.751  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491072.402   ± 5447.034  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     493139.101   ± 2979.658  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-18T04:29:18Z
- **Commit:** [`01f53e9`](https://github.com/prometheus/client_java/commit/01f53e945edfc337b9ed13c0b5c28c8a170c3a48)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.97K | ± 1.77K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.78K | ± 620.89 | ops/s | 1.2x slower |
| prometheusAdd | 51.40K | ± 414.06 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.67K | ± 1.08K | ops/s | 1.4x slower |
| simpleclientInc | 6.73K | ± 64.25 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.68K | ± 17.11 | ops/s | 9.9x slower |
| simpleclientAdd | 6.42K | ± 209.06 | ops/s | 10x slower |
| openTelemetryAdd | 1.43K | ± 212.50 | ops/s | 46x slower |
| openTelemetryInc | 1.31K | ± 25.94 | ops/s | 50x slower |
| openTelemetryIncNoLabels | 1.24K | ± 35.32 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.87K | ± 1.61K | ops/s | **fastest** |
| simpleclient | 4.53K | ± 34.02 | ops/s | 1.1x slower |
| prometheusNative | 2.89K | ± 303.53 | ops/s | 1.7x slower |
| openTelemetryClassic | 715.78 | ± 33.76 | ops/s | 6.8x slower |
| openTelemetryExponential | 539.54 | ± 7.29 | ops/s | 9.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 493.37K | ± 2.36K | ops/s | **fastest** |
| prometheusWriteToByteArray | 490.39K | ± 1.53K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 479.39K | ± 3.47K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 478.27K | ± 5.70K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48669.324   ± 1077.316  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1434.023    ± 212.497  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1309.553     ± 25.943  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1243.721     ± 35.318  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51404.454    ± 414.063  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65967.595   ± 1768.160  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56778.516    ± 620.886  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6422.607    ± 209.059  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6725.250     ± 64.253  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6678.983     ± 17.106  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        715.784     ± 33.761  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        539.542      ± 7.286  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4874.541   ± 1606.072  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2888.539    ± 303.531  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4528.246     ± 34.016  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478269.756   ± 5700.843  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     479394.377   ± 3467.368  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     490389.402   ± 1526.823  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     493372.721   ± 2361.725  ops/s
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

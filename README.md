# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-26T04:01:22Z
- **Commit:** [`63149ae`](https://github.com/prometheus/client_java/commit/63149ae439676abf4c2fc2c348d6e87e7156369c)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 58.93K | ± 73.58 | ops/s |
| prometheusNoLabelsInc | 50.77K | ± 157.70 | ops/s |
| prometheusAdd | 47.36K | ± 612.13 | ops/s |
| codahaleIncNoLabels | 43.53K | ± 1.95K | ops/s |
| openTelemetryIncNoLabels | 17.11K | ± 204.71 | ops/s |
| openTelemetryInc | 14.31K | ± 334.53 | ops/s |
| openTelemetryAdd | 12.21K | ± 24.63 | ops/s |
| simpleclientInc | 6.12K | ± 59.08 | ops/s |
| simpleclientNoLabelsInc | 5.91K | ± 79.62 | ops/s |
| simpleclientAdd | 5.85K | ± 359.45 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.58K | ± 101.56 | ops/s |
| prometheusClassic | 6.39K | ± 1.47K | ops/s |
| prometheusClassicSingleThread | 5.81K | ± 24.92 | ops/s |
| simpleclient | 4.51K | ± 56.42 | ops/s |
| prometheusNative | 2.88K | ± 243.41 | ops/s |
| openTelemetryClassic | 846.37 | ± 75.47 | ops/s |
| openTelemetryExponential | 699.56 | ± 67.10 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.62K | ± 160.75 | ops/s |
| openMetricsWriteToNull | 27.59K | ± 334.70 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 580.43K | ± 2.98K | ops/s |
| prometheusWriteToByteArray | 569.17K | ± 15.05K | ops/s |
| openMetricsWriteToNull | 554.63K | ± 3.15K | ops/s |
| openMetricsWriteToByteArray | 541.24K | ± 4.14K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43532.459   ± 1949.130  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12213.660     ± 24.630  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14305.061    ± 334.534  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17108.362    ± 204.712  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47362.721    ± 612.127  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58934.988     ± 73.583  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50773.015    ± 157.702  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5845.910    ± 359.451  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6123.106     ± 59.076  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5910.581     ± 79.622  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        846.367     ± 75.467  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        699.564     ± 67.105  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6388.722   ± 1465.320  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13577.228    ± 101.562  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5805.062     ± 24.923  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2881.322    ± 243.410  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4511.851     ± 56.419  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27587.090    ± 334.697  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27618.640    ± 160.749  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     541239.252   ± 4135.846  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     554632.993   ± 3153.137  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     569165.237  ± 15046.097  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     580431.131   ± 2977.379  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

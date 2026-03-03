# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-03T04:26:30Z
- **Commit:** [`b9906c1`](https://github.com/prometheus/client_java/commit/b9906c11d6b9125b642ffbe6527dfe727880090b)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.67K | ± 1.65K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.50K | ± 1.43K | ops/s | 1.2x slower |
| prometheusAdd | 51.28K | ± 338.40 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.61K | ± 917.18 | ops/s | 1.3x slower |
| simpleclientAdd | 6.55K | ± 17.09 | ops/s | 10x slower |
| simpleclientInc | 6.50K | ± 213.52 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.43K | ± 210.36 | ops/s | 10x slower |
| openTelemetryInc | 1.24K | ± 15.32 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.22K | ± 29.27 | ops/s | 54x slower |
| openTelemetryAdd | 1.21K | ± 35.28 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.55K | ± 1.39K | ops/s | **fastest** |
| simpleclient | 4.55K | ± 20.00 | ops/s | 1.2x slower |
| prometheusNative | 3.02K | ± 260.28 | ops/s | 1.8x slower |
| openTelemetryClassic | 692.31 | ± 13.07 | ops/s | 8.0x slower |
| openTelemetryExponential | 534.04 | ± 10.58 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 488.09K | ± 3.00K | ops/s | **fastest** |
| prometheusWriteToByteArray | 485.07K | ± 1.38K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 477.07K | ± 4.69K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.12K | ± 4.83K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49614.168    ± 917.179  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1209.202     ± 35.283  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1239.905     ± 15.325  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1216.602     ± 29.271  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51278.394    ± 338.399  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65670.949   ± 1653.438  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56503.194   ± 1432.773  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6545.997     ± 17.092  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6499.220    ± 213.518  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6434.865    ± 210.360  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        692.309     ± 13.074  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        534.037     ± 10.581  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5548.079   ± 1389.946  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3020.675    ± 260.281  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4550.423     ± 19.997  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473120.200   ± 4831.418  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     477071.743   ± 4694.500  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485069.887   ± 1377.818  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     488094.081   ± 3000.181  ops/s
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

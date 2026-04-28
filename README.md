# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-28T04:36:32Z
- **Commit:** [`fa68aa7`](https://github.com/prometheus/client_java/commit/fa68aa7789c53d54ea1783f120194a3feae7e7b8)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.49K | ± 645.06 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.40K | ± 992.80 | ops/s | 1.2x slower |
| prometheusAdd | 48.92K | ± 651.14 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.02K | ± 312.37 | ops/s | 1.4x slower |
| simpleclientInc | 6.14K | ± 143.27 | ops/s | 9.7x slower |
| simpleclientAdd | 6.06K | ± 187.32 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.03K | ± 196.49 | ops/s | 9.9x slower |
| openTelemetryIncNoLabels | 5.00K | ± 646.25 | ops/s | 12x slower |
| openTelemetryInc | 4.37K | ± 128.12 | ops/s | 14x slower |
| openTelemetryAdd | 4.17K | ± 890.84 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.45K | ± 588.95 | ops/s | **fastest** |
| simpleclient | 4.38K | ± 51.47 | ops/s | 1.0x slower |
| prometheusNative | 3.04K | ± 263.26 | ops/s | 1.5x slower |
| openTelemetryClassic | 751.21 | ± 10.19 | ops/s | 5.9x slower |
| openTelemetryExponential | 574.89 | ± 13.24 | ops/s | 7.7x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 560.02K | ± 2.27K | ops/s | **fastest** |
| prometheusWriteToByteArray | 549.95K | ± 1.82K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 533.31K | ± 5.11K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 528.75K | ± 2.05K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44024.721    ± 312.369  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4171.385    ± 890.838  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4366.102    ± 128.119  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5000.888    ± 646.249  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48920.876    ± 651.137  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59492.234    ± 645.055  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51402.710    ± 992.795  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6056.753    ± 187.315  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6143.041    ± 143.273  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6028.696    ± 196.492  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        751.209     ± 10.190  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        574.886     ± 13.244  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4454.513    ± 588.954  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3040.267    ± 263.258  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4382.323     ± 51.466  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     528750.895   ± 2054.031  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     533306.423   ± 5106.627  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     549952.456   ± 1820.690  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     560015.945   ± 2271.228  ops/s
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

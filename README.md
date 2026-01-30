# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-01-30T04:22:06Z
- **Commit:** [`958297d`](https://github.com/prometheus/client_java/commit/958297d5f2802bbe3dc70709b645df557461be9b)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 67.11K | ± 392.13 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.47K | ± 1.03K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.67K | ± 1.22K | ops/s | 1.4x slower |
| prometheusAdd | 43.38K | ± 12.13K | ops/s | 1.5x slower |
| simpleclientInc | 6.71K | ± 131.89 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.63K | ± 121.80 | ops/s | 10x slower |
| simpleclientAdd | 6.56K | ± 9.07 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.36K | ± 159.09 | ops/s | 49x slower |
| openTelemetryAdd | 1.32K | ± 51.71 | ops/s | 51x slower |
| openTelemetryInc | 1.22K | ± 35.02 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.25K | ± 200.51 | ops/s | **fastest** |
| simpleclient | 4.51K | ± 23.94 | ops/s | 1.2x slower |
| prometheusNative | 3.13K | ± 157.61 | ops/s | 1.7x slower |
| openTelemetryClassic | 691.69 | ± 55.87 | ops/s | 7.6x slower |
| openTelemetryExponential | 527.91 | ± 15.32 | ops/s | 9.9x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 546.04K | ± 10.72K | ops/s | **fastest** |
| prometheusWriteToByteArray | 535.36K | ± 4.67K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 517.85K | ± 7.16K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 513.08K | ± 5.22K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48666.143   ± 1219.918  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1322.274     ± 51.708  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1219.875     ± 35.017  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1362.196    ± 159.090  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      43377.024  ± 12134.554  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67108.333    ± 392.133  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56473.200   ± 1033.894  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6556.588      ± 9.067  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6706.615    ± 131.891  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6632.321    ± 121.799  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        691.688     ± 55.869  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        527.908     ± 15.323  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5251.636    ± 200.512  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3129.124    ± 157.609  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4512.155     ± 23.943  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     513080.631   ± 5219.986  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     517851.719   ± 7160.020  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     535357.745   ± 4671.852  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     546036.994  ± 10717.349  ops/s
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

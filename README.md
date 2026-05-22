# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-22T04:38:35Z
- **Commit:** [`a241c16`](https://github.com/prometheus/client_java/commit/a241c165927d3cbb91b97eedd52de9c9eff595d0)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.07K | ± 919.51 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.46K | ± 638.69 | ops/s | 1.1x slower |
| prometheusAdd | 51.10K | ± 774.46 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.29K | ± 745.40 | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 36.17 | ops/s | 9.8x slower |
| simpleclientAdd | 6.46K | ± 86.31 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.38K | ± 24.49 | ops/s | 10x slower |
| openTelemetryInc | 3.38K | ± 316.11 | ops/s | 19x slower |
| openTelemetryAdd | 3.33K | ± 222.59 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.20K | ± 225.07 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.72K | ± 1.09K | ops/s | **fastest** |
| simpleclient | 4.39K | ± 38.27 | ops/s | 1.3x slower |
| prometheusNative | 3.06K | ± 204.82 | ops/s | 1.9x slower |
| openTelemetryClassic | 762.70 | ± 13.94 | ops/s | 7.5x slower |
| openTelemetryExponential | 658.28 | ± 53.80 | ops/s | 8.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.80K | ± 1.08K | ops/s | **fastest** |
| prometheusWriteToNull | 22.61K | ± 731.84 | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 502.23K | ± 10.79K | ops/s | **fastest** |
| prometheusWriteToByteArray | 498.57K | ± 5.80K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 490.07K | ± 2.42K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 474.09K | ± 12.85K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49285.179    ± 745.402  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3332.512    ± 222.589  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3382.705    ± 316.107  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3201.880    ± 225.069  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51104.767    ± 774.461  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64072.796    ± 919.514  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56459.911    ± 638.692  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6455.908     ± 86.313  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6557.866     ± 36.170  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6376.824     ± 24.487  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        762.696     ± 13.945  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        658.275     ± 53.796  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5723.288   ± 1085.929  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3058.452    ± 204.820  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4392.319     ± 38.266  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23795.139   ± 1078.785  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22610.092    ± 731.841  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     474091.894  ± 12854.087  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490074.549   ± 2419.439  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     498572.945   ± 5801.593  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502233.115  ± 10792.813  ops/s
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

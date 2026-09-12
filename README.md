# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-12T03:51:52Z
- **Commit:** [`fcc4466`](https://github.com/prometheus/client_java/commit/fcc4466e36262d155e3f7b0211fd8da6b013e3af)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 60.21K | ± 1.15K | ops/s |
| prometheusNoLabelsInc | 51.55K | ± 441.96 | ops/s |
| prometheusAdd | 46.54K | ± 4.12K | ops/s |
| codahaleIncNoLabels | 40.42K | ± 5.66K | ops/s |
| openTelemetryIncNoLabels | 17.18K | ± 124.49 | ops/s |
| openTelemetryInc | 12.55K | ± 1.75K | ops/s |
| openTelemetryAdd | 12.19K | ± 41.22 | ops/s |
| simpleclientInc | 6.14K | ± 37.27 | ops/s |
| simpleclientAdd | 6.13K | ± 7.11 | ops/s |
| simpleclientNoLabelsInc | 5.87K | ± 58.01 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 14.08K | ± 8.83 | ops/s |
| prometheusClassic | 7.34K | ± 25.20 | ops/s |
| prometheusClassicSingleThread | 5.86K | ± 12.11 | ops/s |
| simpleclient | 4.56K | ± 101.49 | ops/s |
| prometheusNative | 3.12K | ± 69.11 | ops/s |
| openTelemetryClassic | 836.24 | ± 45.67 | ops/s |
| openTelemetryExponential | 645.47 | ± 74.81 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.74K | ± 278.98 | ops/s |
| openMetricsWriteToNull | 27.46K | ± 202.12 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 583.91K | ± 6.30K | ops/s |
| prometheusWriteToByteArray | 568.92K | ± 4.13K | ops/s |
| openMetricsWriteToNull | 546.21K | ± 7.10K | ops/s |
| openMetricsWriteToByteArray | 537.91K | ± 9.38K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      40418.670   ± 5659.830  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12185.929     ± 41.220  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      12548.404   ± 1746.372  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17181.056    ± 124.493  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      46541.085   ± 4117.949  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60206.264   ± 1146.684  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51545.372    ± 441.963  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6125.018      ± 7.106  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6137.392     ± 37.273  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5867.431     ± 58.006  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        836.237     ± 45.674  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        645.465     ± 74.811  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7343.661     ± 25.205  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14083.809      ± 8.835  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5855.337     ± 12.114  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3119.259     ± 69.107  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4564.685    ± 101.488  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27458.406    ± 202.117  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27739.045    ± 278.981  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     537908.867   ± 9382.777  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     546212.596   ± 7100.472  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     568923.076   ± 4126.646  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     583908.503   ± 6304.382  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-14T03:55:47Z
- **Commit:** [`fcc4466`](https://github.com/prometheus/client_java/commit/fcc4466e36262d155e3f7b0211fd8da6b013e3af)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 56.61K | ± 2.10K | ops/s |
| prometheusNoLabelsInc | 50.24K | ± 325.45 | ops/s |
| prometheusAdd | 47.58K | ± 641.41 | ops/s |
| codahaleIncNoLabels | 42.44K | ± 1.74K | ops/s |
| openTelemetryIncNoLabels | 17.16K | ± 156.34 | ops/s |
| openTelemetryInc | 14.06K | ± 386.84 | ops/s |
| openTelemetryAdd | 12.11K | ± 36.81 | ops/s |
| simpleclientInc | 6.10K | ± 133.99 | ops/s |
| simpleclientAdd | 5.96K | ± 221.89 | ops/s |
| simpleclientNoLabelsInc | 5.87K | ± 64.16 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.94K | ± 56.79 | ops/s |
| prometheusClassicSingleThread | 5.84K | ± 19.36 | ops/s |
| prometheusClassic | 5.67K | ± 1.34K | ops/s |
| simpleclient | 4.56K | ± 53.32 | ops/s |
| prometheusNative | 2.98K | ± 272.38 | ops/s |
| openTelemetryClassic | 850.94 | ± 111.70 | ops/s |
| openTelemetryExponential | 687.97 | ± 51.31 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.30K | ± 612.31 | ops/s |
| openMetricsWriteToNull | 27.19K | ± 346.57 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 585.67K | ± 6.12K | ops/s |
| prometheusWriteToByteArray | 571.33K | ± 8.94K | ops/s |
| openMetricsWriteToNull | 549.94K | ± 5.67K | ops/s |
| openMetricsWriteToByteArray | 535.52K | ± 4.68K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42444.432   ± 1743.420  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12110.148     ± 36.810  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14059.794    ± 386.841  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17164.932    ± 156.344  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47582.907    ± 641.410  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      56605.166   ± 2102.613  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50240.635    ± 325.448  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5957.762    ± 221.892  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6104.918    ± 133.993  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5871.672     ± 64.157  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        850.938    ± 111.699  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        687.973     ± 51.306  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5669.020   ± 1337.898  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13935.516     ± 56.785  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5838.370     ± 19.363  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2977.935    ± 272.380  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4559.378     ± 53.319  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27185.840    ± 346.567  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27303.678    ± 612.313  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     535516.782   ± 4683.064  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     549943.314   ± 5666.927  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     571333.651   ± 8941.633  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     585667.928   ± 6122.286  ops/s
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

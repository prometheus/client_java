# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-27T05:22:04Z
- **Commit:** [`5aed579`](https://github.com/prometheus/client_java/commit/5aed5790d04fb67adf834d640c828ff51fae43e6)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 65.48K | ± 1.75K | ops/s |
| prometheusNoLabelsInc | 56.85K | ± 364.92 | ops/s |
| prometheusAdd | 51.01K | ± 406.14 | ops/s |
| codahaleIncNoLabels | 48.06K | ± 1.59K | ops/s |
| openTelemetryIncNoLabels | 17.94K | ± 948.95 | ops/s |
| openTelemetryInc | 15.03K | ± 143.19 | ops/s |
| openTelemetryAdd | 12.88K | ± 74.99 | ops/s |
| simpleclientInc | 6.56K | ± 38.66 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 20.70 | ops/s |
| simpleclientAdd | 6.15K | ± 226.95 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.29K | ± 30.71 | ops/s |
| prometheusClassic | 5.66K | ± 1.43K | ops/s |
| prometheusClassicSingleThread | 4.57K | ± 37.90 | ops/s |
| simpleclient | 4.41K | ± 10.02 | ops/s |
| prometheusNative | 2.76K | ± 298.20 | ops/s |
| openTelemetryClassic | 858.44 | ± 58.57 | ops/s |
| openTelemetryExponential | 769.45 | ± 138.97 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 24.04K | ± 590.93 | ops/s |
| prometheusWriteToNull | 23.79K | ± 1.01K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 496.22K | ± 3.10K | ops/s |
| prometheusWriteToByteArray | 492.69K | ± 2.90K | ops/s |
| openMetricsWriteToNull | 477.53K | ± 5.12K | ops/s |
| openMetricsWriteToByteArray | 473.07K | ± 1.49K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48058.906   ± 1592.027  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12883.082     ± 74.986  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15031.628    ± 143.185  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17938.117    ± 948.953  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51014.653    ± 406.140  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65482.862   ± 1750.297  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56854.733    ± 364.920  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6147.150    ± 226.954  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6557.641     ± 38.660  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6347.858     ± 20.701  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        858.438     ± 58.572  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        769.454    ± 138.972  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5660.159   ± 1429.830  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12289.089     ± 30.713  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4574.335     ± 37.898  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2763.865    ± 298.203  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4411.871     ± 10.017  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24035.953    ± 590.934  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23791.976   ± 1014.240  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473074.475   ± 1490.714  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     477531.408   ± 5121.176  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     492685.199   ± 2904.592  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     496221.936   ± 3097.636  ops/s
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

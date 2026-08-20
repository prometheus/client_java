# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-20T03:56:11Z
- **Commit:** [`92f8344`](https://github.com/prometheus/client_java/commit/92f834476837cab3d281d4b435937e11a2ff7729)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.35K | ± 192.98 | ops/s |
| prometheusNoLabelsInc | 56.09K | ± 1.26K | ops/s |
| prometheusAdd | 50.80K | ± 1.11K | ops/s |
| codahaleIncNoLabels | 50.17K | ± 846.20 | ops/s |
| openTelemetryIncNoLabels | 18.52K | ± 22.90 | ops/s |
| openTelemetryInc | 14.81K | ± 219.82 | ops/s |
| openTelemetryAdd | 12.70K | ± 454.74 | ops/s |
| simpleclientInc | 6.56K | ± 48.24 | ops/s |
| simpleclientNoLabelsInc | 6.36K | ± 37.88 | ops/s |
| simpleclientAdd | 6.30K | ± 236.01 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.27K | ± 62.06 | ops/s |
| prometheusClassic | 5.17K | ± 1.26K | ops/s |
| prometheusClassicSingleThread | 4.58K | ± 23.49 | ops/s |
| simpleclient | 4.39K | ± 34.99 | ops/s |
| prometheusNative | 3.22K | ± 91.98 | ops/s |
| openTelemetryClassic | 781.98 | ± 37.21 | ops/s |
| openTelemetryExponential | 689.30 | ± 77.89 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.73K | ± 370.15 | ops/s |
| openMetricsWriteToNull | 23.55K | ± 957.78 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 495.20K | ± 3.43K | ops/s |
| prometheusWriteToByteArray | 492.52K | ± 4.24K | ops/s |
| openMetricsWriteToNull | 479.66K | ± 2.57K | ops/s |
| openMetricsWriteToByteArray | 469.79K | ± 6.40K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50167.990    ± 846.203  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12698.303    ± 454.739  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14813.713    ± 219.825  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18522.311     ± 22.904  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50804.422   ± 1107.503  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66349.893    ± 192.978  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56086.423   ± 1263.950  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6298.201    ± 236.008  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6564.361     ± 48.240  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6355.488     ± 37.881  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        781.984     ± 37.212  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        689.295     ± 77.892  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5171.046   ± 1260.284  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12274.570     ± 62.059  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4579.072     ± 23.486  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3218.666     ± 91.977  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4385.188     ± 34.992  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23549.470    ± 957.784  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23733.711    ± 370.148  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     469787.827   ± 6404.651  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     479659.033   ± 2571.798  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     492518.147   ± 4238.866  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     495202.770   ± 3432.271  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-24T04:00:40Z
- **Commit:** [`ac0d68a`](https://github.com/prometheus/client_java/commit/ac0d68a62886473ac4afd736602760e97024b528)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.22K | ± 367.54 | ops/s |
| prometheusNoLabelsInc | 56.06K | ± 1.21K | ops/s |
| prometheusAdd | 51.32K | ± 371.09 | ops/s |
| codahaleIncNoLabels | 43.75K | ± 7.74K | ops/s |
| openTelemetryIncNoLabels | 18.45K | ± 161.52 | ops/s |
| openTelemetryInc | 14.92K | ± 276.94 | ops/s |
| openTelemetryAdd | 12.96K | ± 69.87 | ops/s |
| simpleclientInc | 6.53K | ± 54.36 | ops/s |
| simpleclientNoLabelsInc | 6.34K | ± 8.57 | ops/s |
| simpleclientAdd | 6.17K | ± 329.96 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.29K | ± 31.04 | ops/s |
| prometheusClassic | 5.49K | ± 1.54K | ops/s |
| prometheusClassicSingleThread | 4.58K | ± 33.25 | ops/s |
| simpleclient | 4.36K | ± 43.83 | ops/s |
| prometheusNative | 2.81K | ± 327.53 | ops/s |
| openTelemetryClassic | 824.48 | ± 16.78 | ops/s |
| openTelemetryExponential | 769.76 | ± 62.49 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 24.33K | ± 389.97 | ops/s |
| prometheusWriteToNull | 23.70K | ± 429.37 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 495.69K | ± 2.46K | ops/s |
| prometheusWriteToByteArray | 489.93K | ± 1.98K | ops/s |
| openMetricsWriteToNull | 480.20K | ± 6.78K | ops/s |
| openMetricsWriteToByteArray | 471.51K | ± 5.06K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43745.950   ± 7741.782  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12956.377     ± 69.875  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14915.548    ± 276.940  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18452.126    ± 161.524  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51317.075    ± 371.086  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66220.509    ± 367.542  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56062.045   ± 1214.430  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6172.565    ± 329.959  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6534.593     ± 54.360  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6341.971      ± 8.571  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        824.479     ± 16.782  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        769.763     ± 62.492  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5491.150   ± 1542.830  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12288.036     ± 31.038  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4584.905     ± 33.247  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2807.302    ± 327.529  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4362.044     ± 43.830  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24325.701    ± 389.972  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23697.960    ± 429.373  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     471505.387   ± 5059.116  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     480197.815   ± 6779.589  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489928.810   ± 1979.192  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     495689.343   ± 2458.662  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-22T03:54:05Z
- **Commit:** [`ac0d68a`](https://github.com/prometheus/client_java/commit/ac0d68a62886473ac4afd736602760e97024b528)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 77.57K | ± 1.38K | ops/s |
| prometheusNoLabelsInc | 66.34K | ± 499.27 | ops/s |
| prometheusAdd | 61.88K | ± 396.75 | ops/s |
| codahaleIncNoLabels | 55.76K | ± 1.64K | ops/s |
| openTelemetryIncNoLabels | 21.36K | ± 1.38K | ops/s |
| openTelemetryInc | 18.07K | ± 205.35 | ops/s |
| openTelemetryAdd | 15.36K | ± 646.43 | ops/s |
| simpleclientAdd | 7.88K | ± 49.71 | ops/s |
| simpleclientInc | 7.81K | ± 72.22 | ops/s |
| simpleclientNoLabelsInc | 7.61K | ± 10.17 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.94K | ± 173.35 | ops/s |
| prometheusClassic | 8.31K | ± 1.88K | ops/s |
| prometheusClassicSingleThread | 7.47K | ± 24.83 | ops/s |
| simpleclient | 5.88K | ± 164.31 | ops/s |
| prometheusNative | 3.76K | ± 367.19 | ops/s |
| openTelemetryClassic | 1.05K | ± 69.10 | ops/s |
| openTelemetryExponential | 986.12 | ± 28.16 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 35.54K | ± 86.75 | ops/s |
| openMetricsWriteToNull | 35.46K | ± 297.34 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 707.54K | ± 6.22K | ops/s |
| prometheusWriteToByteArray | 694.37K | ± 3.02K | ops/s |
| openMetricsWriteToNull | 666.85K | ± 8.20K | ops/s |
| openMetricsWriteToByteArray | 651.99K | ± 5.62K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      55762.383   ± 1635.665  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15364.027    ± 646.428  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18069.932    ± 205.354  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21358.752   ± 1380.794  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      61876.253    ± 396.751  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      77574.905   ± 1379.491  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66343.542    ± 499.274  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7881.678     ± 49.708  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7806.791     ± 72.219  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7606.441     ± 10.174  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       1051.961     ± 69.099  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        986.124     ± 28.157  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8308.957   ± 1880.137  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17941.738    ± 173.353  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7474.007     ± 24.829  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3761.634    ± 367.189  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5879.358    ± 164.314  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35458.320    ± 297.337  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35542.666     ± 86.748  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     651985.696   ± 5620.807  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     666849.251   ± 8201.523  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     694370.932   ± 3021.877  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     707543.929   ± 6224.217  ops/s
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

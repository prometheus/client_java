# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-28T06:18:39Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 59.02K | ± 73.04 | ops/s |
| prometheusNoLabelsInc | 51.23K | ± 551.20 | ops/s |
| prometheusAdd | 48.37K | ± 131.23 | ops/s |
| codahaleIncNoLabels | 44.12K | ± 313.60 | ops/s |
| openTelemetryIncNoLabels | 17.09K | ± 240.72 | ops/s |
| openTelemetryInc | 13.81K | ± 125.17 | ops/s |
| openTelemetryAdd | 12.14K | ± 142.64 | ops/s |
| simpleclientInc | 6.16K | ± 62.87 | ops/s |
| simpleclientNoLabelsInc | 5.91K | ± 28.14 | ops/s |
| simpleclientAdd | 5.84K | ± 302.39 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.94K | ± 63.41 | ops/s |
| prometheusClassicSingleThread | 5.81K | ± 24.10 | ops/s |
| simpleclient | 4.54K | ± 75.98 | ops/s |
| prometheusClassic | 4.43K | ± 404.66 | ops/s |
| prometheusNative | 3.19K | ± 143.17 | ops/s |
| openTelemetryClassic | 870.26 | ± 60.20 | ops/s |
| openTelemetryExponential | 657.05 | ± 26.00 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.27K | ± 584.42 | ops/s |
| openMetricsWriteToNull | 27.25K | ± 240.74 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 555.26K | ± 3.36K | ops/s |
| prometheusWriteToByteArray | 550.21K | ± 6.88K | ops/s |
| openMetricsWriteToNull | 529.57K | ± 9.72K | ops/s |
| openMetricsWriteToByteArray | 518.81K | ± 2.92K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44121.177    ± 313.595  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12142.022    ± 142.638  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13809.253    ± 125.174  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17092.475    ± 240.720  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48367.113    ± 131.233  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59018.369     ± 73.042  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51231.656    ± 551.199  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5841.874    ± 302.389  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6164.280     ± 62.872  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5906.376     ± 28.140  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        870.262     ± 60.195  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        657.045     ± 25.996  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4433.931    ± 404.657  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13940.441     ± 63.406  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5806.158     ± 24.103  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3188.396    ± 143.175  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4536.681     ± 75.978  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27246.138    ± 240.737  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27267.166    ± 584.424  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     518810.154   ± 2919.823  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     529572.414   ± 9724.042  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     550213.842   ± 6882.663  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     555262.333   ± 3361.985  ops/s
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

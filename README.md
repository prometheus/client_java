# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-02T04:07:05Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| codahaleIncNoLabels | 27.34K | ± 324.45 | ops/s |
| prometheusInc | 27.13K | ± 822.25 | ops/s |
| prometheusNoLabelsInc | 26.92K | ± 606.84 | ops/s |
| prometheusAdd | 25.85K | ± 72.30 | ops/s |
| openTelemetryIncNoLabels | 17.15K | ± 58.63 | ops/s |
| openTelemetryInc | 15.27K | ± 151.13 | ops/s |
| openTelemetryAdd | 13.13K | ± 114.21 | ops/s |
| simpleclientInc | 6.69K | ± 121.09 | ops/s |
| simpleclientNoLabelsInc | 6.64K | ± 5.45 | ops/s |
| simpleclientAdd | 6.60K | ± 104.36 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 6.83K | ± 46.90 | ops/s |
| simpleclient | 4.41K | ± 72.81 | ops/s |
| prometheusClassicSingleThread | 2.91K | ± 76.39 | ops/s |
| prometheusClassic | 2.62K | ± 667.67 | ops/s |
| prometheusNative | 2.02K | ± 265.96 | ops/s |
| openTelemetryExponential | 481.30 | ± 12.40 | ops/s |
| openTelemetryClassic | 452.20 | ± 51.67 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 17.95K | ± 19.86 | ops/s |
| prometheusWriteToNull | 17.91K | ± 24.02 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 304.07K | ± 1.04K | ops/s |
| prometheusWriteToByteArray | 302.45K | ± 1.66K | ops/s |
| openMetricsWriteToNull | 285.49K | ± 1.47K | ops/s |
| openMetricsWriteToByteArray | 284.64K | ± 1.25K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      27341.940    ± 324.455  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      13132.208    ± 114.207  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15268.483    ± 151.133  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17149.492     ± 58.625  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      25845.477     ± 72.299  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      27130.496    ± 822.250  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      26920.616    ± 606.838  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6597.091    ± 104.364  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6690.709    ± 121.092  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6642.774      ± 5.454  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        452.202     ± 51.670  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        481.302     ± 12.398  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2622.319    ± 667.670  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6834.429     ± 46.901  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2913.511     ± 76.389  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2022.868    ± 265.958  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4413.635     ± 72.809  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      17945.920     ± 19.863  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17905.714     ± 24.017  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     284635.365   ± 1251.351  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     285491.311   ± 1465.225  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     302450.246   ± 1664.840  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     304072.195   ± 1039.010  ops/s
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

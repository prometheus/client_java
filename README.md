# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-01T03:53:52Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 65.23K | ± 1.87K | ops/s |
| prometheusNoLabelsInc | 56.83K | ± 404.46 | ops/s |
| prometheusAdd | 51.19K | ± 367.30 | ops/s |
| codahaleIncNoLabels | 49.41K | ± 697.60 | ops/s |
| openTelemetryIncNoLabels | 18.32K | ± 294.34 | ops/s |
| openTelemetryInc | 14.86K | ± 497.56 | ops/s |
| openTelemetryAdd | 12.84K | ± 218.06 | ops/s |
| simpleclientInc | 6.56K | ± 53.09 | ops/s |
| simpleclientNoLabelsInc | 6.51K | ± 151.50 | ops/s |
| simpleclientAdd | 6.06K | ± 303.77 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.30K | ± 28.46 | ops/s |
| prometheusClassicSingleThread | 4.48K | ± 153.15 | ops/s |
| simpleclient | 4.43K | ± 57.78 | ops/s |
| prometheusClassic | 4.20K | ± 74.74 | ops/s |
| prometheusNative | 2.87K | ± 327.63 | ops/s |
| openTelemetryClassic | 908.50 | ± 115.42 | ops/s |
| openTelemetryExponential | 899.49 | ± 130.29 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.46K | ± 848.35 | ops/s |
| openMetricsWriteToNull | 22.41K | ± 255.64 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 520.49K | ± 2.91K | ops/s |
| prometheusWriteToByteArray | 507.87K | ± 11.62K | ops/s |
| openMetricsWriteToNull | 493.03K | ± 2.13K | ops/s |
| openMetricsWriteToByteArray | 487.71K | ± 5.49K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49405.998    ± 697.598  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12837.171    ± 218.056  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14856.751    ± 497.563  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18322.761    ± 294.338  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51192.857    ± 367.296  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65228.930   ± 1868.766  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56829.394    ± 404.457  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6064.679    ± 303.766  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6556.448     ± 53.092  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6508.989    ± 151.502  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        908.505    ± 115.419  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        899.489    ± 130.286  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4196.077     ± 74.738  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12300.874     ± 28.463  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4477.392    ± 153.147  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2871.721    ± 327.634  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4428.122     ± 57.777  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      22406.420    ± 255.639  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23464.983    ± 848.348  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     487709.475   ± 5487.814  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     493029.897   ± 2129.086  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     507871.664  ± 11615.239  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     520490.509   ± 2906.725  ops/s
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

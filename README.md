# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-03T04:31:22Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.30K | ± 865.89 | ops/s |
| prometheusNoLabelsInc | 56.89K | ± 220.44 | ops/s |
| prometheusAdd | 51.19K | ± 431.04 | ops/s |
| codahaleIncNoLabels | 43.87K | ± 7.56K | ops/s |
| openTelemetryIncNoLabels | 18.42K | ± 126.65 | ops/s |
| openTelemetryInc | 15.03K | ± 380.48 | ops/s |
| openTelemetryAdd | 12.98K | ± 20.05 | ops/s |
| simpleclientInc | 6.53K | ± 37.75 | ops/s |
| simpleclientAdd | 6.43K | ± 50.03 | ops/s |
| simpleclientNoLabelsInc | 6.36K | ± 14.59 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.16K | ± 231.32 | ops/s |
| prometheusClassic | 6.76K | ± 1.74K | ops/s |
| prometheusClassicSingleThread | 4.57K | ± 33.67 | ops/s |
| simpleclient | 4.37K | ± 39.59 | ops/s |
| prometheusNative | 2.79K | ± 269.32 | ops/s |
| openTelemetryClassic | 788.66 | ± 25.49 | ops/s |
| openTelemetryExponential | 701.05 | ± 93.43 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 23.68K | ± 365.30 | ops/s |
| prometheusWriteToNull | 23.18K | ± 733.85 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 499.26K | ± 4.04K | ops/s |
| prometheusWriteToByteArray | 487.78K | ± 4.87K | ops/s |
| openMetricsWriteToNull | 473.58K | ± 7.86K | ops/s |
| openMetricsWriteToByteArray | 471.73K | ± 3.76K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43871.590   ± 7564.573  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12976.916     ± 20.053  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15033.737    ± 380.479  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18424.722    ± 126.654  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51190.697    ± 431.042  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66303.588    ± 865.892  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56887.980    ± 220.438  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6427.335     ± 50.029  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6533.009     ± 37.750  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6360.032     ± 14.588  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        788.660     ± 25.486  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        701.046     ± 93.433  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6756.498   ± 1741.865  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12157.114    ± 231.316  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4566.605     ± 33.666  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2786.145    ± 269.321  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4369.999     ± 39.587  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23676.101    ± 365.305  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23182.002    ± 733.855  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     471733.078   ± 3757.545  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     473575.036   ± 7863.008  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     487777.162   ± 4865.589  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     499256.134   ± 4036.567  ops/s
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

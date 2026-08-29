# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-29T03:49:23Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 64.18K | ± 1.25K | ops/s |
| prometheusNoLabelsInc | 57.11K | ± 71.76 | ops/s |
| prometheusAdd | 51.25K | ± 549.37 | ops/s |
| codahaleIncNoLabels | 47.44K | ± 224.35 | ops/s |
| openTelemetryIncNoLabels | 18.65K | ± 215.32 | ops/s |
| openTelemetryInc | 14.78K | ± 181.41 | ops/s |
| openTelemetryAdd | 12.84K | ± 160.66 | ops/s |
| simpleclientInc | 6.59K | ± 8.55 | ops/s |
| simpleclientNoLabelsInc | 6.45K | ± 135.67 | ops/s |
| simpleclientAdd | 6.36K | ± 202.45 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.31K | ± 23.92 | ops/s |
| prometheusClassic | 7.28K | ± 2.67K | ops/s |
| prometheusClassicSingleThread | 4.57K | ± 40.65 | ops/s |
| simpleclient | 4.44K | ± 46.56 | ops/s |
| prometheusNative | 3.00K | ± 362.50 | ops/s |
| openTelemetryClassic | 816.39 | ± 64.19 | ops/s |
| openTelemetryExponential | 739.39 | ± 140.27 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 23.36K | ± 246.39 | ops/s |
| prometheusWriteToNull | 23.13K | ± 608.93 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 523.95K | ± 3.91K | ops/s |
| prometheusWriteToByteArray | 504.86K | ± 9.35K | ops/s |
| openMetricsWriteToNull | 493.28K | ± 3.87K | ops/s |
| openMetricsWriteToByteArray | 490.76K | ± 3.48K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47442.778    ± 224.355  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12840.460    ± 160.655  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14776.538    ± 181.412  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18653.494    ± 215.320  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51248.536    ± 549.371  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64178.584   ± 1245.047  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57111.090     ± 71.759  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6360.773    ± 202.450  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6591.604      ± 8.555  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6454.524    ± 135.667  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        816.390     ± 64.194  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        739.394    ± 140.266  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7278.728   ± 2671.603  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12311.793     ± 23.918  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4566.227     ± 40.653  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2998.503    ± 362.502  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4443.662     ± 46.555  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23358.000    ± 246.388  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23129.920    ± 608.932  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     490758.835   ± 3477.857  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     493284.573   ± 3872.068  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     504857.927   ± 9348.152  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     523949.948   ± 3914.225  ops/s
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

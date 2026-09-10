# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-10T03:52:38Z
- **Commit:** [`39a91dd`](https://github.com/prometheus/client_java/commit/39a91ddb316ebbeebb8740a436109f9b9cca7e17)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 65.90K | ± 341.99 | ops/s |
| prometheusNoLabelsInc | 57.08K | ± 553.40 | ops/s |
| prometheusAdd | 50.96K | ± 632.77 | ops/s |
| codahaleIncNoLabels | 48.47K | ± 2.11K | ops/s |
| openTelemetryIncNoLabels | 18.59K | ± 72.56 | ops/s |
| openTelemetryInc | 15.16K | ± 370.38 | ops/s |
| openTelemetryAdd | 12.70K | ± 346.81 | ops/s |
| simpleclientInc | 6.52K | ± 64.11 | ops/s |
| simpleclientNoLabelsInc | 6.32K | ± 95.55 | ops/s |
| simpleclientAdd | 6.23K | ± 335.32 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.19K | ± 151.06 | ops/s |
| prometheusClassic | 6.12K | ± 1.09K | ops/s |
| prometheusClassicSingleThread | 4.56K | ± 34.53 | ops/s |
| simpleclient | 4.37K | ± 50.72 | ops/s |
| prometheusNative | 3.23K | ± 24.94 | ops/s |
| openTelemetryClassic | 894.64 | ± 76.62 | ops/s |
| openTelemetryExponential | 809.27 | ± 125.69 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 23.58K | ± 264.62 | ops/s |
| prometheusWriteToNull | 22.77K | ± 1.25K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 505.92K | ± 3.75K | ops/s |
| prometheusWriteToNull | 498.37K | ± 8.68K | ops/s |
| openMetricsWriteToNull | 491.65K | ± 1.31K | ops/s |
| openMetricsWriteToByteArray | 478.02K | ± 4.91K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48474.436   ± 2109.839  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12697.570    ± 346.811  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15155.143    ± 370.384  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18590.767     ± 72.559  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50955.881    ± 632.766  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65897.250    ± 341.986  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57084.859    ± 553.401  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6231.625    ± 335.317  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6521.927     ± 64.105  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6319.911     ± 95.553  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        894.637     ± 76.617  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        809.271    ± 125.695  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6115.620   ± 1089.831  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12186.370    ± 151.057  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4563.544     ± 34.531  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3228.848     ± 24.938  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4371.628     ± 50.717  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23581.702    ± 264.615  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22773.657   ± 1250.074  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478020.066   ± 4914.160  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     491645.912   ± 1309.302  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     505924.899   ± 3750.602  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     498365.612   ± 8684.382  ops/s
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

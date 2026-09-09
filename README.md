# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-09T03:51:50Z
- **Commit:** [`39a91dd`](https://github.com/prometheus/client_java/commit/39a91ddb316ebbeebb8740a436109f9b9cca7e17)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) 6973P-C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusAdd | 35.75K | ± 831.87 | ops/s |
| codahaleIncNoLabels | 35.51K | ± 1.16K | ops/s |
| prometheusInc | 34.71K | ± 1.14K | ops/s |
| prometheusNoLabelsInc | 34.18K | ± 861.42 | ops/s |
| openTelemetryIncNoLabels | 24.88K | ± 807.15 | ops/s |
| openTelemetryInc | 22.40K | ± 351.15 | ops/s |
| openTelemetryAdd | 19.50K | ± 273.87 | ops/s |
| simpleclientInc | 9.08K | ± 162.75 | ops/s |
| simpleclientNoLabelsInc | 8.89K | ± 197.66 | ops/s |
| simpleclientAdd | 8.87K | ± 162.83 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 9.21K | ± 159.38 | ops/s |
| simpleclient | 6.03K | ± 127.01 | ops/s |
| prometheusClassicSingleThread | 4.56K | ± 63.63 | ops/s |
| prometheusClassic | 3.01K | ± 1.73K | ops/s |
| prometheusNative | 2.37K | ± 99.77 | ops/s |
| openTelemetryClassic | 571.49 | ± 14.85 | ops/s |
| openTelemetryExponential | 482.63 | ± 15.42 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 24.93K | ± 376.04 | ops/s |
| openMetricsWriteToNull | 24.67K | ± 740.40 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 346.25K | ± 7.62K | ops/s |
| prometheusWriteToByteArray | 345.34K | ± 3.42K | ops/s |
| openMetricsWriteToByteArray | 329.34K | ± 3.99K | ops/s |
| openMetricsWriteToNull | 325.45K | ± 4.15K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      35509.716   ± 1161.535  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19498.542    ± 273.869  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22404.602    ± 351.154  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      24875.624    ± 807.150  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      35748.727    ± 831.872  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      34714.101   ± 1141.768  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      34178.854    ± 861.420  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       8868.747    ± 162.832  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       9079.310    ± 162.754  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       8887.050    ± 197.664  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        571.492     ± 14.852  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        482.630     ± 15.421  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3011.795   ± 1728.568  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       9213.740    ± 159.376  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4560.495     ± 63.632  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2374.024     ± 99.771  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       6033.996    ± 127.007  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24665.803    ± 740.405  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24930.332    ± 376.035  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     329338.829   ± 3990.435  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     325449.441   ± 4145.821  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     345337.898   ± 3419.200  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     346247.643   ± 7618.622  ops/s
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

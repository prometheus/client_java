# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-21T04:19:29Z
- **Commit:** [`5ce2b57`](https://github.com/prometheus/client_java/commit/5ce2b575272a06b5115f40f3298d5c861cef8bbd)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 67.83K | ± 1.36K | ops/s | **fastest** |
| prometheusNoLabelsInc | 58.45K | ± 565.21 | ops/s | 1.2x slower |
| prometheusAdd | 53.40K | ± 325.81 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.04K | ± 1.56K | ops/s | 1.4x slower |
| simpleclientInc | 6.74K | ± 150.47 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.57K | ± 212.76 | ops/s | 10x slower |
| simpleclientAdd | 6.41K | ± 309.76 | ops/s | 11x slower |
| openTelemetryInc | 1.40K | ± 129.03 | ops/s | 48x slower |
| openTelemetryAdd | 1.33K | ± 52.99 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.31K | ± 110.64 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.74K | ± 1.17K | ops/s | **fastest** |
| simpleclient | 4.53K | ± 30.82 | ops/s | 1.5x slower |
| prometheusNative | 2.77K | ± 310.25 | ops/s | 2.4x slower |
| openTelemetryClassic | 704.91 | ± 23.61 | ops/s | 9.6x slower |
| openTelemetryExponential | 571.37 | ± 29.59 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 491.89K | ± 5.90K | ops/s | **fastest** |
| prometheusWriteToNull | 491.83K | ± 5.33K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 483.60K | ± 5.53K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 477.64K | ± 10.07K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50035.832   ± 1555.786  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1328.469     ± 52.988  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1404.339    ± 129.029  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1306.199    ± 110.637  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      53401.138    ± 325.805  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67834.696   ± 1357.568  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      58449.841    ± 565.213  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6407.669    ± 309.760  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6739.121    ± 150.469  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6574.797    ± 212.764  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        704.909     ± 23.614  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        571.372     ± 29.595  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6743.459   ± 1165.135  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2774.780    ± 310.250  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4530.331     ± 30.819  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477635.757  ± 10069.593  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     483601.359   ± 5532.806  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491887.947   ± 5899.163  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491832.452   ± 5328.571  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-08T04:28:31Z
- **Commit:** [`5b65034`](https://github.com/prometheus/client_java/commit/5b65034f0a095c3f9ed2294cfe98c7699fbfe0d2)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.19K | ± 10.16K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.68K | ± 91.80 | ops/s | 1.1x slower |
| prometheusAdd | 51.38K | ± 478.77 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.69K | ± 910.24 | ops/s | 1.2x slower |
| simpleclientInc | 6.66K | ± 172.67 | ops/s | 9.0x slower |
| simpleclientNoLabelsInc | 6.59K | ± 184.11 | ops/s | 9.1x slower |
| simpleclientAdd | 6.16K | ± 76.84 | ops/s | 9.8x slower |
| openTelemetryInc | 1.48K | ± 117.54 | ops/s | 41x slower |
| openTelemetryAdd | 1.39K | ± 190.01 | ops/s | 43x slower |
| openTelemetryIncNoLabels | 1.37K | ± 173.41 | ops/s | 44x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.41K | ± 1.77K | ops/s | **fastest** |
| simpleclient | 4.54K | ± 79.68 | ops/s | 1.2x slower |
| prometheusNative | 2.99K | ± 220.49 | ops/s | 1.8x slower |
| openTelemetryClassic | 674.25 | ± 2.81 | ops/s | 8.0x slower |
| openTelemetryExponential | 542.22 | ± 2.97 | ops/s | 10.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 497.08K | ± 996.27 | ops/s | **fastest** |
| prometheusWriteToByteArray | 495.33K | ± 2.93K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 492.53K | ± 1.65K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 484.48K | ± 6.38K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49691.831    ± 910.241  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1386.658    ± 190.009  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1483.411    ± 117.544  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1366.126    ± 173.412  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51378.339    ± 478.770  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60190.372  ± 10160.474  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56676.573     ± 91.799  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6160.046     ± 76.835  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6655.565    ± 172.674  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6586.759    ± 184.106  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        674.253      ± 2.813  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        542.216      ± 2.975  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5407.425   ± 1770.457  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2993.311    ± 220.494  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4538.573     ± 79.678  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     484479.821   ± 6380.265  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     492531.483   ± 1646.133  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     495326.143   ± 2931.970  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     497080.947    ± 996.271  ops/s
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

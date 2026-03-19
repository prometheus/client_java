# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-19T04:29:07Z
- **Commit:** [`37c7806`](https://github.com/prometheus/client_java/commit/37c7806004081709080b60d173cf30d04dc3e622)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.51K | ± 102.91 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.11K | ± 1.20K | ops/s | 1.2x slower |
| prometheusAdd | 51.03K | ± 612.13 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.17K | ± 1.61K | ops/s | 1.4x slower |
| simpleclientInc | 6.79K | ± 19.07 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.64K | ± 72.45 | ops/s | 10x slower |
| simpleclientAdd | 6.29K | ± 205.55 | ops/s | 11x slower |
| openTelemetryAdd | 1.42K | ± 192.17 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.25K | ± 13.69 | ops/s | 53x slower |
| openTelemetryInc | 1.23K | ± 31.44 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.50K | ± 14.63 | ops/s | **fastest** |
| prometheusClassic | 4.21K | ± 456.63 | ops/s | 1.1x slower |
| prometheusNative | 2.83K | ± 318.46 | ops/s | 1.6x slower |
| openTelemetryClassic | 707.46 | ± 35.21 | ops/s | 6.4x slower |
| openTelemetryExponential | 586.86 | ± 23.53 | ops/s | 7.7x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 488.63K | ± 5.32K | ops/s | **fastest** |
| openMetricsWriteToNull | 483.79K | ± 3.77K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 482.17K | ± 4.12K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.66K | ± 8.74K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49169.458   ± 1614.107  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1420.370    ± 192.166  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1231.590     ± 31.435  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1246.152     ± 13.695  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51025.420    ± 612.129  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66513.307    ± 102.912  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56106.976   ± 1203.659  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6290.316    ± 205.548  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6790.251     ± 19.070  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6643.550     ± 72.450  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        707.463     ± 35.208  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        586.856     ± 23.531  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4214.929    ± 456.631  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2830.128    ± 318.464  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4501.983     ± 14.627  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473663.748   ± 8742.066  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     483787.060   ± 3767.435  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     482174.024   ± 4116.094  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     488628.681   ± 5324.167  ops/s
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

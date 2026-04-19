# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-19T04:31:28Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.78K | ± 1.22K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.53K | ± 1.87K | ops/s | 1.2x slower |
| prometheusAdd | 48.79K | ± 890.56 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.90K | ± 391.44 | ops/s | 1.4x slower |
| simpleclientInc | 6.24K | ± 133.96 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.13K | ± 260.92 | ops/s | 9.8x slower |
| simpleclientAdd | 5.88K | ± 213.66 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.46K | ± 144.93 | ops/s | 41x slower |
| openTelemetryAdd | 1.41K | ± 91.59 | ops/s | 42x slower |
| openTelemetryInc | 1.40K | ± 61.80 | ops/s | 43x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.77K | ± 1.26K | ops/s | **fastest** |
| simpleclient | 4.54K | ± 42.78 | ops/s | 1.3x slower |
| prometheusNative | 2.77K | ± 185.60 | ops/s | 2.1x slower |
| openTelemetryClassic | 609.99 | ± 30.25 | ops/s | 9.5x slower |
| openTelemetryExponential | 504.51 | ± 6.74 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 553.24K | ± 4.23K | ops/s | **fastest** |
| prometheusWriteToByteArray | 543.92K | ± 4.26K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 536.25K | ± 5.47K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 524.71K | ± 10.55K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43899.035    ± 391.435  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1407.040     ± 91.590  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1395.208     ± 61.799  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1456.006    ± 144.930  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48785.598    ± 890.561  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59781.585   ± 1218.635  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51526.129   ± 1866.869  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5879.240    ± 213.658  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6236.561    ± 133.956  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6130.672    ± 260.923  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        609.995     ± 30.248  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        504.505      ± 6.737  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5771.833   ± 1259.951  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2772.945    ± 185.604  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4543.842     ± 42.779  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     524713.523  ± 10550.156  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     536250.723   ± 5470.088  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     543924.553   ± 4257.181  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     553240.717   ± 4230.651  ops/s
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

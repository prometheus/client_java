# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-14T04:21:30Z
- **Commit:** [`b81332e`](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.46K | ± 538.33 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.04K | ± 499.91 | ops/s | 1.2x slower |
| prometheusAdd | 49.18K | ± 3.78K | ops/s | 1.4x slower |
| codahaleIncNoLabels | 48.85K | ± 1.19K | ops/s | 1.4x slower |
| simpleclientInc | 6.76K | ± 33.25 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.49K | ± 168.68 | ops/s | 10x slower |
| simpleclientAdd | 6.44K | ± 170.20 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.42K | ± 180.11 | ops/s | 47x slower |
| openTelemetryInc | 1.33K | ± 102.84 | ops/s | 50x slower |
| openTelemetryAdd | 1.27K | ± 7.18 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.96K | ± 686.66 | ops/s | **fastest** |
| simpleclient | 4.53K | ± 46.12 | ops/s | 1.1x slower |
| prometheusNative | 3.07K | ± 244.80 | ops/s | 1.6x slower |
| openTelemetryClassic | 640.83 | ± 14.84 | ops/s | 7.7x slower |
| openTelemetryExponential | 555.92 | ± 31.87 | ops/s | 8.9x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.63K | ± 3.50K | ops/s | **fastest** |
| openMetricsWriteToNull | 485.99K | ± 3.41K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 481.42K | ± 8.38K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.86K | ± 5.66K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48847.682   ± 1185.296  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1266.135      ± 7.179  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1327.916    ± 102.842  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1416.499    ± 180.106  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      49182.127   ± 3783.881  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66455.994    ± 538.325  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57035.879    ± 499.912  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6441.835    ± 170.201  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6760.211     ± 33.246  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6492.186    ± 168.676  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        640.834     ± 14.841  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        555.923     ± 31.873  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4960.676    ± 686.659  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3070.954    ± 244.804  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4529.803     ± 46.118  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473861.259   ± 5659.609  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485989.512   ± 3408.268  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     481420.722   ± 8380.749  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491632.243   ± 3501.207  ops/s
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

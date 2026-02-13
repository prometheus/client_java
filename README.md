# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-13T04:29:16Z
- **Commit:** [`7e472c7`](https://github.com/prometheus/client_java/commit/7e472c7c9c9b4b49339cc95162b67c1b10cb3ba7)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 62.00K | ± 2.47K | ops/s | **fastest** |
| prometheusNoLabelsInc | 54.95K | ± 3.18K | ops/s | 1.1x slower |
| prometheusAdd | 51.47K | ± 292.29 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.24K | ± 1.29K | ops/s | 1.3x slower |
| simpleclientInc | 6.78K | ± 32.76 | ops/s | 9.1x slower |
| simpleclientNoLabelsInc | 6.61K | ± 128.36 | ops/s | 9.4x slower |
| simpleclientAdd | 6.32K | ± 191.46 | ops/s | 9.8x slower |
| openTelemetryAdd | 1.45K | ± 171.68 | ops/s | 43x slower |
| openTelemetryInc | 1.38K | ± 128.07 | ops/s | 45x slower |
| openTelemetryIncNoLabels | 1.24K | ± 47.52 | ops/s | 50x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.62K | ± 1.44K | ops/s | **fastest** |
| simpleclient | 4.48K | ± 119.55 | ops/s | 1.3x slower |
| prometheusNative | 2.57K | ± 115.73 | ops/s | 2.2x slower |
| openTelemetryClassic | 664.52 | ± 9.80 | ops/s | 8.5x slower |
| openTelemetryExponential | 543.39 | ± 30.31 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 500.75K | ± 5.93K | ops/s | **fastest** |
| prometheusWriteToByteArray | 495.14K | ± 2.51K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 494.90K | ± 2.78K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.04K | ± 6.59K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49243.207   ± 1289.040  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1446.273    ± 171.682  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1375.221    ± 128.073  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1241.462     ± 47.518  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51473.959    ± 292.294  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62003.956   ± 2471.925  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54946.840   ± 3182.696  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6315.032    ± 191.461  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6778.598     ± 32.760  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6607.254    ± 128.358  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        664.516      ± 9.801  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        543.387     ± 30.313  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5615.958   ± 1439.207  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2572.324    ± 115.729  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4479.113    ± 119.548  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483043.254   ± 6587.935  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     494898.445   ± 2777.958  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     495144.654   ± 2506.941  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     500746.356   ± 5928.813  ops/s
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

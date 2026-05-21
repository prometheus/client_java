# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-21T04:38:47Z
- **Commit:** [`320538a`](https://github.com/prometheus/client_java/commit/320538a09efad128c6d80bcc3d6eecca394603db)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.73K | ± 361.02 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.44K | ± 983.47 | ops/s | 1.2x slower |
| prometheusAdd | 48.75K | ± 608.91 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.70K | ± 304.25 | ops/s | 1.4x slower |
| simpleclientInc | 6.09K | ± 17.91 | ops/s | 9.8x slower |
| simpleclientAdd | 5.92K | ± 243.02 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 5.88K | ± 20.25 | ops/s | 10x slower |
| openTelemetryInc | 5.52K | ± 945.88 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 4.28K | ± 372.20 | ops/s | 14x slower |
| openTelemetryAdd | 4.08K | ± 960.19 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.83K | ± 1.39K | ops/s | **fastest** |
| simpleclient | 4.24K | ± 165.63 | ops/s | 1.8x slower |
| prometheusNative | 3.00K | ± 265.03 | ops/s | 2.6x slower |
| openTelemetryClassic | 694.36 | ± 23.25 | ops/s | 11x slower |
| openTelemetryExponential | 546.97 | ± 22.89 | ops/s | 14x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.35K | ± 362.49 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.24K | ± 411.12 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 578.34K | ± 6.45K | ops/s | **fastest** |
| prometheusWriteToByteArray | 560.41K | ± 18.65K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 546.89K | ± 6.21K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 534.37K | ± 1.87K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43704.847    ± 304.247  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4081.489    ± 960.185  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5522.323    ± 945.879  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4277.273    ± 372.204  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48749.095    ± 608.910  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59734.294    ± 361.024  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51443.662    ± 983.472  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5919.973    ± 243.020  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6086.606     ± 17.906  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5879.054     ± 20.254  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        694.364     ± 23.245  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        546.974     ± 22.888  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7831.213   ± 1386.657  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2995.589    ± 265.026  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4236.023    ± 165.634  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27235.707    ± 411.118  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27345.212    ± 362.493  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     534374.734   ± 1865.418  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     546892.888   ± 6214.629  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     560408.739  ± 18650.066  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     578338.766   ± 6445.027  ops/s
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

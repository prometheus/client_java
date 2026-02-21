# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-21T04:19:17Z
- **Commit:** [`a483539`](https://github.com/prometheus/client_java/commit/a4835397c5fe237a534bd9c3259827d7e3e38d31)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.40K | ± 1.24K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.03K | ± 470.91 | ops/s | 1.1x slower |
| prometheusAdd | 51.53K | ± 280.38 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.53K | ± 902.33 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.71K | ± 10.02 | ops/s | 9.6x slower |
| simpleclientInc | 6.66K | ± 154.28 | ops/s | 9.7x slower |
| simpleclientAdd | 6.21K | ± 284.87 | ops/s | 10x slower |
| openTelemetryAdd | 1.41K | ± 202.78 | ops/s | 46x slower |
| openTelemetryInc | 1.22K | ± 34.06 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.22K | ± 5.85 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.08K | ± 1.62K | ops/s | **fastest** |
| simpleclient | 4.54K | ± 36.49 | ops/s | 1.1x slower |
| prometheusNative | 2.82K | ± 183.67 | ops/s | 1.8x slower |
| openTelemetryClassic | 670.27 | ± 27.24 | ops/s | 7.6x slower |
| openTelemetryExponential | 533.08 | ± 15.44 | ops/s | 9.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 496.82K | ± 1.58K | ops/s | **fastest** |
| prometheusWriteToByteArray | 492.47K | ± 2.88K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 489.51K | ± 4.42K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 480.03K | ± 4.50K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49529.730    ± 902.326  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1408.797    ± 202.781  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1224.675     ± 34.064  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1221.178      ± 5.852  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51528.917    ± 280.383  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64400.465   ± 1241.378  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57026.562    ± 470.909  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6210.832    ± 284.868  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6655.721    ± 154.276  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6710.963     ± 10.021  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        670.265     ± 27.239  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        533.076     ± 15.445  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5082.567   ± 1620.020  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2821.437    ± 183.667  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4544.047     ± 36.491  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     480033.605   ± 4501.626  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489512.491   ± 4423.750  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     492470.773   ± 2880.512  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     496815.448   ± 1578.328  ops/s
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

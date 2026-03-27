# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-27T04:30:53Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.90K | ± 1.89K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.02K | ± 382.43 | ops/s | 1.2x slower |
| prometheusAdd | 51.21K | ± 495.36 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.81K | ± 1.26K | ops/s | 1.4x slower |
| simpleclientInc | 6.62K | ± 140.30 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.52K | ± 140.19 | ops/s | 10x slower |
| simpleclientAdd | 6.17K | ± 33.83 | ops/s | 11x slower |
| openTelemetryInc | 1.39K | ± 184.04 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.35K | ± 181.07 | ops/s | 49x slower |
| openTelemetryAdd | 1.28K | ± 42.36 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.42K | ± 1.78K | ops/s | **fastest** |
| simpleclient | 4.54K | ± 20.86 | ops/s | 1.4x slower |
| prometheusNative | 2.95K | ± 334.99 | ops/s | 2.2x slower |
| openTelemetryClassic | 713.51 | ± 32.07 | ops/s | 9.0x slower |
| openTelemetryExponential | 559.19 | ± 2.83 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.82K | ± 4.44K | ops/s | **fastest** |
| prometheusWriteToByteArray | 489.82K | ± 4.73K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 488.52K | ± 1.60K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 481.29K | ± 4.80K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48805.931   ± 1260.617  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1277.999     ± 42.361  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1389.859    ± 184.042  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1349.862    ± 181.069  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51211.953    ± 495.363  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65899.926   ± 1892.614  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57023.502    ± 382.431  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6172.303     ± 33.830  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6620.107    ± 140.303  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6522.305    ± 140.192  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        713.515     ± 32.071  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        559.193      ± 2.835  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6422.628   ± 1777.882  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2951.420    ± 334.986  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4544.122     ± 20.862  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481289.714   ± 4802.110  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     488520.464   ± 1599.524  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489824.617   ± 4733.368  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492820.983   ± 4435.608  ops/s
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

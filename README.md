# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-25T04:40:26Z
- **Commit:** [`5ee188f`](https://github.com/prometheus/client_java/commit/5ee188ff288806f76e53a89d32431a93bb53da11)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.19K | ± 706.10 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.41K | ± 762.75 | ops/s | 1.2x slower |
| prometheusAdd | 51.43K | ± 124.69 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.41K | ± 2.08K | ops/s | 1.4x slower |
| simpleclientInc | 6.56K | ± 43.79 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.35K | ± 8.54 | ops/s | 10x slower |
| simpleclientAdd | 6.33K | ± 207.68 | ops/s | 10x slower |
| openTelemetryAdd | 3.19K | ± 149.96 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 3.15K | ± 231.74 | ops/s | 21x slower |
| openTelemetryInc | 3.04K | ± 104.73 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.98K | ± 159.35 | ops/s | **fastest** |
| simpleclient | 4.42K | ± 66.35 | ops/s | 1.6x slower |
| prometheusNative | 3.09K | ± 257.57 | ops/s | 2.3x slower |
| openTelemetryClassic | 732.64 | ± 50.11 | ops/s | 9.5x slower |
| openTelemetryExponential | 640.76 | ± 62.91 | ops/s | 11x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.33K | ± 590.79 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.54K | ± 272.83 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 501.43K | ± 6.92K | ops/s | **fastest** |
| prometheusWriteToByteArray | 496.99K | ± 1.75K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.60K | ± 4.63K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 474.97K | ± 3.17K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48406.417   ± 2079.746  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3191.031    ± 149.961  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3035.759    ± 104.732  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3147.452    ± 231.738  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51430.052    ± 124.686  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66186.786    ± 706.099  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56407.835    ± 762.751  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6330.920    ± 207.678  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6563.966     ± 43.791  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6345.618      ± 8.539  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        732.644     ± 50.109  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        640.756     ± 62.907  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6983.723    ± 159.349  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3087.240    ± 257.570  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4423.211     ± 66.352  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23537.034    ± 272.830  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24326.438    ± 590.791  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     474966.437   ± 3174.689  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485603.315   ± 4625.187  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496986.996   ± 1752.001  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     501425.481   ± 6917.097  ops/s
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

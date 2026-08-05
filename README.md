# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-05T04:36:57Z
- **Commit:** [`565a583`](https://github.com/prometheus/client_java/commit/565a58396c92ddfbe1b64de37c40a0a8c165a612)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 57.48K | ± 1.82K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.14K | ± 514.55 | ops/s | 1.1x slower |
| prometheusAdd | 48.82K | ± 786.45 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.91K | ± 169.85 | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 17.22K | ± 102.27 | ops/s | 3.3x slower |
| openTelemetryInc | 13.93K | ± 427.16 | ops/s | 4.1x slower |
| openTelemetryAdd | 12.19K | ± 56.97 | ops/s | 4.7x slower |
| simpleclientInc | 6.18K | ± 39.09 | ops/s | 9.3x slower |
| simpleclientAdd | 5.93K | ± 189.83 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 5.92K | ± 29.31 | ops/s | 9.7x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 13.77K | ± 305.54 | ops/s | **fastest** |
| prometheusClassicSingleThread | 5.81K | ± 17.66 | ops/s | 2.4x slower |
| prometheusClassic | 4.90K | ± 1.77K | ops/s | 2.8x slower |
| simpleclient | 4.56K | ± 76.59 | ops/s | 3.0x slower |
| prometheusNative | 2.90K | ± 252.63 | ops/s | 4.7x slower |
| openTelemetryClassic | 718.78 | ± 44.58 | ops/s | 19x slower |
| openTelemetryExponential | 644.86 | ± 56.31 | ops/s | 21x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 27.67K | ± 40.10 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.38K | ± 201.10 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 560.89K | ± 2.82K | ops/s | **fastest** |
| prometheusWriteToByteArray | 554.40K | ± 3.26K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 534.77K | ± 1.49K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 522.72K | ± 2.67K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43914.460    ± 169.848  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12193.152     ± 56.967  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13927.289    ± 427.156  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17219.583    ± 102.267  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48822.230    ± 786.447  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      57480.448   ± 1816.657  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51139.834    ± 514.554  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5931.411    ± 189.830  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6183.792     ± 39.088  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5919.198     ± 29.313  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        718.778     ± 44.579  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        644.858     ± 56.310  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4904.885   ± 1768.236  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13773.585    ± 305.540  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5812.682     ± 17.665  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2904.657    ± 252.627  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4557.648     ± 76.589  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27382.570    ± 201.098  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27672.197     ± 40.098  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     522724.915   ± 2671.478  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     534771.043   ± 1492.360  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     554400.382   ± 3258.163  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     560893.910   ± 2821.247  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

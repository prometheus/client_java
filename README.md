# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-03T04:39:05Z
- **Commit:** [`922943c`](https://github.com/prometheus/client_java/commit/922943cfe12acb5e373a0a6152384673c3c7b6dc)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 57.03K | ± 3.72K | ops/s | **fastest** |
| prometheusNoLabelsInc | 50.97K | ± 648.09 | ops/s | 1.1x slower |
| prometheusAdd | 48.49K | ± 1.01K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.95K | ± 107.50 | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 17.16K | ± 62.19 | ops/s | 3.3x slower |
| openTelemetryInc | 13.72K | ± 663.27 | ops/s | 4.2x slower |
| openTelemetryAdd | 12.18K | ± 23.23 | ops/s | 4.7x slower |
| simpleclientInc | 6.17K | ± 67.31 | ops/s | 9.2x slower |
| simpleclientAdd | 6.15K | ± 21.21 | ops/s | 9.3x slower |
| simpleclientNoLabelsInc | 5.89K | ± 30.65 | ops/s | 9.7x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 13.73K | ± 196.67 | ops/s | **fastest** |
| prometheusClassic | 6.32K | ± 2.84K | ops/s | 2.2x slower |
| prometheusClassicSingleThread | 5.82K | ± 19.50 | ops/s | 2.4x slower |
| simpleclient | 4.52K | ± 85.58 | ops/s | 3.0x slower |
| prometheusNative | 2.63K | ± 37.59 | ops/s | 5.2x slower |
| openTelemetryClassic | 879.78 | ± 54.79 | ops/s | 16x slower |
| openTelemetryExponential | 665.30 | ± 31.29 | ops/s | 21x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 27.43K | ± 199.91 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.29K | ± 91.59 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 568.70K | ± 3.52K | ops/s | **fastest** |
| prometheusWriteToByteArray | 552.81K | ± 14.34K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 535.09K | ± 2.44K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 508.82K | ± 14.47K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44952.759    ± 107.500  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12178.886     ± 23.233  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13724.694    ± 663.273  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17164.320     ± 62.191  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48488.846   ± 1007.613  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      57032.682   ± 3720.525  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50966.048    ± 648.092  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6151.192     ± 21.208  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6172.515     ± 67.311  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5894.150     ± 30.649  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        879.779     ± 54.792  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        665.297     ± 31.294  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6320.138   ± 2838.815  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13725.658    ± 196.665  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5817.483     ± 19.498  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2631.765     ± 37.588  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4516.156     ± 85.579  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27286.767     ± 91.586  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27430.001    ± 199.909  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     508824.753  ± 14470.220  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     535094.260   ± 2435.717  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     552808.150  ± 14337.309  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     568698.371   ± 3522.563  ops/s
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

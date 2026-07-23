# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-23T04:37:38Z
- **Commit:** [`c291eba`](https://github.com/prometheus/client_java/commit/c291ebad6a15ba7ec57622b9e2a67e9e9eb9e986)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.78K | ± 141.46 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.23K | ± 253.82 | ops/s | 1.1x slower |
| prometheusAdd | 51.43K | ± 237.21 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.50K | ± 1.83K | ops/s | 1.3x slower |
| simpleclientInc | 6.53K | ± 46.66 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 29.74 | ops/s | 10x slower |
| simpleclientAdd | 6.32K | ± 196.12 | ops/s | 10x slower |
| openTelemetryInc | 3.59K | ± 343.78 | ops/s | 18x slower |
| openTelemetryAdd | 3.32K | ± 574.05 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.29K | ± 475.92 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.56K | ± 78.11 | ops/s | **fastest** |
| prometheusClassicSingleThread | 4.59K | ± 12.36 | ops/s | 2.7x slower |
| simpleclient | 4.44K | ± 49.27 | ops/s | 2.8x slower |
| prometheusClassic | 3.98K | ± 108.70 | ops/s | 3.2x slower |
| prometheusNative | 3.01K | ± 251.61 | ops/s | 4.2x slower |
| openTelemetryClassic | 742.66 | ± 34.40 | ops/s | 17x slower |
| openTelemetryExponential | 576.25 | ± 23.41 | ops/s | 22x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.72K | ± 775.81 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.43K | ± 773.20 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 505.74K | ± 8.54K | ops/s | **fastest** |
| prometheusWriteToByteArray | 494.82K | ± 2.05K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 480.86K | ± 2.94K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 471.64K | ± 6.55K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49496.867   ± 1832.397  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3319.467    ± 574.052  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3588.580    ± 343.779  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3290.654    ± 475.920  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51432.726    ± 237.210  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65784.472    ± 141.460  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57226.394    ± 253.818  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6323.630    ± 196.120  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6532.478     ± 46.664  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6364.147     ± 29.741  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        742.661     ± 34.404  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        576.247     ± 23.415  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3978.161    ± 108.703  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12559.233     ± 78.107  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4594.720     ± 12.361  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3009.420    ± 251.606  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4440.448     ± 49.269  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23428.818    ± 773.203  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23719.972    ± 775.814  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     471644.962   ± 6553.300  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     480856.796   ± 2935.896  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     494820.802   ± 2048.444  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     505740.910   ± 8535.706  ops/s
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

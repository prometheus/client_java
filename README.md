# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-11T04:13:59Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 59.24K | ± 489.60 | ops/s | **fastest** |
| prometheusNoLabelsInc | 52.12K | ± 1.02K | ops/s | 1.1x slower |
| prometheusAdd | 48.87K | ± 801.55 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 38.52K | ± 4.18K | ops/s | 1.5x slower |
| openTelemetryIncNoLabels | 17.16K | ± 112.07 | ops/s | 3.5x slower |
| openTelemetryInc | 14.08K | ± 370.86 | ops/s | 4.2x slower |
| openTelemetryAdd | 12.11K | ± 190.11 | ops/s | 4.9x slower |
| simpleclientInc | 6.23K | ± 117.78 | ops/s | 9.5x slower |
| simpleclientNoLabelsInc | 5.88K | ± 26.94 | ops/s | 10x slower |
| simpleclientAdd | 5.86K | ± 294.27 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 14.03K | ± 41.56 | ops/s | **fastest** |
| prometheusClassicSingleThread | 5.82K | ± 10.54 | ops/s | 2.4x slower |
| prometheusClassic | 5.56K | ± 1.58K | ops/s | 2.5x slower |
| simpleclient | 4.48K | ± 181.28 | ops/s | 3.1x slower |
| prometheusNative | 2.98K | ± 238.52 | ops/s | 4.7x slower |
| openTelemetryClassic | 799.02 | ± 82.17 | ops/s | 18x slower |
| openTelemetryExponential | 680.48 | ± 44.55 | ops/s | 21x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 27.50K | ± 174.19 | ops/s | **fastest** |
| prometheusWriteToNull | 27.50K | ± 229.95 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 584.75K | ± 3.32K | ops/s | **fastest** |
| prometheusWriteToByteArray | 572.23K | ± 9.25K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 540.56K | ± 11.90K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 532.32K | ± 15.47K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      38523.836   ± 4176.296  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12114.302    ± 190.110  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14079.204    ± 370.855  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17159.298    ± 112.072  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48870.507    ± 801.555  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59242.787    ± 489.597  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      52122.335   ± 1018.793  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5863.615    ± 294.273  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6227.995    ± 117.780  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5882.214     ± 26.936  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        799.024     ± 82.171  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        680.481     ± 44.545  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5561.043   ± 1575.821  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14026.466     ± 41.561  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5823.924     ± 10.540  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2983.275    ± 238.516  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4481.795    ± 181.285  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27503.158    ± 174.191  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27499.076    ± 229.948  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     532315.596  ± 15467.731  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     540562.088  ± 11903.259  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     572230.835   ± 9245.068  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     584753.979   ± 3322.632  ops/s
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

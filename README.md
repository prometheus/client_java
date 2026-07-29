# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-29T04:35:48Z
- **Commit:** [`c78e5e1`](https://github.com/prometheus/client_java/commit/c78e5e147bb54f03776e52719bb796b7a0e003f7)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.52K | ± 748.62 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.87K | ± 251.15 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.60K | ± 1.51K | ops/s | 1.3x slower |
| prometheusAdd | 47.57K | ± 4.97K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 18.54K | ± 30.49 | ops/s | 3.6x slower |
| openTelemetryInc | 15.38K | ± 308.69 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.71K | ± 438.95 | ops/s | 5.2x slower |
| simpleclientInc | 6.58K | ± 21.53 | ops/s | 10x slower |
| simpleclientAdd | 6.33K | ± 249.39 | ops/s | 11x slower |
| simpleclientNoLabelsInc | 6.33K | ± 7.68 | ops/s | 11x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.59K | ± 116.96 | ops/s | **fastest** |
| prometheusClassic | 6.07K | ± 1.60K | ops/s | 2.1x slower |
| prometheusClassicSingleThread | 4.60K | ± 13.91 | ops/s | 2.7x slower |
| simpleclient | 4.44K | ± 44.57 | ops/s | 2.8x slower |
| prometheusNative | 2.59K | ± 85.24 | ops/s | 4.9x slower |
| openTelemetryClassic | 883.95 | ± 86.89 | ops/s | 14x slower |
| openTelemetryExponential | 730.64 | ± 190.75 | ops/s | 17x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.71K | ± 760.76 | ops/s | **fastest** |
| prometheusWriteToNull | 22.84K | ± 157.63 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 498.94K | ± 3.34K | ops/s | **fastest** |
| prometheusWriteToByteArray | 497.71K | ± 5.80K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 468.11K | ± 3.85K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 465.87K | ± 6.04K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49595.967   ± 1513.405  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12709.122    ± 438.947  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15378.033    ± 308.687  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18540.014     ± 30.493  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47572.118   ± 4969.971  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66524.033    ± 748.625  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56874.660    ± 251.149  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6333.580    ± 249.387  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6576.145     ± 21.527  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6331.314      ± 7.683  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        883.949     ± 86.889  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        730.641    ± 190.751  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6067.081   ± 1601.269  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12594.468    ± 116.960  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4600.127     ± 13.913  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2592.033     ± 85.243  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4442.461     ± 44.573  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23712.307    ± 760.757  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22841.106    ± 157.628  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     465872.329   ± 6042.614  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     468108.348   ± 3850.308  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     497705.072   ± 5802.604  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     498937.890   ± 3342.257  ops/s
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

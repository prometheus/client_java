# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-28T04:37:29Z
- **Commit:** [`372423c`](https://github.com/prometheus/client_java/commit/372423c0e54cff6206e9ba5845bd33f69f6c033c)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 60.45K | ± 772.89 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.61K | ± 483.86 | ops/s | 1.2x slower |
| prometheusAdd | 48.33K | ± 196.05 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 43.99K | ± 91.96 | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 17.09K | ± 70.98 | ops/s | 3.5x slower |
| openTelemetryInc | 13.97K | ± 152.69 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.20K | ± 23.42 | ops/s | 5.0x slower |
| simpleclientInc | 6.17K | ± 55.44 | ops/s | 9.8x slower |
| simpleclientAdd | 6.04K | ± 183.85 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 5.88K | ± 27.72 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 14.50K | ± 38.80 | ops/s | **fastest** |
| prometheusClassicSingleThread | 5.80K | ± 250.64 | ops/s | 2.5x slower |
| prometheusClassic | 5.19K | ± 1.61K | ops/s | 2.8x slower |
| simpleclient | 4.48K | ± 28.12 | ops/s | 3.2x slower |
| prometheusNative | 2.89K | ± 188.44 | ops/s | 5.0x slower |
| openTelemetryClassic | 812.77 | ± 55.96 | ops/s | 18x slower |
| openTelemetryExponential | 721.22 | ± 54.37 | ops/s | 20x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 27.49K | ± 198.20 | ops/s | **fastest** |
| openMetricsWriteToNull | 26.31K | ± 1.03K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 576.84K | ± 3.55K | ops/s | **fastest** |
| prometheusWriteToByteArray | 569.74K | ± 4.35K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 547.32K | ± 4.24K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 539.27K | ± 3.63K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43994.760     ± 91.962  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12201.150     ± 23.419  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13969.337    ± 152.693  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17087.502     ± 70.981  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48330.065    ± 196.047  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60446.110    ± 772.888  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51606.150    ± 483.855  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6042.237    ± 183.855  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6173.555     ± 55.435  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5880.088     ± 27.720  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        812.774     ± 55.962  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        721.220     ± 54.370  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5186.569   ± 1611.651  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14496.855     ± 38.796  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5803.835    ± 250.643  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2891.931    ± 188.445  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4481.926     ± 28.122  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      26311.680   ± 1034.260  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27487.602    ± 198.197  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     539265.430   ± 3626.622  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     547316.063   ± 4244.377  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     569741.959   ± 4345.790  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     576844.002   ± 3549.485  ops/s
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

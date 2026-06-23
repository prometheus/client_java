# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-23T04:37:05Z
- **Commit:** [`a017f80`](https://github.com/prometheus/client_java/commit/a017f80980d91a5fa8ffe930c820f836c3d1b2ff)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.69K | ± 1.64K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.54K | ± 754.01 | ops/s | 1.1x slower |
| prometheusAdd | 50.79K | ± 616.53 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.51K | ± 1.48K | ops/s | 1.3x slower |
| simpleclientInc | 6.61K | ± 48.81 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.37K | ± 32.07 | ops/s | 10x slower |
| simpleclientAdd | 6.12K | ± 372.42 | ops/s | 11x slower |
| openTelemetryInc | 3.79K | ± 409.84 | ops/s | 17x slower |
| openTelemetryIncNoLabels | 3.32K | ± 334.42 | ops/s | 20x slower |
| openTelemetryAdd | 2.97K | ± 242.23 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.71K | ± 979.83 | ops/s | **fastest** |
| simpleclient | 4.46K | ± 65.25 | ops/s | 1.3x slower |
| prometheusNative | 3.02K | ± 405.07 | ops/s | 1.9x slower |
| openTelemetryClassic | 760.96 | ± 46.15 | ops/s | 7.5x slower |
| openTelemetryExponential | 586.82 | ± 21.59 | ops/s | 9.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.59K | ± 923.33 | ops/s | **fastest** |
| prometheusWriteToNull | 22.88K | ± 744.91 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 511.89K | ± 10.80K | ops/s | **fastest** |
| prometheusWriteToByteArray | 509.26K | ± 7.10K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 495.47K | ± 2.35K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 486.04K | ± 1.95K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48512.169   ± 1478.330  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2968.712    ± 242.232  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3789.214    ± 409.845  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3317.196    ± 334.417  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50787.579    ± 616.534  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64688.716   ± 1636.183  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56538.898    ± 754.007  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6124.922    ± 372.418  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6611.570     ± 48.815  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6369.232     ± 32.067  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        760.964     ± 46.152  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        586.816     ± 21.588  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5712.749    ± 979.831  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3017.512    ± 405.068  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4457.698     ± 65.255  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23594.513    ± 923.326  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22875.108    ± 744.906  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     486038.677   ± 1954.524  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     495471.163   ± 2352.266  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     509260.917   ± 7099.781  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     511886.554  ± 10799.289  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-10T04:21:52Z
- **Commit:** [`e854af4`](https://github.com/prometheus/client_java/commit/e854af48392c5ad5535a153bafa62253d2dced24)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.90K | ± 1.22K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.41K | ± 915.80 | ops/s | 1.2x slower |
| prometheusAdd | 51.53K | ± 235.09 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.88K | ± 463.94 | ops/s | 1.3x slower |
| simpleclientInc | 6.68K | ± 115.14 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.49K | ± 186.35 | ops/s | 10x slower |
| simpleclientAdd | 6.39K | ± 252.88 | ops/s | 10x slower |
| openTelemetryAdd | 1.51K | ± 226.53 | ops/s | 43x slower |
| openTelemetryInc | 1.27K | ± 29.98 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.21K | ± 38.95 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.62K | ± 777.53 | ops/s | **fastest** |
| simpleclient | 4.52K | ± 28.28 | ops/s | 1.0x slower |
| prometheusNative | 3.19K | ± 124.35 | ops/s | 1.4x slower |
| openTelemetryClassic | 701.73 | ± 30.41 | ops/s | 6.6x slower |
| openTelemetryExponential | 566.35 | ± 19.05 | ops/s | 8.2x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 483.90K | ± 3.33K | ops/s | **fastest** |
| prometheusWriteToByteArray | 480.50K | ± 2.60K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 476.75K | ± 4.50K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 472.78K | ± 1.98K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49878.624    ± 463.944  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1513.696    ± 226.533  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1272.390     ± 29.976  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1210.012     ± 38.954  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51531.329    ± 235.085  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64898.407   ± 1222.851  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56414.781    ± 915.803  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6393.575    ± 252.883  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6675.495    ± 115.144  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6485.044    ± 186.345  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        701.732     ± 30.408  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        566.355     ± 19.046  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4617.355    ± 777.535  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3194.777    ± 124.350  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4517.794     ± 28.280  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     476749.776   ± 4495.718  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     472779.760   ± 1978.594  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     480501.039   ± 2599.996  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     483902.102   ± 3327.678  ops/s
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

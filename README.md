# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-03T04:41:13Z
- **Commit:** [`ab37635`](https://github.com/prometheus/client_java/commit/ab3763518ee1d5d95f5cd2580cacbd4f4be7ac8d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.51K | ± 654.32 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.00K | ± 764.14 | ops/s | 1.2x slower |
| prometheusAdd | 51.11K | ± 238.91 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.64K | ± 1.84K | ops/s | 1.4x slower |
| simpleclientInc | 6.55K | ± 33.96 | ops/s | 10x slower |
| simpleclientAdd | 6.38K | ± 220.44 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 220.28 | ops/s | 10x slower |
| openTelemetryAdd | 3.27K | ± 313.54 | ops/s | 20x slower |
| openTelemetryInc | 3.27K | ± 284.30 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.21K | ± 143.12 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.30K | ± 1.19K | ops/s | **fastest** |
| simpleclient | 4.48K | ± 22.42 | ops/s | 1.4x slower |
| prometheusNative | 2.78K | ± 334.01 | ops/s | 2.3x slower |
| openTelemetryClassic | 777.61 | ± 10.91 | ops/s | 8.1x slower |
| openTelemetryExponential | 698.17 | ± 23.49 | ops/s | 9.0x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.72K | ± 272.60 | ops/s | **fastest** |
| prometheusWriteToNull | 23.53K | ± 822.64 | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 491.60K | ± 5.68K | ops/s | **fastest** |
| prometheusWriteToNull | 488.78K | ± 7.24K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.11K | ± 5.54K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.83K | ± 4.29K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48642.468   ± 1839.424  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3266.508    ± 313.543  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3265.627    ± 284.297  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3207.649    ± 143.121  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51107.227    ± 238.911  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66507.034    ± 654.319  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55998.256    ± 764.140  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6376.752    ± 220.436  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6554.031     ± 33.963  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6359.890    ± 220.282  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        777.611     ± 10.914  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        698.174     ± 23.487  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6304.028   ± 1190.755  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2783.024    ± 334.011  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4477.146     ± 22.422  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24718.051    ± 272.595  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23532.604    ± 822.638  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483833.813   ± 4287.011  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485108.672   ± 5540.151  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491602.411   ± 5682.444  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     488782.535   ± 7236.946  ops/s
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

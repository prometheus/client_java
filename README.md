# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-20T04:37:20Z
- **Commit:** [`da14412`](https://github.com/prometheus/client_java/commit/da144125367c0410df66120631f6cae5ead25fcb)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.80K | ± 131.49 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.09K | ± 939.39 | ops/s | 1.2x slower |
| prometheusAdd | 50.33K | ± 234.73 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.49K | ± 1.87K | ops/s | 1.4x slower |
| simpleclientInc | 6.54K | ± 31.97 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.33K | ± 10.41 | ops/s | 10x slower |
| simpleclientAdd | 6.18K | ± 216.91 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 3.64K | ± 325.72 | ops/s | 18x slower |
| openTelemetryInc | 3.44K | ± 333.46 | ops/s | 19x slower |
| openTelemetryAdd | 3.29K | ± 416.34 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.49K | ± 1.89K | ops/s | **fastest** |
| simpleclient | 4.40K | ± 68.34 | ops/s | 1.2x slower |
| prometheusNative | 2.90K | ± 271.56 | ops/s | 1.9x slower |
| openTelemetryClassic | 738.61 | ± 27.78 | ops/s | 7.4x slower |
| openTelemetryExponential | 625.50 | ± 77.74 | ops/s | 8.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.16K | ± 527.17 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.58K | ± 880.47 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 490.90K | ± 6.02K | ops/s | **fastest** |
| prometheusWriteToByteArray | 487.02K | ± 4.39K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 474.22K | ± 5.60K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 465.45K | ± 4.89K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48490.663   ± 1868.859  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3292.683    ± 416.336  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3443.578    ± 333.464  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3635.391    ± 325.723  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50325.715    ± 234.735  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65795.878    ± 131.489  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56087.149    ± 939.392  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6177.734    ± 216.906  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6542.511     ± 31.973  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6331.017     ± 10.413  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        738.609     ± 27.780  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        625.504     ± 77.744  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5492.825   ± 1886.116  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2897.795    ± 271.563  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4403.525     ± 68.344  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23584.399    ± 880.466  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24157.310    ± 527.170  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     465451.654   ± 4889.673  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     474220.501   ± 5601.152  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     487016.997   ± 4391.641  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     490903.902   ± 6017.044  ops/s
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

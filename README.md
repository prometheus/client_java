# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-29T04:36:20Z
- **Commit:** [`ebd00ba`](https://github.com/prometheus/client_java/commit/ebd00baaf85655bd3f293e786ae56d414250af6b)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.30K | ± 753.57 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.03K | ± 528.50 | ops/s | 1.2x slower |
| prometheusAdd | 48.07K | ± 298.82 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.25K | ± 635.11 | ops/s | 1.4x slower |
| simpleclientInc | 6.27K | ± 51.01 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.27K | ± 23.34 | ops/s | 9.6x slower |
| simpleclientAdd | 5.73K | ± 327.95 | ops/s | 11x slower |
| openTelemetryInc | 5.47K | ± 700.56 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 4.63K | ± 363.01 | ops/s | 13x slower |
| openTelemetryAdd | 3.82K | ± 977.56 | ops/s | 16x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.14K | ± 2.40K | ops/s | **fastest** |
| simpleclient | 4.20K | ± 55.62 | ops/s | 1.5x slower |
| prometheusNative | 2.97K | ± 242.59 | ops/s | 2.1x slower |
| openTelemetryClassic | 721.81 | ± 14.64 | ops/s | 8.5x slower |
| openTelemetryExponential | 536.75 | ± 15.86 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 559.61K | ± 1.82K | ops/s | **fastest** |
| prometheusWriteToByteArray | 546.14K | ± 6.35K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 544.06K | ± 5.12K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 528.58K | ± 4.53K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44252.405    ± 635.107  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3816.218    ± 977.563  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5474.858    ± 700.556  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4632.842    ± 363.013  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48073.767    ± 298.818  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60297.454    ± 753.570  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51032.282    ± 528.495  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5725.932    ± 327.954  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6274.910     ± 51.006  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6268.008     ± 23.345  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        721.810     ± 14.638  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        536.751     ± 15.858  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6139.889   ± 2401.737  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2974.235    ± 242.595  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4197.361     ± 55.620  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     528580.617   ± 4532.468  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     544060.390   ± 5118.856  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     546138.620   ± 6354.482  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     559610.033   ± 1818.749  ops/s
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

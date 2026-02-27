# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-27T04:23:16Z
- **Commit:** [`35735f9`](https://github.com/prometheus/client_java/commit/35735f9e41c5f1eb7dfbf739cc3e3507eeba15a7)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.61K | ± 1.54K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.28K | ± 1.25K | ops/s | 1.1x slower |
| prometheusAdd | 50.87K | ± 744.20 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.50K | ± 1.41K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.57K | ± 220.08 | ops/s | 9.8x slower |
| simpleclientInc | 6.56K | ± 182.89 | ops/s | 9.8x slower |
| simpleclientAdd | 6.41K | ± 228.18 | ops/s | 10x slower |
| openTelemetryInc | 1.45K | ± 177.13 | ops/s | 45x slower |
| openTelemetryAdd | 1.42K | ± 168.19 | ops/s | 45x slower |
| openTelemetryIncNoLabels | 1.19K | ± 33.03 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.52K | ± 31.48 | ops/s | **fastest** |
| prometheusClassic | 4.38K | ± 488.19 | ops/s | 1.0x slower |
| prometheusNative | 2.84K | ± 349.58 | ops/s | 1.6x slower |
| openTelemetryClassic | 726.19 | ± 46.44 | ops/s | 6.2x slower |
| openTelemetryExponential | 546.01 | ± 36.55 | ops/s | 8.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.11K | ± 5.31K | ops/s | **fastest** |
| prometheusWriteToByteArray | 483.53K | ± 1.07K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 481.02K | ± 2.46K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 470.41K | ± 4.93K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48500.901   ± 1409.129  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1421.935    ± 168.194  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1446.479    ± 177.134  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1186.908     ± 33.025  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50865.435    ± 744.195  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64607.836   ± 1535.623  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56276.341   ± 1245.414  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6406.258    ± 228.183  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6561.780    ± 182.889  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6574.978    ± 220.076  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        726.188     ± 46.444  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        546.015     ± 36.545  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4384.745    ± 488.189  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2840.377    ± 349.579  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4517.376     ± 31.479  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     470406.421   ± 4928.843  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481019.327   ± 2458.103  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     483525.674   ± 1071.497  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491113.766   ± 5312.279  ops/s
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

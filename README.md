# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-09T04:32:44Z
- **Commit:** [`79a5990`](https://github.com/prometheus/client_java/commit/79a5990fbde8597023bb40a07e9f77e32b19fdd1)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.41K | ± 657.28 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.00K | ± 407.71 | ops/s | 1.2x slower |
| prometheusAdd | 51.46K | ± 159.10 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.82K | ± 2.02K | ops/s | 1.4x slower |
| simpleclientInc | 6.55K | ± 84.20 | ops/s | 10x slower |
| simpleclientAdd | 6.46K | ± 12.06 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 29.56 | ops/s | 10x slower |
| openTelemetryAdd | 3.45K | ± 311.95 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.05K | ± 71.21 | ops/s | 22x slower |
| openTelemetryInc | 2.89K | ± 43.20 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.55K | ± 1.42K | ops/s | **fastest** |
| simpleclient | 4.40K | ± 54.26 | ops/s | 1.3x slower |
| prometheusNative | 2.70K | ± 341.13 | ops/s | 2.1x slower |
| openTelemetryClassic | 761.91 | ± 25.00 | ops/s | 7.3x slower |
| openTelemetryExponential | 714.71 | ± 80.77 | ops/s | 7.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.66K | ± 310.80 | ops/s | **fastest** |
| prometheusWriteToNull | 23.06K | ± 730.10 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 470.85K | ± 4.57K | ops/s | **fastest** |
| prometheusWriteToByteArray | 470.67K | ± 5.51K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 454.62K | ± 2.13K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 446.93K | ± 9.34K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47816.338   ± 2017.501  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3445.455    ± 311.951  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2890.911     ± 43.200  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3050.264     ± 71.210  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51459.749    ± 159.103  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66410.199    ± 657.283  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57001.171    ± 407.714  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6459.961     ± 12.065  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6553.285     ± 84.203  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6361.537     ± 29.562  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        761.905     ± 24.999  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        714.705     ± 80.768  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5550.810   ± 1422.388  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2696.592    ± 341.127  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4404.701     ± 54.260  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23655.205    ± 310.804  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23060.303    ± 730.104  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     446932.162   ± 9343.496  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     454621.464   ± 2134.123  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     470668.012   ± 5505.070  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     470849.118   ± 4569.948  ops/s
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

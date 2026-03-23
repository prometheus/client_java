# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-23T04:30:43Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.05K | ± 2.30K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.25K | ± 1.51K | ops/s | 1.2x slower |
| prometheusAdd | 47.80K | ± 965.42 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.02K | ± 215.67 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.39K | ± 31.14 | ops/s | 9.2x slower |
| simpleclientInc | 6.24K | ± 208.40 | ops/s | 9.5x slower |
| simpleclientAdd | 5.84K | ± 326.32 | ops/s | 10x slower |
| openTelemetryInc | 1.36K | ± 63.79 | ops/s | 44x slower |
| openTelemetryIncNoLabels | 1.34K | ± 155.16 | ops/s | 44x slower |
| openTelemetryAdd | 1.31K | ± 14.13 | ops/s | 45x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.13K | ± 1.85K | ops/s | **fastest** |
| simpleclient | 4.53K | ± 86.43 | ops/s | 1.1x slower |
| prometheusNative | 3.05K | ± 151.83 | ops/s | 1.7x slower |
| openTelemetryClassic | 622.15 | ± 7.70 | ops/s | 8.2x slower |
| openTelemetryExponential | 543.66 | ± 27.18 | ops/s | 9.4x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 555.55K | ± 5.53K | ops/s | **fastest** |
| prometheusWriteToByteArray | 545.54K | ± 3.24K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 536.94K | ± 8.24K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 528.32K | ± 5.14K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44024.431    ± 215.671  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1314.272     ± 14.131  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1355.159     ± 63.788  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1340.053    ± 155.163  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47797.497    ± 965.420  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59049.325   ± 2304.154  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51246.534   ± 1514.417  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5840.232    ± 326.318  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6244.849    ± 208.397  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6390.717     ± 31.137  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        622.146      ± 7.696  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        543.663     ± 27.183  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5125.087   ± 1852.403  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3051.767    ± 151.827  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4528.283     ± 86.432  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     528320.246   ± 5139.721  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     536937.338   ± 8235.250  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     545540.735   ± 3237.657  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     555546.015   ± 5527.458  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-16T04:39:44Z
- **Commit:** [`9672749`](https://github.com/prometheus/client_java/commit/9672749085f9029ccb7328b3e88e8e78fa29e402)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.98K | ± 3.53K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.86K | ± 433.23 | ops/s | 1.1x slower |
| prometheusAdd | 51.24K | ± 224.46 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.68K | ± 1.38K | ops/s | 1.3x slower |
| simpleclientInc | 6.63K | ± 69.81 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.35K | ± 10.81 | ops/s | 10x slower |
| simpleclientAdd | 5.98K | ± 496.19 | ops/s | 11x slower |
| openTelemetryAdd | 3.66K | ± 231.52 | ops/s | 17x slower |
| openTelemetryIncNoLabels | 3.51K | ± 764.79 | ops/s | 18x slower |
| openTelemetryInc | 3.37K | ± 545.65 | ops/s | 19x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.20K | ± 873.65 | ops/s | **fastest** |
| simpleclient | 4.37K | ± 71.67 | ops/s | 1.4x slower |
| prometheusNative | 2.87K | ± 173.49 | ops/s | 2.2x slower |
| openTelemetryClassic | 750.91 | ± 45.32 | ops/s | 8.3x slower |
| openTelemetryExponential | 683.50 | ± 97.97 | ops/s | 9.1x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.25K | ± 346.90 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.89K | ± 816.36 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 517.69K | ± 4.95K | ops/s | **fastest** |
| prometheusWriteToByteArray | 513.85K | ± 6.14K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 492.52K | ± 2.20K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 485.07K | ± 4.22K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48684.027   ± 1383.473  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3664.342    ± 231.515  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3367.501    ± 545.651  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3510.468    ± 764.790  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51239.707    ± 224.464  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63976.843   ± 3526.868  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56858.929    ± 433.226  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5975.265    ± 496.194  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6629.256     ± 69.806  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6345.780     ± 10.811  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        750.907     ± 45.316  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        683.503     ± 97.965  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6198.164    ± 873.646  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2867.979    ± 173.491  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4371.008     ± 71.672  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23893.526    ± 816.365  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24245.727    ± 346.905  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     485072.409   ± 4219.779  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     492518.804   ± 2200.412  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     513847.122   ± 6137.824  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     517691.910   ± 4945.504  ops/s
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

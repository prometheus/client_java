# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-01T04:30:56Z
- **Commit:** [`deb782f`](https://github.com/prometheus/client_java/commit/deb782f9fce60ffb1308a98b661c0a1ccb79a82b)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 56.69K | ± 2.42K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.82K | ± 771.01 | ops/s | 1.1x slower |
| prometheusAdd | 48.61K | ± 1.05K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.94K | ± 96.61 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.10K | ± 274.20 | ops/s | 9.3x slower |
| simpleclientInc | 6.09K | ± 85.67 | ops/s | 9.3x slower |
| simpleclientAdd | 6.04K | ± 187.33 | ops/s | 9.4x slower |
| openTelemetryAdd | 1.42K | ± 90.01 | ops/s | 40x slower |
| openTelemetryInc | 1.36K | ± 31.42 | ops/s | 42x slower |
| openTelemetryIncNoLabels | 1.33K | ± 12.02 | ops/s | 43x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.11K | ± 1.82K | ops/s | **fastest** |
| simpleclient | 4.29K | ± 92.08 | ops/s | 1.4x slower |
| prometheusNative | 2.84K | ± 93.00 | ops/s | 2.2x slower |
| openTelemetryClassic | 628.25 | ± 32.26 | ops/s | 9.7x slower |
| openTelemetryExponential | 524.44 | ± 43.48 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 539.52K | ± 7.89K | ops/s | **fastest** |
| prometheusWriteToByteArray | 523.18K | ± 8.28K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 514.36K | ± 7.03K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 510.37K | ± 5.92K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43944.621     ± 96.606  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1415.319     ± 90.010  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1360.965     ± 31.425  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1326.639     ± 12.016  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48609.103   ± 1048.288  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      56694.036   ± 2419.382  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51815.449    ± 771.007  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6042.808    ± 187.328  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6090.136     ± 85.673  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6102.810    ± 274.201  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        628.249     ± 32.255  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        524.436     ± 43.481  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6111.765   ± 1821.278  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2840.826     ± 93.002  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4292.971     ± 92.081  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     510365.675   ± 5922.023  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     514362.865   ± 7033.848  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     523178.562   ± 8280.032  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     539521.005   ± 7887.178  ops/s
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

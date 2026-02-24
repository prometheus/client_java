# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-24T04:26:02Z
- **Commit:** [`122b09c`](https://github.com/prometheus/client_java/commit/122b09cf3af6137354c2925f9054e49271047484)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 56.71K | ± 2.52K | ops/s | **fastest** |
| prometheusNoLabelsInc | 52.01K | ± 357.91 | ops/s | 1.1x slower |
| prometheusAdd | 48.97K | ± 668.24 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.57K | ± 665.22 | ops/s | 1.3x slower |
| simpleclientInc | 6.48K | ± 30.47 | ops/s | 8.7x slower |
| simpleclientNoLabelsInc | 6.16K | ± 218.08 | ops/s | 9.2x slower |
| simpleclientAdd | 5.91K | ± 30.80 | ops/s | 9.6x slower |
| openTelemetryAdd | 1.49K | ± 132.33 | ops/s | 38x slower |
| openTelemetryInc | 1.35K | ± 48.27 | ops/s | 42x slower |
| openTelemetryIncNoLabels | 1.34K | ± 16.15 | ops/s | 42x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.31K | ± 501.55 | ops/s | **fastest** |
| simpleclient | 4.54K | ± 59.64 | ops/s | 1.2x slower |
| prometheusNative | 3.15K | ± 24.96 | ops/s | 1.7x slower |
| openTelemetryClassic | 601.31 | ± 9.97 | ops/s | 8.8x slower |
| openTelemetryExponential | 507.65 | ± 27.90 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 552.15K | ± 6.55K | ops/s | **fastest** |
| prometheusWriteToByteArray | 548.21K | ± 5.03K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 539.96K | ± 7.06K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 527.44K | ± 3.89K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44573.903    ± 665.220  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1492.653    ± 132.331  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1350.521     ± 48.274  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1340.553     ± 16.148  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48973.016    ± 668.245  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      56711.340   ± 2524.506  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      52013.088    ± 357.909  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5907.471     ± 30.795  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6484.492     ± 30.470  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6161.558    ± 218.079  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        601.306      ± 9.973  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        507.646     ± 27.897  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5306.528    ± 501.551  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3154.217     ± 24.964  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4536.048     ± 59.640  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     527442.454   ± 3890.456  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     539961.787   ± 7056.682  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     548210.050   ± 5032.934  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     552150.687   ± 6550.936  ops/s
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

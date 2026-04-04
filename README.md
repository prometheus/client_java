# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-04T04:27:04Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.75K | ± 1.74K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.02K | ± 311.71 | ops/s | 1.2x slower |
| prometheusAdd | 50.96K | ± 507.45 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.31K | ± 507.90 | ops/s | 1.4x slower |
| simpleclientInc | 6.60K | ± 185.05 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.37K | ± 214.41 | ops/s | 10x slower |
| simpleclientAdd | 6.18K | ± 197.27 | ops/s | 11x slower |
| openTelemetryInc | 1.34K | ± 236.74 | ops/s | 49x slower |
| openTelemetryIncNoLabels | 1.32K | ± 240.25 | ops/s | 50x slower |
| openTelemetryAdd | 1.25K | ± 18.43 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.53K | ± 1.27K | ops/s | **fastest** |
| simpleclient | 4.41K | ± 19.88 | ops/s | 1.7x slower |
| prometheusNative | 3.08K | ± 245.18 | ops/s | 2.4x slower |
| openTelemetryClassic | 686.19 | ± 36.60 | ops/s | 11x slower |
| openTelemetryExponential | 556.68 | ± 30.68 | ops/s | 14x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.01K | ± 1.83K | ops/s | **fastest** |
| openMetricsWriteToNull | 486.80K | ± 2.39K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 485.02K | ± 3.77K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 484.13K | ± 7.48K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47313.897    ± 507.896  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1245.707     ± 18.429  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1342.327    ± 236.743  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1322.425    ± 240.246  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50956.202    ± 507.448  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65749.469   ± 1744.559  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57018.153    ± 311.711  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6180.116    ± 197.274  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6599.296    ± 185.049  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6366.864    ± 214.409  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        686.188     ± 36.600  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        556.677     ± 30.677  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7532.230   ± 1267.474  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3084.854    ± 245.183  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4414.044     ± 19.885  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     485023.505   ± 3768.909  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486803.933   ± 2387.531  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     484125.966   ± 7480.221  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491005.721   ± 1832.784  ops/s
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

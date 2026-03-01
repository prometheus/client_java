# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-01T04:29:18Z
- **Commit:** [`6938479`](https://github.com/prometheus/client_java/commit/69384791685f0e86a28f04191434ecab310365ba)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.09K | ± 407.02 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.22K | ± 187.46 | ops/s | 1.2x slower |
| prometheusAdd | 51.04K | ± 263.69 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.01K | ± 525.01 | ops/s | 1.3x slower |
| simpleclientInc | 6.77K | ± 21.35 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.46K | ± 189.81 | ops/s | 10x slower |
| simpleclientAdd | 6.30K | ± 211.95 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.36K | ± 186.17 | ops/s | 49x slower |
| openTelemetryInc | 1.34K | ± 148.03 | ops/s | 49x slower |
| openTelemetryAdd | 1.30K | ± 63.87 | ops/s | 51x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.15K | ± 1.53K | ops/s | **fastest** |
| simpleclient | 4.56K | ± 75.14 | ops/s | 1.6x slower |
| prometheusNative | 3.22K | ± 110.54 | ops/s | 2.2x slower |
| openTelemetryClassic | 686.35 | ± 72.93 | ops/s | 10x slower |
| openTelemetryExponential | 556.29 | ± 26.46 | ops/s | 13x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 478.23K | ± 2.65K | ops/s | **fastest** |
| prometheusWriteToByteArray | 475.21K | ± 5.28K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 464.87K | ± 4.35K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 461.36K | ± 4.58K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50010.959    ± 525.006  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1295.168     ± 63.872  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1343.779    ± 148.025  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1356.264    ± 186.173  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51036.623    ± 263.686  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66094.780    ± 407.020  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57215.291    ± 187.461  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6300.354    ± 211.951  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6766.799     ± 21.348  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6455.432    ± 189.810  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        686.352     ± 72.935  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        556.294     ± 26.459  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7154.990   ± 1530.072  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3217.101    ± 110.536  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4557.099     ± 75.144  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     464867.041   ± 4353.004  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     461355.168   ± 4584.012  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     475205.303   ± 5283.701  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     478229.728   ± 2646.520  ops/s
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

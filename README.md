# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-26T04:24:19Z
- **Commit:** [`28e49da`](https://github.com/prometheus/client_java/commit/28e49dac7fd80d6c83adfb054a23e9e15ce627b6)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.66K | ± 1.44K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.20K | ± 159.16 | ops/s | 1.1x slower |
| prometheusAdd | 51.11K | ± 294.92 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.42K | ± 1.29K | ops/s | 1.3x slower |
| simpleclientInc | 6.64K | ± 182.03 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.52K | ± 142.14 | ops/s | 10x slower |
| simpleclientAdd | 6.31K | ± 372.15 | ops/s | 10x slower |
| openTelemetryAdd | 1.46K | ± 277.52 | ops/s | 45x slower |
| openTelemetryIncNoLabels | 1.39K | ± 181.53 | ops/s | 47x slower |
| openTelemetryInc | 1.26K | ± 38.18 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.54K | ± 77.28 | ops/s | **fastest** |
| prometheusClassic | 4.11K | ± 154.53 | ops/s | 1.1x slower |
| prometheusNative | 2.92K | ± 254.71 | ops/s | 1.6x slower |
| openTelemetryClassic | 663.46 | ± 24.05 | ops/s | 6.8x slower |
| openTelemetryExponential | 588.33 | ± 10.60 | ops/s | 7.7x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.75K | ± 2.45K | ops/s | **fastest** |
| prometheusWriteToByteArray | 489.44K | ± 2.76K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.70K | ± 3.44K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 476.42K | ± 2.63K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49420.789   ± 1285.156  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1457.143    ± 277.525  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1260.987     ± 38.177  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1389.379    ± 181.530  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51105.004    ± 294.920  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65662.774   ± 1437.898  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57204.182    ± 159.157  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6308.431    ± 372.146  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6641.077    ± 182.033  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6517.882    ± 142.140  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        663.456     ± 24.050  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        588.325     ± 10.595  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4108.483    ± 154.531  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2924.222    ± 254.709  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4537.258     ± 77.275  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     476423.337   ± 2627.199  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487700.993   ± 3441.707  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489444.799   ± 2757.104  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492751.934   ± 2447.592  ops/s
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

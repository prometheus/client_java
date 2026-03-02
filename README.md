# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-02T04:25:03Z
- **Commit:** [`7fe2528`](https://github.com/prometheus/client_java/commit/7fe2528a85574ff6ae35e539039619c4a6db7231)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.13K | ± 302.37 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.86K | ± 222.78 | ops/s | 1.2x slower |
| prometheusAdd | 50.98K | ± 206.80 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.21K | ± 1.30K | ops/s | 1.3x slower |
| simpleclientInc | 6.78K | ± 21.30 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.59K | ± 146.20 | ops/s | 10x slower |
| simpleclientAdd | 6.41K | ± 229.01 | ops/s | 10x slower |
| openTelemetryAdd | 1.65K | ± 330.08 | ops/s | 40x slower |
| openTelemetryIncNoLabels | 1.30K | ± 120.69 | ops/s | 51x slower |
| openTelemetryInc | 1.27K | ± 77.31 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.64K | ± 555.47 | ops/s | **fastest** |
| simpleclient | 4.50K | ± 59.02 | ops/s | 1.0x slower |
| prometheusNative | 2.88K | ± 184.17 | ops/s | 1.6x slower |
| openTelemetryClassic | 728.64 | ± 30.56 | ops/s | 6.4x slower |
| openTelemetryExponential | 546.52 | ± 26.04 | ops/s | 8.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 494.76K | ± 3.24K | ops/s | **fastest** |
| prometheusWriteToByteArray | 484.74K | ± 6.02K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.80K | ± 10.35K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 477.65K | ± 6.87K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49214.792   ± 1296.979  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1645.099    ± 330.078  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1271.133     ± 77.306  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1304.556    ± 120.689  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50977.402    ± 206.798  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66130.353    ± 302.371  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56862.524    ± 222.776  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6412.290    ± 229.014  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6781.317     ± 21.298  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6588.681    ± 146.201  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        728.644     ± 30.557  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        546.522     ± 26.042  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4636.918    ± 555.468  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2880.576    ± 184.168  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4501.305     ± 59.024  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477654.915   ± 6871.750  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482795.640  ± 10354.344  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     484741.857   ± 6015.697  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     494760.803   ± 3236.276  ops/s
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

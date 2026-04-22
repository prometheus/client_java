# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-22T04:33:49Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1011-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.78K | ± 1.41K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.17K | ± 857.91 | ops/s | 1.2x slower |
| prometheusAdd | 51.42K | ± 215.83 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.82K | ± 1.31K | ops/s | 1.3x slower |
| simpleclientInc | 6.70K | ± 20.46 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.47K | ± 237.44 | ops/s | 10x slower |
| simpleclientAdd | 6.33K | ± 252.19 | ops/s | 10x slower |
| openTelemetryAdd | 1.38K | ± 271.18 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.29K | ± 240.47 | ops/s | 50x slower |
| openTelemetryInc | 1.27K | ± 67.15 | ops/s | 51x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.25K | ± 1.31K | ops/s | **fastest** |
| simpleclient | 4.46K | ± 58.69 | ops/s | 1.2x slower |
| prometheusNative | 2.86K | ± 311.79 | ops/s | 1.8x slower |
| openTelemetryClassic | 689.67 | ± 33.72 | ops/s | 7.6x slower |
| openTelemetryExponential | 557.67 | ± 34.82 | ops/s | 9.4x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 489.92K | ± 3.69K | ops/s | **fastest** |
| prometheusWriteToNull | 487.08K | ± 2.89K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 484.95K | ± 5.19K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 482.70K | ± 3.45K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48822.355   ± 1310.400  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1382.846    ± 271.185  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1269.873     ± 67.152  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1290.568    ± 240.470  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51423.188    ± 215.832  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64776.692   ± 1411.246  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56169.353    ± 857.910  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6329.225    ± 252.189  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6704.168     ± 20.465  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6465.470    ± 237.437  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        689.673     ± 33.724  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        557.667     ± 34.817  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5248.134   ± 1306.933  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2855.451    ± 311.793  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4464.270     ± 58.690  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     484948.719   ± 5192.053  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489923.440   ± 3692.944  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     482704.572   ± 3450.694  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     487079.022   ± 2894.874  ops/s
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

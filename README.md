# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-02T04:30:26Z
- **Commit:** [`deb782f`](https://github.com/prometheus/client_java/commit/deb782f9fce60ffb1308a98b661c0a1ccb79a82b)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.34K | ± 4.56K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.69K | ± 448.98 | ops/s | 1.1x slower |
| prometheusAdd | 51.61K | ± 141.64 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.71K | ± 1.77K | ops/s | 1.3x slower |
| simpleclientInc | 6.57K | ± 217.83 | ops/s | 9.6x slower |
| simpleclientAdd | 6.28K | ± 203.60 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.24K | ± 94.74 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.29K | ± 242.00 | ops/s | 49x slower |
| openTelemetryAdd | 1.26K | ± 61.18 | ops/s | 50x slower |
| openTelemetryInc | 1.24K | ± 30.74 | ops/s | 51x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.13K | ± 918.56 | ops/s | **fastest** |
| simpleclient | 4.43K | ± 63.82 | ops/s | 1.6x slower |
| prometheusNative | 3.08K | ± 344.33 | ops/s | 2.3x slower |
| openTelemetryClassic | 688.62 | ± 12.81 | ops/s | 10x slower |
| openTelemetryExponential | 542.57 | ± 29.92 | ops/s | 13x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 469.97K | ± 8.90K | ops/s | **fastest** |
| prometheusWriteToByteArray | 467.41K | ± 13.16K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 456.27K | ± 6.11K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 448.91K | ± 11.86K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49708.065   ± 1768.341  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1261.048     ± 61.180  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1242.278     ± 30.743  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1288.809    ± 242.003  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51608.563    ± 141.639  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63337.826   ± 4559.230  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56686.075    ± 448.981  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6280.030    ± 203.597  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6567.468    ± 217.829  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6244.502     ± 94.737  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        688.618     ± 12.808  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        542.568     ± 29.916  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7130.152    ± 918.562  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3079.289    ± 344.335  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4427.061     ± 63.820  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     448914.484  ± 11857.115  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     456273.780   ± 6105.998  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     467406.041  ± 13156.857  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     469970.704   ± 8897.233  ops/s
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

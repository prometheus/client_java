# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-19T04:26:48Z
- **Commit:** [`9776bc9`](https://github.com/prometheus/client_java/commit/9776bc9ce102e5eff974b337fd6c44d97be0b8dd)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.70K | ± 1.17K | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.73K | ± 984.19 | ops/s | 1.2x slower |
| prometheusAdd | 51.52K | ± 197.90 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.17K | ± 1.72K | ops/s | 1.3x slower |
| simpleclientInc | 6.77K | ± 39.27 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.47K | ± 203.82 | ops/s | 10x slower |
| simpleclientAdd | 6.14K | ± 62.98 | ops/s | 11x slower |
| openTelemetryAdd | 1.32K | ± 29.84 | ops/s | 49x slower |
| openTelemetryInc | 1.25K | ± 18.13 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.24K | ± 24.41 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.62K | ± 1.36K | ops/s | **fastest** |
| simpleclient | 4.56K | ± 25.36 | ops/s | 1.2x slower |
| prometheusNative | 2.73K | ± 223.48 | ops/s | 2.1x slower |
| openTelemetryClassic | 675.46 | ± 14.41 | ops/s | 8.3x slower |
| openTelemetryExponential | 576.23 | ± 36.06 | ops/s | 9.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.95K | ± 4.41K | ops/s | **fastest** |
| prometheusWriteToByteArray | 485.51K | ± 3.07K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 476.51K | ± 2.84K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.24K | ± 11.74K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49173.682   ± 1715.938  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1318.546     ± 29.837  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1247.980     ± 18.135  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1239.201     ± 24.407  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51521.560    ± 197.904  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64699.683   ± 1167.147  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55725.807    ± 984.191  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6142.688     ± 62.982  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6767.274     ± 39.273  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6469.432    ± 203.820  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        675.456     ± 14.407  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        576.225     ± 36.060  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5620.755   ± 1359.480  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2726.112    ± 223.484  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4564.602     ± 25.362  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473241.907  ± 11738.546  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     476514.718   ± 2844.658  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485514.035   ± 3069.562  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492954.086   ± 4408.329  ops/s
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

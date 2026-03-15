# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-15T04:30:07Z
- **Commit:** [`b81332e`](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 57.09K | ± 2.97K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.47K | ± 681.60 | ops/s | 1.1x slower |
| prometheusAdd | 47.90K | ± 963.68 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 42.68K | ± 2.07K | ops/s | 1.3x slower |
| simpleclientInc | 6.33K | ± 124.99 | ops/s | 9.0x slower |
| simpleclientAdd | 6.01K | ± 286.71 | ops/s | 9.5x slower |
| simpleclientNoLabelsInc | 5.89K | ± 54.63 | ops/s | 9.7x slower |
| openTelemetryAdd | 1.36K | ± 75.96 | ops/s | 42x slower |
| openTelemetryIncNoLabels | 1.35K | ± 62.30 | ops/s | 42x slower |
| openTelemetryInc | 1.33K | ± 91.05 | ops/s | 43x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.57K | ± 2.62K | ops/s | **fastest** |
| simpleclient | 4.33K | ± 76.71 | ops/s | 1.5x slower |
| prometheusNative | 3.02K | ± 221.05 | ops/s | 2.2x slower |
| openTelemetryClassic | 636.44 | ± 6.56 | ops/s | 10x slower |
| openTelemetryExponential | 516.32 | ± 9.45 | ops/s | 13x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 557.84K | ± 2.35K | ops/s | **fastest** |
| prometheusWriteToByteArray | 545.52K | ± 2.47K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 536.09K | ± 5.24K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 526.23K | ± 6.02K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42677.758   ± 2071.123  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1361.203     ± 75.956  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1333.889     ± 91.053  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1347.834     ± 62.304  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47904.752    ± 963.685  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      57090.063   ± 2971.423  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51469.642    ± 681.602  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6011.520    ± 286.711  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6325.970    ± 124.985  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5887.317     ± 54.628  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        636.444      ± 6.562  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        516.323      ± 9.453  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6567.018   ± 2624.765  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3018.796    ± 221.055  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4334.181     ± 76.715  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     526228.490   ± 6024.924  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     536090.821   ± 5243.900  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     545517.424   ± 2473.975  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     557837.270   ± 2345.819  ops/s
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

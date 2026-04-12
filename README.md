# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-12T04:31:59Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.66K | ± 1.69K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.83K | ± 452.23 | ops/s | 1.2x slower |
| prometheusAdd | 51.37K | ± 348.90 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.07K | ± 1.75K | ops/s | 1.3x slower |
| simpleclientInc | 6.55K | ± 164.70 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.40K | ± 152.41 | ops/s | 10x slower |
| simpleclientAdd | 6.24K | ± 234.98 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 1.34K | ± 139.59 | ops/s | 49x slower |
| openTelemetryInc | 1.25K | ± 83.19 | ops/s | 52x slower |
| openTelemetryAdd | 1.22K | ± 14.67 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.50K | ± 1.55K | ops/s | **fastest** |
| simpleclient | 4.44K | ± 60.37 | ops/s | 1.2x slower |
| prometheusNative | 2.81K | ± 358.36 | ops/s | 2.0x slower |
| openTelemetryClassic | 684.03 | ± 4.53 | ops/s | 8.0x slower |
| openTelemetryExponential | 562.07 | ± 16.71 | ops/s | 9.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.50K | ± 1.44K | ops/s | **fastest** |
| prometheusWriteToByteArray | 490.35K | ± 1.63K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 483.71K | ± 6.84K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 477.02K | ± 5.37K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49072.304   ± 1745.565  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1223.746     ± 14.665  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1254.487     ± 83.186  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1338.772    ± 139.589  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51367.930    ± 348.898  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65657.831   ± 1692.948  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56833.148    ± 452.232  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6241.211    ± 234.984  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6549.374    ± 164.696  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6398.703    ± 152.409  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        684.025      ± 4.534  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        562.067     ± 16.710  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5498.719   ± 1554.191  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2814.764    ± 358.357  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4440.457     ± 60.369  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477019.102   ± 5367.005  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     483712.931   ± 6844.339  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     490351.237   ± 1632.717  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492501.460   ± 1443.180  ops/s
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

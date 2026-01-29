# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-01-29T04:21:08Z
- **Commit:** [`8c1cf17`](https://github.com/prometheus/client_java/commit/8c1cf1747c382cf80c40e88b7114125976ebd9c4)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.42K | ± 3.92K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.84K | ± 630.84 | ops/s | 1.1x slower |
| prometheusAdd | 51.71K | ± 164.80 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.68K | ± 1.88K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.63K | ± 115.69 | ops/s | 9.6x slower |
| simpleclientInc | 6.60K | ± 162.88 | ops/s | 9.6x slower |
| simpleclientAdd | 6.42K | ± 154.17 | ops/s | 9.9x slower |
| openTelemetryInc | 1.44K | ± 233.13 | ops/s | 44x slower |
| openTelemetryAdd | 1.41K | ± 302.23 | ops/s | 45x slower |
| openTelemetryIncNoLabels | 1.18K | ± 24.84 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.56K | ± 251.90 | ops/s | **fastest** |
| simpleclient | 4.55K | ± 30.17 | ops/s | 1.2x slower |
| prometheusNative | 2.96K | ± 160.11 | ops/s | 1.9x slower |
| openTelemetryClassic | 691.76 | ± 24.42 | ops/s | 8.0x slower |
| openTelemetryExponential | 545.44 | ± 14.45 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 503.30K | ± 1.92K | ops/s | **fastest** |
| prometheusWriteToByteArray | 496.67K | ± 4.24K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.36K | ± 4.34K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 485.16K | ± 3.07K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49679.249   ± 1875.605  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1406.274    ± 302.233  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1438.080    ± 233.125  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1179.698     ± 24.844  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51708.496    ± 164.798  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63422.034   ± 3920.134  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56835.135    ± 630.840  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6421.742    ± 154.171  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6602.414    ± 162.884  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6633.966    ± 115.690  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        691.755     ± 24.418  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        545.441     ± 14.451  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5559.181    ± 251.899  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2964.178    ± 160.111  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4552.216     ± 30.165  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     485162.589   ± 3072.145  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485363.144   ± 4343.521  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496670.140   ± 4239.780  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     503296.592   ± 1924.672  ops/s
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

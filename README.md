# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-09T04:14:17Z
- **Commit:** [`565a583`](https://github.com/prometheus/client_java/commit/565a58396c92ddfbe1b64de37c40a0a8c165a612)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.93K | ± 1.32K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.83K | ± 268.23 | ops/s | 1.2x slower |
| prometheusAdd | 51.29K | ± 1.06K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.18K | ± 1.44K | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.60K | ± 38.18 | ops/s | 3.5x slower |
| openTelemetryInc | 15.32K | ± 190.95 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.87K | ± 212.60 | ops/s | 5.1x slower |
| simpleclientAdd | 6.46K | ± 33.06 | ops/s | 10x slower |
| simpleclientInc | 6.39K | ± 77.03 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.32K | ± 81.92 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.28K | ± 24.58 | ops/s | **fastest** |
| prometheusClassic | 5.56K | ± 1.42K | ops/s | 2.2x slower |
| prometheusClassicSingleThread | 4.58K | ± 21.92 | ops/s | 2.7x slower |
| simpleclient | 4.41K | ± 67.05 | ops/s | 2.8x slower |
| prometheusNative | 3.19K | ± 54.93 | ops/s | 3.8x slower |
| openTelemetryExponential | 823.92 | ± 198.81 | ops/s | 15x slower |
| openTelemetryClassic | 812.22 | ± 58.05 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 24.29K | ± 914.82 | ops/s | **fastest** |
| prometheusWriteToNull | 23.66K | ± 298.45 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 516.72K | ± 1.42K | ops/s | **fastest** |
| prometheusWriteToByteArray | 514.32K | ± 5.57K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 493.36K | ± 2.88K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 492.05K | ± 1.28K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49181.906   ± 1441.883  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12871.526    ± 212.604  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15322.548    ± 190.947  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18604.324     ± 38.184  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51289.168   ± 1061.385  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65930.117   ± 1324.952  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56832.296    ± 268.232  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6456.588     ± 33.061  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6389.670     ± 77.033  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6319.699     ± 81.918  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        812.219     ± 58.045  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        823.924    ± 198.805  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5557.928   ± 1422.533  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12282.210     ± 24.584  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4579.784     ± 21.919  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3192.696     ± 54.929  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4410.984     ± 67.050  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24288.244    ± 914.818  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23662.365    ± 298.451  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     492046.388   ± 1284.789  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     493361.619   ± 2879.196  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     514324.761   ± 5571.974  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     516717.694   ± 1423.950  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

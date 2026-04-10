# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-10T04:32:08Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.36K | ± 1.41K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.74K | ± 303.88 | ops/s | 1.2x slower |
| prometheusAdd | 51.01K | ± 382.06 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.07K | ± 1.63K | ops/s | 1.3x slower |
| simpleclientInc | 6.66K | ± 63.83 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.51K | ± 180.90 | ops/s | 10x slower |
| simpleclientAdd | 6.47K | ± 32.64 | ops/s | 10x slower |
| openTelemetryAdd | 1.43K | ± 226.86 | ops/s | 46x slower |
| openTelemetryInc | 1.24K | ± 16.98 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.19K | ± 62.73 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.43K | ± 1.32K | ops/s | **fastest** |
| simpleclient | 4.48K | ± 38.53 | ops/s | 1.2x slower |
| prometheusNative | 2.79K | ± 340.34 | ops/s | 1.9x slower |
| openTelemetryClassic | 699.23 | ± 10.47 | ops/s | 7.8x slower |
| openTelemetryExponential | 554.87 | ± 28.36 | ops/s | 9.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 500.41K | ± 3.13K | ops/s | **fastest** |
| prometheusWriteToByteArray | 497.82K | ± 2.12K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 492.31K | ± 4.01K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.39K | ± 9.24K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49069.941   ± 1630.822  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1427.493    ± 226.862  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1240.717     ± 16.984  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1193.672     ± 62.727  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51009.891    ± 382.057  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65361.224   ± 1414.262  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56736.359    ± 303.880  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6472.482     ± 32.638  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6656.302     ± 63.828  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6508.440    ± 180.901  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        699.227     ± 10.467  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        554.872     ± 28.358  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5426.876   ± 1323.690  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2791.659    ± 340.345  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4482.365     ± 38.531  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483386.013   ± 9238.169  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     492310.741   ± 4011.683  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     497823.580   ± 2123.513  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     500408.939   ± 3129.204  ops/s
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

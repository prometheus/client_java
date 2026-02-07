# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-07T04:19:53Z
- **Commit:** [`5cfa5c0`](https://github.com/prometheus/client_java/commit/5cfa5c08cf169dc5854b16d5fb457e37dc7885a3)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.84K | ± 1.71K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.00K | ± 337.13 | ops/s | 1.2x slower |
| prometheusAdd | 51.61K | ± 176.78 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.40K | ± 1.56K | ops/s | 1.3x slower |
| simpleclientInc | 6.77K | ± 27.68 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.58K | ± 126.82 | ops/s | 10x slower |
| simpleclientAdd | 6.39K | ± 234.36 | ops/s | 10x slower |
| openTelemetryInc | 1.36K | ± 189.12 | ops/s | 49x slower |
| openTelemetryAdd | 1.28K | ± 59.20 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.23K | ± 29.85 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.63K | ± 2.30K | ops/s | **fastest** |
| simpleclient | 4.52K | ± 60.31 | ops/s | 1.5x slower |
| prometheusNative | 3.04K | ± 422.19 | ops/s | 2.2x slower |
| openTelemetryClassic | 678.06 | ± 24.93 | ops/s | 9.8x slower |
| openTelemetryExponential | 536.36 | ± 12.96 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 493.06K | ± 4.86K | ops/s | **fastest** |
| prometheusWriteToByteArray | 486.36K | ± 3.53K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 478.26K | ± 9.01K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 472.32K | ± 6.39K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49397.413   ± 1564.983  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1276.958     ± 59.201  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1356.248    ± 189.125  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1225.348     ± 29.848  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51607.735    ± 176.781  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65836.809   ± 1708.595  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56996.697    ± 337.132  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6394.016    ± 234.363  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6772.149     ± 27.681  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6583.203    ± 126.816  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        678.059     ± 24.934  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        536.363     ± 12.964  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6626.462   ± 2303.981  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3035.003    ± 422.187  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4519.020     ± 60.311  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     472317.640   ± 6386.775  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     478261.090   ± 9006.173  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     486360.839   ± 3530.631  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     493062.947   ± 4857.091  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-12T04:28:17Z
- **Commit:** [`09bbeee`](https://github.com/prometheus/client_java/commit/09bbeee1225edb7d7e4acb6c4525c9c53fb2e613)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.34K | ± 1.48K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.50K | ± 1.41K | ops/s | 1.2x slower |
| prometheusAdd | 51.62K | ± 229.64 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.59K | ± 431.00 | ops/s | 1.3x slower |
| simpleclientInc | 6.77K | ± 38.31 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.70K | ± 11.01 | ops/s | 9.8x slower |
| simpleclientAdd | 6.32K | ± 109.83 | ops/s | 10x slower |
| openTelemetryAdd | 1.60K | ± 246.93 | ops/s | 41x slower |
| openTelemetryInc | 1.39K | ± 178.13 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.21K | ± 14.62 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.25K | ± 1.51K | ops/s | **fastest** |
| simpleclient | 4.56K | ± 18.41 | ops/s | 1.6x slower |
| prometheusNative | 3.01K | ± 243.60 | ops/s | 2.4x slower |
| openTelemetryClassic | 673.81 | ± 22.18 | ops/s | 11x slower |
| openTelemetryExponential | 546.74 | ± 20.14 | ops/s | 13x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 488.62K | ± 3.14K | ops/s | **fastest** |
| prometheusWriteToNull | 487.58K | ± 4.18K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 481.45K | ± 4.93K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 481.41K | ± 4.82K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50587.599    ± 431.002  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1604.976    ± 246.934  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1393.740    ± 178.129  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1212.981     ± 14.615  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51622.764    ± 229.640  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65336.154   ± 1483.132  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56503.197   ± 1406.049  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6317.994    ± 109.832  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6766.264     ± 38.308  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6697.282     ± 11.011  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        673.812     ± 22.183  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        546.737     ± 20.138  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7245.788   ± 1507.229  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3007.422    ± 243.604  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4558.133     ± 18.407  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481445.593   ± 4934.802  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481409.060   ± 4820.585  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     488619.781   ± 3141.954  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     487576.914   ± 4181.913  ops/s
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

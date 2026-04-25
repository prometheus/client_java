# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-25T04:29:32Z
- **Commit:** [`e898720`](https://github.com/prometheus/client_java/commit/e898720958021b1e81753f3cce45aa9ce5bfdca0)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.82K | ± 1.11K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.83K | ± 304.39 | ops/s | 1.1x slower |
| prometheusAdd | 51.06K | ± 344.33 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.39K | ± 935.46 | ops/s | 1.3x slower |
| simpleclientInc | 6.52K | ± 190.19 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.49K | ± 187.65 | ops/s | 10.0x slower |
| simpleclientAdd | 6.20K | ± 232.80 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 4.03K | ± 385.06 | ops/s | 16x slower |
| openTelemetryInc | 3.39K | ± 533.94 | ops/s | 19x slower |
| openTelemetryAdd | 3.22K | ± 165.60 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.79K | ± 956.79 | ops/s | **fastest** |
| simpleclient | 4.44K | ± 35.65 | ops/s | 1.5x slower |
| prometheusNative | 2.90K | ± 251.15 | ops/s | 2.3x slower |
| openTelemetryClassic | 770.81 | ± 23.29 | ops/s | 8.8x slower |
| openTelemetryExponential | 607.84 | ± 24.44 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 502.78K | ± 1.97K | ops/s | **fastest** |
| prometheusWriteToByteArray | 497.73K | ± 4.01K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 494.59K | ± 2.56K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 489.58K | ± 3.22K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48386.894    ± 935.459  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3215.942    ± 165.603  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3391.369    ± 533.936  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4032.733    ± 385.062  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51062.539    ± 344.330  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64816.918   ± 1113.737  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56830.638    ± 304.394  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6204.136    ± 232.796  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6519.662    ± 190.186  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6487.179    ± 187.647  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        770.811     ± 23.294  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        607.842     ± 24.437  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6791.566    ± 956.786  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2895.499    ± 251.152  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4439.147     ± 35.649  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     489582.899   ± 3218.916  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     494585.808   ± 2562.384  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     497725.212   ± 4011.724  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502778.441   ± 1973.374  ops/s
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

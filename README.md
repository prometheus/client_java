# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-06T04:34:54Z
- **Commit:** [`b5137b2`](https://github.com/prometheus/client_java/commit/b5137b283a03b11f05a6979f4480593bda44b1b4)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.16K | ± 335.35 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.25K | ± 129.94 | ops/s | 1.2x slower |
| prometheusAdd | 51.61K | ± 220.91 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 46.81K | ± 650.75 | ops/s | 1.4x slower |
| simpleclientInc | 6.66K | ± 58.47 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.61K | ± 9.83 | ops/s | 10x slower |
| simpleclientAdd | 6.47K | ± 20.10 | ops/s | 10x slower |
| openTelemetryAdd | 3.35K | ± 340.61 | ops/s | 20x slower |
| openTelemetryInc | 3.08K | ± 295.21 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 3.07K | ± 493.70 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.39K | ± 1.37K | ops/s | **fastest** |
| simpleclient | 4.50K | ± 14.07 | ops/s | 1.2x slower |
| prometheusNative | 2.79K | ± 322.92 | ops/s | 1.9x slower |
| openTelemetryClassic | 764.65 | ± 17.51 | ops/s | 7.1x slower |
| openTelemetryExponential | 565.59 | ± 14.01 | ops/s | 9.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 488.63K | ± 9.28K | ops/s | **fastest** |
| prometheusWriteToByteArray | 485.64K | ± 4.66K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.10K | ± 7.81K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 476.51K | ± 11.16K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      46806.701    ± 650.746  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3352.932    ± 340.609  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3082.500    ± 295.214  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3074.054    ± 493.698  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51613.247    ± 220.905  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66156.370    ± 335.354  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57246.557    ± 129.938  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6473.437     ± 20.099  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6664.618     ± 58.468  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6609.423      ± 9.832  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        764.647     ± 17.512  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        565.594     ± 14.011  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5394.536   ± 1368.428  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2788.871    ± 322.920  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4504.008     ± 14.067  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     476514.255  ± 11164.179  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482095.528   ± 7813.758  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485642.127   ± 4663.272  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     488629.914   ± 9278.262  ops/s
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

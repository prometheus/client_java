# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-10T04:29:11Z
- **Commit:** [`04bc727`](https://github.com/prometheus/client_java/commit/04bc727fcb2b9ba4da8eb7268c562f5385f5eda4)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.65K | ± 1.76K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.32K | ± 233.74 | ops/s | 1.1x slower |
| prometheusAdd | 51.62K | ± 227.13 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.06K | ± 1.32K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.63K | ± 107.85 | ops/s | 9.9x slower |
| simpleclientInc | 6.59K | ± 171.41 | ops/s | 10.0x slower |
| simpleclientAdd | 6.24K | ± 174.36 | ops/s | 11x slower |
| openTelemetryAdd | 1.29K | ± 72.72 | ops/s | 51x slower |
| openTelemetryInc | 1.27K | ± 7.51 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.24K | ± 64.39 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.00K | ± 820.95 | ops/s | **fastest** |
| simpleclient | 4.52K | ± 59.59 | ops/s | 1.1x slower |
| prometheusNative | 2.86K | ± 359.02 | ops/s | 1.7x slower |
| openTelemetryClassic | 697.47 | ± 11.15 | ops/s | 7.2x slower |
| openTelemetryExponential | 572.81 | ± 14.59 | ops/s | 8.7x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 485.98K | ± 3.05K | ops/s | **fastest** |
| prometheusWriteToByteArray | 484.23K | ± 2.13K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 481.85K | ± 4.61K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.37K | ± 2.12K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49062.091   ± 1316.270  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1290.242     ± 72.720  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1272.407      ± 7.514  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1242.584     ± 64.391  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51615.799    ± 227.126  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65651.082   ± 1761.119  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57323.662    ± 233.740  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6239.539    ± 174.356  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6587.544    ± 171.412  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6626.365    ± 107.853  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        697.466     ± 11.147  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        572.815     ± 14.593  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4997.250    ± 820.951  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2858.277    ± 359.017  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4523.223     ± 59.591  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475367.063   ± 2124.044  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481848.744   ± 4613.020  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     484229.675   ± 2129.522  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     485978.071   ± 3052.404  ops/s
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

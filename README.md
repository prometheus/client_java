# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-30T04:34:37Z
- **Commit:** [`1d99672`](https://github.com/prometheus/client_java/commit/1d996722d26b910992bc9f8f477f9af8f811096d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 67.21K | ± 2.00K | ops/s | **fastest** |
| prometheusNoLabelsInc | 58.14K | ± 643.23 | ops/s | 1.2x slower |
| prometheusAdd | 52.24K | ± 607.23 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.69K | ± 1.50K | ops/s | 1.4x slower |
| simpleclientInc | 6.54K | ± 159.99 | ops/s | 10x slower |
| simpleclientAdd | 6.23K | ± 323.85 | ops/s | 11x slower |
| simpleclientNoLabelsInc | 6.23K | ± 68.21 | ops/s | 11x slower |
| openTelemetryAdd | 3.44K | ± 451.34 | ops/s | 20x slower |
| openTelemetryInc | 3.39K | ± 341.73 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 2.96K | ± 230.06 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.30K | ± 756.82 | ops/s | **fastest** |
| simpleclient | 4.53K | ± 87.08 | ops/s | 1.2x slower |
| prometheusNative | 2.77K | ± 273.77 | ops/s | 1.9x slower |
| openTelemetryClassic | 797.52 | ± 30.81 | ops/s | 6.6x slower |
| openTelemetryExponential | 742.00 | ± 104.20 | ops/s | 7.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 500.44K | ± 6.73K | ops/s | **fastest** |
| prometheusWriteToByteArray | 498.38K | ± 4.03K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 491.75K | ± 8.40K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 491.59K | ± 3.54K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48694.977   ± 1504.091  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3437.395    ± 451.345  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3393.623    ± 341.728  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2955.114    ± 230.062  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      52241.807    ± 607.230  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67206.577   ± 2002.905  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      58142.691    ± 643.226  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6230.707    ± 323.852  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6542.418    ± 159.988  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6230.541     ± 68.206  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        797.516     ± 30.806  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        741.999    ± 104.201  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5301.023    ± 756.823  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2774.781    ± 273.765  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4534.430     ± 87.076  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     491752.779   ± 8395.948  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     491586.836   ± 3540.919  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     498381.923   ± 4030.146  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     500437.632   ± 6729.489  ops/s
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

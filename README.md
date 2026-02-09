# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-09T04:28:56Z
- **Commit:** [`4129f40`](https://github.com/prometheus/client_java/commit/4129f408ec471e5e531e37c8fb6048ef2f9134af)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.31K | ± 1.35K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.22K | ± 1.53K | ops/s | 1.2x slower |
| prometheusAdd | 51.50K | ± 250.76 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.74K | ± 928.14 | ops/s | 1.3x slower |
| simpleclientInc | 6.80K | ± 9.10 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.70K | ± 13.85 | ops/s | 9.8x slower |
| simpleclientAdd | 6.27K | ± 179.43 | ops/s | 10x slower |
| openTelemetryInc | 1.40K | ± 164.42 | ops/s | 47x slower |
| openTelemetryAdd | 1.39K | ± 232.64 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.32K | ± 158.06 | ops/s | 49x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.06K | ± 1.65K | ops/s | **fastest** |
| simpleclient | 4.50K | ± 54.41 | ops/s | 1.3x slower |
| prometheusNative | 2.95K | ± 342.87 | ops/s | 2.1x slower |
| openTelemetryClassic | 676.78 | ± 9.59 | ops/s | 9.0x slower |
| openTelemetryExponential | 551.47 | ± 34.05 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 437.73K | ± 4.48K | ops/s | **fastest** |
| prometheusWriteToNull | 436.57K | ± 3.79K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 426.95K | ± 2.56K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 419.96K | ± 8.87K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49744.823    ± 928.142  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1386.350    ± 232.644  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1397.156    ± 164.417  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1324.791    ± 158.057  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51503.420    ± 250.765  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65305.341   ± 1350.504  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56221.436   ± 1527.068  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6273.205    ± 179.432  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6796.387      ± 9.096  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6697.272     ± 13.851  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        676.783      ± 9.590  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        551.471     ± 34.047  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6058.480   ± 1648.381  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2949.542    ± 342.868  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4498.016     ± 54.413  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     419956.703   ± 8865.290  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     426948.837   ± 2561.740  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     437732.540   ± 4478.506  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     436573.755   ± 3792.921  ops/s
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

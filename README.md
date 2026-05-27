# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-27T04:39:25Z
- **Commit:** [`ba6b0b5`](https://github.com/prometheus/client_java/commit/ba6b0b5a95b98ad40d2b513f3e446fdfbf94d0ab)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 57.62K | ± 1.97K | ops/s | **fastest** |
| prometheusNoLabelsInc | 52.20K | ± 501.78 | ops/s | 1.1x slower |
| prometheusAdd | 48.72K | ± 930.52 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.31K | ± 491.40 | ops/s | 1.3x slower |
| simpleclientInc | 6.12K | ± 84.85 | ops/s | 9.4x slower |
| simpleclientAdd | 5.95K | ± 267.42 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 5.93K | ± 23.82 | ops/s | 9.7x slower |
| openTelemetryInc | 5.27K | ± 1.02K | ops/s | 11x slower |
| openTelemetryIncNoLabels | 5.15K | ± 1.27K | ops/s | 11x slower |
| openTelemetryAdd | 3.72K | ± 953.55 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.00K | ± 1.65K | ops/s | **fastest** |
| simpleclient | 4.39K | ± 78.01 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 137.12 | ops/s | 1.6x slower |
| openTelemetryClassic | 720.23 | ± 26.77 | ops/s | 6.9x slower |
| openTelemetryExponential | 532.71 | ± 12.64 | ops/s | 9.4x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.70K | ± 198.73 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.37K | ± 231.04 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 587.16K | ± 5.10K | ops/s | **fastest** |
| prometheusWriteToByteArray | 554.96K | ± 21.08K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 542.50K | ± 7.03K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 531.11K | ± 14.36K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44309.766    ± 491.405  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3719.754    ± 953.553  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5266.921   ± 1018.912  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5149.323   ± 1269.633  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48715.124    ± 930.516  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      57621.135   ± 1973.278  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      52196.096    ± 501.781  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5952.975    ± 267.421  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6122.576     ± 84.846  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5927.394     ± 23.825  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        720.226     ± 26.773  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        532.707     ± 12.638  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5004.068   ± 1645.264  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3063.802    ± 137.119  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4391.203     ± 78.010  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27371.091    ± 231.037  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27695.473    ± 198.731  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     531109.982  ± 14361.173  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     542504.274   ± 7028.789  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     554955.035  ± 21077.990  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     587162.945   ± 5100.598  ops/s
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

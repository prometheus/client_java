# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-18T04:39:44Z
- **Commit:** [`94b33b7`](https://github.com/prometheus/client_java/commit/94b33b7527ce21b12ff2a3f9cd23c63cdb42e274)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.45K | ± 1.75K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.75K | ± 309.15 | ops/s | 1.2x slower |
| prometheusAdd | 51.16K | ± 540.21 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.47K | ± 2.01K | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 36.19 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.46K | ± 125.09 | ops/s | 10x slower |
| simpleclientAdd | 6.19K | ± 239.97 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 3.85K | ± 618.95 | ops/s | 17x slower |
| openTelemetryAdd | 3.19K | ± 287.71 | ops/s | 21x slower |
| openTelemetryInc | 3.18K | ± 401.43 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.47K | ± 77.97 | ops/s | **fastest** |
| prometheusClassic | 4.46K | ± 361.99 | ops/s | 1.0x slower |
| prometheusNative | 2.71K | ± 355.02 | ops/s | 1.7x slower |
| openTelemetryClassic | 760.52 | ± 35.27 | ops/s | 5.9x slower |
| openTelemetryExponential | 616.51 | ± 54.58 | ops/s | 7.3x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.19K | ± 179.71 | ops/s | **fastest** |
| prometheusWriteToNull | 22.90K | ± 632.35 | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 505.67K | ± 3.98K | ops/s | **fastest** |
| prometheusWriteToByteArray | 499.95K | ± 6.92K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 486.89K | ± 4.22K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 476.93K | ± 3.06K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49466.099   ± 2013.823  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3185.164    ± 287.713  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3175.342    ± 401.429  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3846.744    ± 618.955  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51158.773    ± 540.214  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65452.064   ± 1748.685  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56754.283    ± 309.154  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6185.504    ± 239.974  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6556.847     ± 36.187  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6458.814    ± 125.094  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        760.521     ± 35.271  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        616.511     ± 54.582  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4460.126    ± 361.993  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2705.802    ± 355.021  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4470.841     ± 77.971  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24187.430    ± 179.714  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22896.722    ± 632.350  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     476926.259   ± 3061.675  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486894.455   ± 4218.081  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     499952.543   ± 6922.482  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     505668.499   ± 3975.867  ops/s
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

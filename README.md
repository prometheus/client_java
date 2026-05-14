# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-14T04:35:59Z
- **Commit:** [`94b33b7`](https://github.com/prometheus/client_java/commit/94b33b7527ce21b12ff2a3f9cd23c63cdb42e274)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.82K | ± 109.83 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.10K | ± 529.37 | ops/s | 1.2x slower |
| prometheusAdd | 48.54K | ± 49.85 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.10K | ± 177.65 | ops/s | 1.4x slower |
| simpleclientAdd | 6.12K | ± 55.91 | ops/s | 9.8x slower |
| simpleclientInc | 6.09K | ± 7.87 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.04K | ± 187.40 | ops/s | 9.9x slower |
| openTelemetryInc | 4.99K | ± 918.79 | ops/s | 12x slower |
| openTelemetryIncNoLabels | 4.51K | ± 416.72 | ops/s | 13x slower |
| openTelemetryAdd | 4.10K | ± 927.63 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.27K | ± 2.55K | ops/s | **fastest** |
| simpleclient | 4.16K | ± 36.02 | ops/s | 1.5x slower |
| prometheusNative | 3.00K | ± 226.19 | ops/s | 2.1x slower |
| openTelemetryClassic | 733.36 | ± 12.89 | ops/s | 8.5x slower |
| openTelemetryExponential | 559.25 | ± 14.84 | ops/s | 11x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 27.33K | ± 437.15 | ops/s | **fastest** |
| prometheusWriteToNull | 27.24K | ± 194.64 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 576.40K | ± 15.01K | ops/s | **fastest** |
| prometheusWriteToByteArray | 573.18K | ± 4.70K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 546.99K | ± 3.64K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 533.75K | ± 2.85K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44096.385    ± 177.651  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4097.153    ± 927.626  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4987.067    ± 918.791  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4506.077    ± 416.716  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48543.441     ± 49.854  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59815.224    ± 109.827  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51102.829    ± 529.370  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6120.289     ± 55.912  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6092.674      ± 7.872  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6041.464    ± 187.404  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        733.361     ± 12.891  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        559.249     ± 14.841  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6265.755   ± 2546.705  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3004.501    ± 226.192  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4163.655     ± 36.023  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27333.471    ± 437.149  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27237.612    ± 194.637  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     533750.432   ± 2846.021  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     546985.948   ± 3635.545  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     573178.216   ± 4695.844  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     576400.724  ± 15006.787  ops/s
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

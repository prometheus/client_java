# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-23T04:35:21Z
- **Commit:** [`5ee188f`](https://github.com/prometheus/client_java/commit/5ee188ff288806f76e53a89d32431a93bb53da11)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.02K | ± 1.13K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.85K | ± 822.26 | ops/s | 1.2x slower |
| prometheusAdd | 48.52K | ± 278.91 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.20K | ± 529.17 | ops/s | 1.4x slower |
| simpleclientInc | 6.20K | ± 4.50 | ops/s | 9.7x slower |
| simpleclientAdd | 6.06K | ± 32.94 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.02K | ± 221.02 | ops/s | 10.0x slower |
| openTelemetryInc | 5.29K | ± 1.20K | ops/s | 11x slower |
| openTelemetryAdd | 5.10K | ± 50.02 | ops/s | 12x slower |
| openTelemetryIncNoLabels | 4.37K | ± 531.38 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.41K | ± 731.37 | ops/s | **fastest** |
| simpleclient | 4.29K | ± 101.93 | ops/s | 1.0x slower |
| prometheusNative | 2.92K | ± 266.43 | ops/s | 1.5x slower |
| openTelemetryClassic | 700.94 | ± 6.50 | ops/s | 6.3x slower |
| openTelemetryExponential | 550.09 | ± 18.31 | ops/s | 8.0x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.55K | ± 229.43 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.29K | ± 203.68 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 576.58K | ± 5.16K | ops/s | **fastest** |
| prometheusWriteToByteArray | 564.91K | ± 7.74K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 550.07K | ± 6.88K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 533.91K | ± 1.77K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44202.923    ± 529.174  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       5095.050     ± 50.023  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5293.970   ± 1201.066  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4367.094    ± 531.381  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48523.923    ± 278.906  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60023.505   ± 1133.439  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51851.869    ± 822.260  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6062.547     ± 32.945  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6204.361      ± 4.502  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6017.634    ± 221.023  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        700.942      ± 6.503  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        550.089     ± 18.305  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4406.461    ± 731.368  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2922.796    ± 266.429  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4294.657    ± 101.934  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27291.589    ± 203.683  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27547.457    ± 229.431  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     533910.920   ± 1767.242  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     550072.151   ± 6877.678  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     564914.913   ± 7739.330  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     576579.396   ± 5155.586  ops/s
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

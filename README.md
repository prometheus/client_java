# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-04T04:40:40Z
- **Commit:** [`574fb73`](https://github.com/prometheus/client_java/commit/574fb73e4d7eec6bbfd483378600579b966631a6)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.45K | ± 407.90 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.21K | ± 404.74 | ops/s | 1.2x slower |
| prometheusAdd | 48.95K | ± 747.21 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.32K | ± 851.20 | ops/s | 1.3x slower |
| simpleclientInc | 6.22K | ± 38.60 | ops/s | 9.6x slower |
| simpleclientAdd | 6.09K | ± 25.53 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 5.90K | ± 31.01 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.13K | ± 840.97 | ops/s | 12x slower |
| openTelemetryInc | 4.39K | ± 1.30K | ops/s | 14x slower |
| openTelemetryAdd | 3.88K | ± 867.58 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.36K | ± 404.73 | ops/s | **fastest** |
| simpleclient | 4.18K | ± 32.61 | ops/s | 1.0x slower |
| prometheusNative | 2.86K | ± 230.65 | ops/s | 1.5x slower |
| openTelemetryClassic | 691.62 | ± 8.37 | ops/s | 6.3x slower |
| openTelemetryExponential | 586.95 | ± 33.76 | ops/s | 7.4x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.50K | ± 274.06 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.35K | ± 156.03 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 580.82K | ± 5.47K | ops/s | **fastest** |
| prometheusWriteToByteArray | 570.74K | ± 3.90K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 551.78K | ± 2.46K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 531.76K | ± 4.73K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44316.918    ± 851.198  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3879.061    ± 867.578  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4386.908   ± 1301.519  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5126.892    ± 840.972  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48950.078    ± 747.212  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59445.693    ± 407.899  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51207.951    ± 404.736  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6094.712     ± 25.527  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6220.099     ± 38.602  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5903.303     ± 31.008  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        691.617      ± 8.370  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        586.951     ± 33.756  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4364.207    ± 404.733  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2855.076    ± 230.653  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4184.607     ± 32.614  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27346.903    ± 156.032  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27496.600    ± 274.063  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     531763.036   ± 4730.383  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     551784.149   ± 2455.327  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     570742.383   ± 3898.110  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     580818.709   ± 5470.980  ops/s
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

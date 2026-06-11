# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-11T04:37:21Z
- **Commit:** [`0b6a91f`](https://github.com/prometheus/client_java/commit/0b6a91f2bafe0fa15f6fe828f315103d8c20f9f9)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.45K | ± 799.62 | ops/s | **fastest** |
| prometheusNoLabelsInc | 50.81K | ± 2.77K | ops/s | 1.2x slower |
| prometheusAdd | 48.44K | ± 183.20 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 42.43K | ± 1.14K | ops/s | 1.4x slower |
| simpleclientInc | 6.23K | ± 50.69 | ops/s | 9.7x slower |
| openTelemetryIncNoLabels | 6.13K | ± 42.54 | ops/s | 9.9x slower |
| simpleclientAdd | 6.00K | ± 157.06 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 5.93K | ± 21.80 | ops/s | 10x slower |
| openTelemetryInc | 4.33K | ± 1.19K | ops/s | 14x slower |
| openTelemetryAdd | 3.77K | ± 916.72 | ops/s | 16x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.08K | ± 1.94K | ops/s | **fastest** |
| simpleclient | 4.39K | ± 86.68 | ops/s | 1.4x slower |
| prometheusNative | 2.72K | ± 128.10 | ops/s | 2.2x slower |
| openTelemetryClassic | 700.95 | ± 11.24 | ops/s | 8.7x slower |
| openTelemetryExponential | 527.58 | ± 4.88 | ops/s | 12x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 27.36K | ± 245.21 | ops/s | **fastest** |
| prometheusWriteToNull | 26.81K | ± 630.12 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 588.77K | ± 3.31K | ops/s | **fastest** |
| prometheusWriteToByteArray | 573.31K | ± 6.14K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 544.63K | ± 5.11K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 533.20K | ± 11.62K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42430.236   ± 1142.390  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3765.325    ± 916.721  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4327.548   ± 1189.484  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       6132.907     ± 42.538  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48443.305    ± 183.198  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60446.661    ± 799.621  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50812.893   ± 2774.639  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6001.414    ± 157.060  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6228.646     ± 50.693  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5929.777     ± 21.797  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        700.955     ± 11.243  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        527.585      ± 4.878  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6083.998   ± 1941.364  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2716.652    ± 128.103  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4390.329     ± 86.684  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27356.926    ± 245.214  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      26813.928    ± 630.121  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     533199.999  ± 11616.362  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     544631.101   ± 5111.915  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     573313.155   ± 6139.659  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     588770.917   ± 3306.354  ops/s
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

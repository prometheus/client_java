# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-08T04:29:39Z
- **Commit:** [`b0a5e13`](https://github.com/prometheus/client_java/commit/b0a5e13a7546290c74872a8aec62ac5040615e3e)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 77.20K | ± 1.73K | ops/s | **fastest** |
| prometheusAdd | 59.39K | ± 3.51K | ops/s | 1.3x slower |
| prometheusNoLabelsInc | 54.65K | ± 9.43K | ops/s | 1.4x slower |
| codahaleIncNoLabels | 54.47K | ± 1.91K | ops/s | 1.4x slower |
| simpleclientInc | 7.89K | ± 62.48 | ops/s | 9.8x slower |
| simpleclientAdd | 7.62K | ± 220.26 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 7.58K | ± 38.70 | ops/s | 10x slower |
| openTelemetryInc | 6.88K | ± 1.50K | ops/s | 11x slower |
| openTelemetryIncNoLabels | 6.33K | ± 2.00K | ops/s | 12x slower |
| openTelemetryAdd | 5.13K | ± 908.77 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.69K | ± 1.50K | ops/s | **fastest** |
| simpleclient | 5.67K | ± 77.22 | ops/s | 1.4x slower |
| prometheusNative | 3.79K | ± 232.43 | ops/s | 2.0x slower |
| openTelemetryClassic | 972.83 | ± 35.08 | ops/s | 7.9x slower |
| openTelemetryExponential | 737.55 | ± 21.40 | ops/s | 10x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 35.41K | ± 184.18 | ops/s | **fastest** |
| prometheusWriteToNull | 35.28K | ± 189.37 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 689.38K | ± 8.91K | ops/s | **fastest** |
| prometheusWriteToByteArray | 673.20K | ± 2.83K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 645.60K | ± 3.07K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 620.71K | ± 6.77K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      54465.375   ± 1908.601  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       5133.299    ± 908.775  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       6881.160   ± 1499.976  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       6333.826   ± 2004.234  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      59394.002   ± 3507.977  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      77197.584   ± 1732.981  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54653.412   ± 9427.664  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7622.171    ± 220.255  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7894.163     ± 62.483  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7583.050     ± 38.703  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        972.834     ± 35.079  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        737.551     ± 21.403  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7686.550   ± 1503.415  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3787.341    ± 232.427  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5672.338     ± 77.220  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35407.862    ± 184.177  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35278.952    ± 189.366  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     620707.748   ± 6767.275  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     645599.997   ± 3074.365  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     673195.387   ± 2825.224  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     689380.066   ± 8911.888  ops/s
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

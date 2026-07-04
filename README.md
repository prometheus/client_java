# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-04T04:30:57Z
- **Commit:** [`c29367d`](https://github.com/prometheus/client_java/commit/c29367daf4e6aec49eecf321b8e41553c2e194d5)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.00K | ± 376.03 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.82K | ± 399.61 | ops/s | 1.2x slower |
| prometheusAdd | 51.43K | ± 158.89 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 45.34K | ± 8.51K | ops/s | 1.5x slower |
| simpleclientInc | 6.54K | ± 41.72 | ops/s | 10x slower |
| simpleclientAdd | 6.47K | ± 27.62 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.35K | ± 21.72 | ops/s | 10x slower |
| openTelemetryAdd | 3.41K | ± 18.88 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.20K | ± 473.38 | ops/s | 21x slower |
| openTelemetryInc | 3.13K | ± 145.16 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.52K | ± 354.10 | ops/s | **fastest** |
| simpleclient | 4.37K | ± 40.03 | ops/s | 1.0x slower |
| prometheusNative | 2.92K | ± 206.04 | ops/s | 1.5x slower |
| openTelemetryClassic | 765.18 | ± 51.44 | ops/s | 5.9x slower |
| openTelemetryExponential | 586.55 | ± 12.49 | ops/s | 7.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.34K | ± 886.53 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.00K | ± 665.78 | ops/s | 1.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 500.88K | ± 5.85K | ops/s | **fastest** |
| prometheusWriteToByteArray | 500.22K | ± 8.86K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 489.38K | ± 1.27K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.06K | ± 5.11K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      45344.613   ± 8512.337  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3408.762     ± 18.880  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3132.918    ± 145.156  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3198.337    ± 473.383  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51430.395    ± 158.889  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65995.758    ± 376.028  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56816.691    ± 399.609  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6471.525     ± 27.621  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6543.324     ± 41.719  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6349.438     ± 21.720  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        765.177     ± 51.441  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        586.551     ± 12.492  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4520.472    ± 354.097  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2921.500    ± 206.045  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4366.831     ± 40.032  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23000.845    ± 665.777  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24339.930    ± 886.526  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479056.735   ± 5105.433  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489380.907   ± 1270.195  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     500219.987   ± 8863.506  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     500875.213   ± 5854.003  ops/s
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

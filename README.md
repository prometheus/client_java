# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-30T04:37:30Z
- **Commit:** [`2a2c73d`](https://github.com/prometheus/client_java/commit/2a2c73d7d23bfa291b10df85056027398e8a868d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.51K | ± 669.28 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.12K | ± 969.49 | ops/s | 1.2x slower |
| prometheusAdd | 51.40K | ± 93.76 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.69K | ± 1.31K | ops/s | 1.4x slower |
| simpleclientInc | 6.52K | ± 43.21 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.30K | ± 67.01 | ops/s | 11x slower |
| simpleclientAdd | 6.26K | ± 227.81 | ops/s | 11x slower |
| openTelemetryInc | 3.62K | ± 403.35 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.38K | ± 395.18 | ops/s | 20x slower |
| openTelemetryAdd | 3.19K | ± 365.46 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.44K | ± 1.51K | ops/s | **fastest** |
| simpleclient | 4.39K | ± 85.21 | ops/s | 1.2x slower |
| prometheusNative | 2.79K | ± 294.56 | ops/s | 2.0x slower |
| openTelemetryClassic | 743.02 | ± 23.46 | ops/s | 7.3x slower |
| openTelemetryExponential | 635.14 | ± 64.62 | ops/s | 8.6x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.22K | ± 1.11K | ops/s | **fastest** |
| prometheusWriteToNull | 23.57K | ± 717.30 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 504.28K | ± 7.35K | ops/s | **fastest** |
| prometheusWriteToByteArray | 503.13K | ± 3.65K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 484.41K | ± 3.06K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 482.20K | ± 3.66K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48692.528   ± 1307.385  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3186.500    ± 365.460  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3617.028    ± 403.352  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3380.847    ± 395.184  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51403.266     ± 93.764  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66506.979    ± 669.283  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56119.424    ± 969.488  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6258.869    ± 227.811  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6521.990     ± 43.214  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6301.706     ± 67.012  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        743.024     ± 23.459  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        635.138     ± 64.622  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5440.337   ± 1509.703  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2786.972    ± 294.559  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4388.720     ± 85.213  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24218.085   ± 1113.619  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23569.252    ± 717.295  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     482199.801   ± 3656.208  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     484409.275   ± 3062.051  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     503126.578   ± 3652.428  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     504282.388   ± 7350.518  ops/s
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

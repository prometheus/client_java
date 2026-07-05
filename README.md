# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-05T04:34:06Z
- **Commit:** [`c29367d`](https://github.com/prometheus/client_java/commit/c29367daf4e6aec49eecf321b8e41553c2e194d5)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.99K | ± 1.08K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.83K | ± 384.83 | ops/s | 1.2x slower |
| prometheusAdd | 51.30K | ± 84.58 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 39.37K | ± 7.73K | ops/s | 1.7x slower |
| simpleclientInc | 6.59K | ± 8.84 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 8.63 | ops/s | 10x slower |
| simpleclientAdd | 6.31K | ± 212.19 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.50K | ± 830.46 | ops/s | 19x slower |
| openTelemetryInc | 3.27K | ± 258.88 | ops/s | 20x slower |
| openTelemetryAdd | 3.02K | ± 98.63 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.58K | ± 1.64K | ops/s | **fastest** |
| simpleclient | 4.43K | ± 57.39 | ops/s | 1.7x slower |
| prometheusNative | 2.56K | ± 77.92 | ops/s | 3.0x slower |
| openTelemetryClassic | 753.74 | ± 6.24 | ops/s | 10x slower |
| openTelemetryExponential | 601.83 | ± 76.59 | ops/s | 13x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.59K | ± 334.71 | ops/s | **fastest** |
| prometheusWriteToNull | 22.73K | ± 842.72 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 502.72K | ± 9.43K | ops/s | **fastest** |
| prometheusWriteToByteArray | 497.87K | ± 4.76K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.39K | ± 2.50K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 477.53K | ± 4.35K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      39365.332   ± 7728.957  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3022.831     ± 98.635  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3274.093    ± 258.877  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3499.752    ± 830.455  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51302.122     ± 84.579  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65993.205   ± 1082.884  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56825.675    ± 384.826  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6309.339    ± 212.187  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6590.154      ± 8.839  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6337.083      ± 8.628  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        753.736      ± 6.241  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        601.834     ± 76.591  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7581.855   ± 1640.046  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2560.744     ± 77.917  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4430.353     ± 57.391  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23585.117    ± 334.711  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22725.284    ± 842.724  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477528.884   ± 4354.712  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485388.854   ± 2503.276  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     497873.054   ± 4763.215  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502716.243   ± 9425.814  ops/s
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

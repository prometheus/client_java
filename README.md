# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-03T04:34:12Z
- **Commit:** [`188e434`](https://github.com/prometheus/client_java/commit/188e434f25be73f75a463239b5cb4d54a8f72cca)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.48K | ± 610.36 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.54K | ± 1.20K | ops/s | 1.2x slower |
| prometheusAdd | 51.40K | ± 229.03 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.53K | ± 1.57K | ops/s | 1.3x slower |
| simpleclientInc | 6.54K | ± 155.45 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 161.73 | ops/s | 10x slower |
| simpleclientAdd | 6.25K | ± 385.97 | ops/s | 11x slower |
| openTelemetryInc | 3.59K | ± 350.59 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.29K | ± 508.43 | ops/s | 20x slower |
| openTelemetryAdd | 2.93K | ± 91.90 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.30K | ± 1.72K | ops/s | **fastest** |
| simpleclient | 4.46K | ± 31.35 | ops/s | 1.2x slower |
| prometheusNative | 2.69K | ± 58.83 | ops/s | 2.0x slower |
| openTelemetryClassic | 738.24 | ± 74.19 | ops/s | 7.2x slower |
| openTelemetryExponential | 706.95 | ± 51.14 | ops/s | 7.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 482.18K | ± 4.16K | ops/s | **fastest** |
| prometheusWriteToByteArray | 472.08K | ± 3.65K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 469.07K | ± 3.35K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 463.52K | ± 4.79K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49534.643   ± 1570.908  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2928.457     ± 91.895  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3587.179    ± 350.594  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3286.909    ± 508.433  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51401.342    ± 229.029  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66484.698    ± 610.355  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56536.097   ± 1198.116  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6245.859    ± 385.967  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6535.066    ± 155.453  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6361.327    ± 161.729  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        738.244     ± 74.191  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        706.948     ± 51.135  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5296.532   ± 1720.026  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2692.586     ± 58.835  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4462.360     ± 31.351  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     463518.468   ± 4789.839  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     469068.897   ± 3348.646  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     472083.454   ± 3648.746  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     482182.771   ± 4158.605  ops/s
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

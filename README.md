# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-28T04:40:56Z
- **Commit:** [`ba6b0b5`](https://github.com/prometheus/client_java/commit/ba6b0b5a95b98ad40d2b513f3e446fdfbf94d0ab)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.07K | ± 1.33K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.71K | ± 412.00 | ops/s | 1.2x slower |
| prometheusAdd | 50.76K | ± 664.87 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.52K | ± 1.46K | ops/s | 1.4x slower |
| simpleclientInc | 6.54K | ± 52.47 | ops/s | 10x slower |
| simpleclientAdd | 6.41K | ± 50.01 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 19.11 | ops/s | 10x slower |
| openTelemetryAdd | 3.37K | ± 531.02 | ops/s | 20x slower |
| openTelemetryInc | 3.21K | ± 485.35 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 2.94K | ± 168.35 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.31K | ± 769.28 | ops/s | **fastest** |
| simpleclient | 4.37K | ± 37.96 | ops/s | 1.4x slower |
| prometheusNative | 2.90K | ± 246.64 | ops/s | 2.2x slower |
| openTelemetryClassic | 752.03 | ± 7.64 | ops/s | 8.4x slower |
| openTelemetryExponential | 669.55 | ± 58.81 | ops/s | 9.4x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.67K | ± 934.88 | ops/s | **fastest** |
| openMetricsWriteToNull | 22.85K | ± 794.07 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 514.81K | ± 4.96K | ops/s | **fastest** |
| prometheusWriteToByteArray | 502.77K | ± 7.25K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 489.12K | ± 2.28K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 486.04K | ± 2.13K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48522.650   ± 1461.234  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3373.243    ± 531.018  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3213.410    ± 485.346  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2936.267    ± 168.345  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50759.497    ± 664.873  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66066.469   ± 1331.224  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56708.087    ± 412.003  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6411.056     ± 50.006  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6543.000     ± 52.471  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6343.257     ± 19.109  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        752.034      ± 7.635  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        669.551     ± 58.806  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6310.326    ± 769.279  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2900.971    ± 246.643  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4368.810     ± 37.959  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      22845.673    ± 794.066  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23673.834    ± 934.882  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     486038.051   ± 2126.582  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489115.014   ± 2282.534  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     502766.565   ± 7252.323  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     514808.155   ± 4956.358  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-10T04:32:34Z
- **Commit:** [`4a6958a`](https://github.com/prometheus/client_java/commit/4a6958a5f19cc204c69d8c2fa7f7e64bb1478bdf)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.48K | ± 1.65K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.51K | ± 1.16K | ops/s | 1.2x slower |
| prometheusAdd | 51.50K | ± 75.95 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.85K | ± 1.41K | ops/s | 1.3x slower |
| simpleclientInc | 6.53K | ± 37.94 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 6.61 | ops/s | 10x slower |
| simpleclientAdd | 6.23K | ± 367.50 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 3.51K | ± 119.53 | ops/s | 19x slower |
| openTelemetryAdd | 3.31K | ± 527.41 | ops/s | 20x slower |
| openTelemetryInc | 3.20K | ± 255.57 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.07K | ± 977.28 | ops/s | **fastest** |
| simpleclient | 4.44K | ± 72.41 | ops/s | 1.1x slower |
| prometheusNative | 2.67K | ± 135.80 | ops/s | 1.9x slower |
| openTelemetryClassic | 740.62 | ± 22.95 | ops/s | 6.8x slower |
| openTelemetryExponential | 623.10 | ± 96.14 | ops/s | 8.1x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.70K | ± 436.71 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.23K | ± 279.40 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 509.23K | ± 4.57K | ops/s | **fastest** |
| prometheusWriteToByteArray | 502.98K | ± 7.09K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.95K | ± 3.47K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 477.26K | ± 4.42K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48848.339   ± 1414.336  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3312.631    ± 527.412  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3199.462    ± 255.572  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3511.127    ± 119.532  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51498.346     ± 75.954  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65479.585   ± 1652.098  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56514.560   ± 1155.062  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6227.226    ± 367.502  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6532.945     ± 37.940  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6336.378      ± 6.614  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        740.616     ± 22.955  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        623.095     ± 96.143  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5072.980    ± 977.284  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2667.932    ± 135.805  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4439.950     ± 72.409  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23226.069    ± 279.401  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23696.606    ± 436.709  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477255.955   ± 4424.582  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485954.456   ± 3467.320  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     502979.359   ± 7094.054  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     509231.787   ± 4571.296  ops/s
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

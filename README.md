# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-30T04:31:51Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 67.09K | ± 459.35 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.01K | ± 324.46 | ops/s | 1.2x slower |
| prometheusAdd | 51.53K | ± 133.08 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.08K | ± 231.23 | ops/s | 1.4x slower |
| simpleclientInc | 6.71K | ± 21.09 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.51K | ± 167.83 | ops/s | 10x slower |
| simpleclientAdd | 6.48K | ± 19.65 | ops/s | 10x slower |
| openTelemetryAdd | 1.45K | ± 225.55 | ops/s | 46x slower |
| openTelemetryInc | 1.22K | ± 23.99 | ops/s | 55x slower |
| openTelemetryIncNoLabels | 1.22K | ± 24.98 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.58K | ± 1.36K | ops/s | **fastest** |
| simpleclient | 4.44K | ± 72.28 | ops/s | 1.5x slower |
| prometheusNative | 3.10K | ± 219.27 | ops/s | 2.1x slower |
| openTelemetryClassic | 719.07 | ± 44.82 | ops/s | 9.2x slower |
| openTelemetryExponential | 554.85 | ± 23.62 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 494.79K | ± 3.54K | ops/s | **fastest** |
| prometheusWriteToByteArray | 485.45K | ± 8.05K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 481.91K | ± 2.73K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 470.21K | ± 5.79K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47083.652    ± 231.232  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1451.584    ± 225.553  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1223.700     ± 23.993  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1217.660     ± 24.985  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51527.081    ± 133.077  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67086.121    ± 459.354  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57013.766    ± 324.456  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6478.406     ± 19.648  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6708.509     ± 21.093  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6512.973    ± 167.832  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        719.069     ± 44.822  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        554.849     ± 23.622  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6580.194   ± 1355.709  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3096.931    ± 219.267  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4439.138     ± 72.279  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     470211.513   ± 5793.570  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481911.591   ± 2728.439  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485445.492   ± 8047.483  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     494791.945   ± 3538.292  ops/s
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

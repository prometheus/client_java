# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-12T04:36:56Z
- **Commit:** [`b56927d`](https://github.com/prometheus/client_java/commit/b56927d8bdc9618fd5fad7a375ebe126ff89c606)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 31.22K | ± 248.33 | ops/s | **fastest** |
| prometheusInc | 30.22K | ± 1.82K | ops/s | 1.0x slower |
| codahaleIncNoLabels | 29.09K | ± 695.81 | ops/s | 1.1x slower |
| prometheusAdd | 27.51K | ± 1.52K | ops/s | 1.1x slower |
| simpleclientInc | 6.86K | ± 90.61 | ops/s | 4.6x slower |
| simpleclientNoLabelsInc | 6.66K | ± 20.77 | ops/s | 4.7x slower |
| simpleclientAdd | 6.50K | ± 196.57 | ops/s | 4.8x slower |
| openTelemetryIncNoLabels | 2.73K | ± 327.04 | ops/s | 11x slower |
| openTelemetryAdd | 2.59K | ± 84.71 | ops/s | 12x slower |
| openTelemetryInc | 2.55K | ± 111.32 | ops/s | 12x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.33K | ± 142.57 | ops/s | **fastest** |
| prometheusClassic | 3.24K | ± 441.95 | ops/s | 1.3x slower |
| prometheusNative | 2.34K | ± 358.22 | ops/s | 1.8x slower |
| openTelemetryClassic | 605.43 | ± 44.16 | ops/s | 7.1x slower |
| openTelemetryExponential | 441.52 | ± 27.28 | ops/s | 9.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 18.25K | ± 100.31 | ops/s | **fastest** |
| prometheusWriteToNull | 18.18K | ± 121.34 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 320.52K | ± 1.43K | ops/s | **fastest** |
| prometheusWriteToByteArray | 314.95K | ± 1.75K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 295.00K | ± 1.42K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 292.26K | ± 1.67K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29085.113    ± 695.814  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2588.502     ± 84.714  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2548.539    ± 111.322  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2730.955    ± 327.043  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      27513.273   ± 1515.776  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30220.270   ± 1816.040  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31222.226    ± 248.331  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6495.986    ± 196.568  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6860.983     ± 90.607  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6656.917     ± 20.767  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        605.433     ± 44.158  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        441.519     ± 27.282  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3238.709    ± 441.951  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2342.417    ± 358.220  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4325.351    ± 142.574  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18247.462    ± 100.311  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18182.916    ± 121.336  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     292256.353   ± 1671.231  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     295000.375   ± 1421.530  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     314950.034   ± 1746.062  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     320516.278   ± 1430.762  ops/s
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

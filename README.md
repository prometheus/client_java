# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-24T04:25:59Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 30.98K | ± 859.46 | ops/s | **fastest** |
| prometheusInc | 30.26K | ± 1.08K | ops/s | 1.0x slower |
| codahaleIncNoLabels | 29.74K | ± 1.68K | ops/s | 1.0x slower |
| prometheusAdd | 28.44K | ± 52.18 | ops/s | 1.1x slower |
| simpleclientInc | 7.05K | ± 111.60 | ops/s | 4.4x slower |
| simpleclientNoLabelsInc | 6.95K | ± 282.53 | ops/s | 4.5x slower |
| simpleclientAdd | 6.61K | ± 147.88 | ops/s | 4.7x slower |
| openTelemetryIncNoLabels | 1.40K | ± 77.00 | ops/s | 22x slower |
| openTelemetryInc | 1.36K | ± 121.35 | ops/s | 23x slower |
| openTelemetryAdd | 1.29K | ± 49.48 | ops/s | 24x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.54K | ± 16.50 | ops/s | **fastest** |
| prometheusClassic | 2.49K | ± 211.08 | ops/s | 1.8x slower |
| prometheusNative | 1.92K | ± 44.19 | ops/s | 2.4x slower |
| openTelemetryClassic | 519.36 | ± 25.80 | ops/s | 8.7x slower |
| openTelemetryExponential | 402.74 | ± 20.20 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 311.70K | ± 2.34K | ops/s | **fastest** |
| prometheusWriteToByteArray | 309.23K | ± 2.89K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 291.11K | ± 3.12K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 287.54K | ± 2.28K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29741.112   ± 1675.709  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1290.603     ± 49.483  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1356.132    ± 121.349  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1399.823     ± 77.005  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28435.878     ± 52.176  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30256.248   ± 1081.927  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30982.276    ± 859.464  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6611.913    ± 147.879  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7050.117    ± 111.595  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6954.073    ± 282.530  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        519.362     ± 25.797  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        402.737     ± 20.197  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2492.341    ± 211.080  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1922.399     ± 44.187  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4538.586     ± 16.502  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     287543.983   ± 2283.507  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     291107.340   ± 3119.333  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     309232.830   ± 2890.755  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     311696.860   ± 2340.301  ops/s
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

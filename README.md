# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-22T04:28:55Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 31.30K | ± 313.91 | ops/s | **fastest** |
| prometheusInc | 29.50K | ± 921.04 | ops/s | 1.1x slower |
| prometheusAdd | 28.61K | ± 164.28 | ops/s | 1.1x slower |
| codahaleIncNoLabels | 28.10K | ± 25.15 | ops/s | 1.1x slower |
| simpleclientInc | 6.94K | ± 41.28 | ops/s | 4.5x slower |
| simpleclientNoLabelsInc | 6.76K | ± 288.98 | ops/s | 4.6x slower |
| simpleclientAdd | 6.66K | ± 115.44 | ops/s | 4.7x slower |
| openTelemetryIncNoLabels | 1.51K | ± 120.25 | ops/s | 21x slower |
| openTelemetryInc | 1.43K | ± 38.55 | ops/s | 22x slower |
| openTelemetryAdd | 1.38K | ± 45.11 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.51K | ± 30.40 | ops/s | **fastest** |
| prometheusClassic | 2.69K | ± 46.42 | ops/s | 1.7x slower |
| prometheusNative | 2.13K | ± 191.44 | ops/s | 2.1x slower |
| openTelemetryClassic | 507.14 | ± 21.80 | ops/s | 8.9x slower |
| openTelemetryExponential | 404.10 | ± 3.37 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 310.14K | ± 2.49K | ops/s | **fastest** |
| prometheusWriteToByteArray | 303.45K | ± 1.56K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 291.60K | ± 1.25K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 290.85K | ± 1.46K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28097.180     ± 25.148  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1376.153     ± 45.110  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1429.337     ± 38.550  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1505.279    ± 120.255  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28614.854    ± 164.278  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      29498.107    ± 921.037  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31304.851    ± 313.914  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6656.343    ± 115.444  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6939.498     ± 41.281  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6759.352    ± 288.982  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        507.142     ± 21.800  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        404.099      ± 3.369  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2688.263     ± 46.418  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2131.181    ± 191.438  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4511.991     ± 30.404  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     290849.545   ± 1459.961  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     291597.316   ± 1246.233  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     303451.933   ± 1556.820  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     310141.583   ± 2491.641  ops/s
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

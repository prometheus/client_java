# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-25T04:28:47Z
- **Commit:** [`6beb7fd`](https://github.com/prometheus/client_java/commit/6beb7fd3f26fb1629aae21d9d85d975f63d1a6b8)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.82K | ± 1.78K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.15K | ± 198.53 | ops/s | 1.1x slower |
| prometheusAdd | 51.25K | ± 429.09 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.11K | ± 1.08K | ops/s | 1.3x slower |
| simpleclientInc | 6.68K | ± 86.33 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.55K | ± 193.34 | ops/s | 9.7x slower |
| simpleclientAdd | 6.37K | ± 210.37 | ops/s | 10x slower |
| openTelemetryAdd | 1.27K | ± 46.62 | ops/s | 50x slower |
| openTelemetryIncNoLabels | 1.24K | ± 24.16 | ops/s | 51x slower |
| openTelemetryInc | 1.20K | ± 47.64 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.50K | ± 1.81K | ops/s | **fastest** |
| simpleclient | 4.56K | ± 20.40 | ops/s | 1.4x slower |
| prometheusNative | 2.62K | ± 76.89 | ops/s | 2.5x slower |
| openTelemetryClassic | 658.46 | ± 33.48 | ops/s | 9.9x slower |
| openTelemetryExponential | 552.91 | ± 20.77 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.03K | ± 1.62K | ops/s | **fastest** |
| prometheusWriteToByteArray | 487.59K | ± 2.04K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 478.56K | ± 3.95K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 471.87K | ± 5.02K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49107.638   ± 1084.908  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1265.717     ± 46.625  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1197.419     ± 47.644  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1241.139     ± 24.162  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51250.191    ± 429.089  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63816.525   ± 1782.008  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57148.635    ± 198.530  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6373.921    ± 210.367  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6675.218     ± 86.335  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6545.386    ± 193.342  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        658.464     ± 33.476  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        552.913     ± 20.773  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6501.435   ± 1811.684  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2618.753     ± 76.891  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4558.446     ± 20.400  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     471867.892   ± 5016.338  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     478561.182   ± 3945.416  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     487587.699   ± 2039.185  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492033.637   ± 1615.607  ops/s
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

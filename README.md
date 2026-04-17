# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-17T04:31:56Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 57.89K | ± 2.19K | ops/s | **fastest** |
| prometheusAdd | 48.27K | ± 500.23 | ops/s | 1.2x slower |
| prometheusNoLabelsInc | 45.59K | ± 9.87K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.27K | ± 128.21 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.31K | ± 13.96 | ops/s | 9.2x slower |
| simpleclientInc | 6.22K | ± 119.93 | ops/s | 9.3x slower |
| simpleclientAdd | 5.96K | ± 262.16 | ops/s | 9.7x slower |
| openTelemetryInc | 1.46K | ± 183.82 | ops/s | 40x slower |
| openTelemetryAdd | 1.39K | ± 61.22 | ops/s | 42x slower |
| openTelemetryIncNoLabels | 1.35K | ± 79.89 | ops/s | 43x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.05K | ± 577.67 | ops/s | **fastest** |
| simpleclient | 4.41K | ± 151.49 | ops/s | 1.1x slower |
| prometheusNative | 2.69K | ± 88.17 | ops/s | 1.9x slower |
| openTelemetryClassic | 594.91 | ± 13.79 | ops/s | 8.5x slower |
| openTelemetryExponential | 517.88 | ± 7.14 | ops/s | 9.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 538.23K | ± 4.87K | ops/s | **fastest** |
| prometheusWriteToByteArray | 524.73K | ± 4.85K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 515.10K | ± 2.85K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 512.71K | ± 5.41K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44269.214    ± 128.214  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1385.722     ± 61.225  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1459.418    ± 183.824  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1347.389     ± 79.885  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48272.981    ± 500.227  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      57887.728   ± 2189.105  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      45590.463   ± 9870.529  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5962.117    ± 262.165  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6222.610    ± 119.930  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6305.687     ± 13.956  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        594.912     ± 13.786  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        517.885      ± 7.140  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5050.278    ± 577.672  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2690.729     ± 88.170  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4410.880    ± 151.488  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     512706.586   ± 5407.507  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     515096.737   ± 2845.521  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     524732.251   ± 4851.511  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     538233.274   ± 4866.546  ops/s
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

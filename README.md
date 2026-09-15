# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-15T03:53:29Z
- **Commit:** [`02622cb`](https://github.com/prometheus/client_java/commit/02622cbea68e584146500840ea59c1a0e50299f2)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.08K | ± 283.34 | ops/s |
| prometheusNoLabelsInc | 56.63K | ± 366.83 | ops/s |
| prometheusAdd | 51.06K | ± 670.42 | ops/s |
| codahaleIncNoLabels | 47.16K | ± 787.64 | ops/s |
| openTelemetryIncNoLabels | 18.56K | ± 50.14 | ops/s |
| openTelemetryInc | 15.08K | ± 175.59 | ops/s |
| openTelemetryAdd | 12.74K | ± 158.05 | ops/s |
| simpleclientInc | 6.50K | ± 8.35 | ops/s |
| simpleclientNoLabelsInc | 6.42K | ± 135.95 | ops/s |
| simpleclientAdd | 6.13K | ± 345.74 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.97K | ± 37.41 | ops/s |
| prometheusClassic | 6.50K | ± 783.13 | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 136.97 | ops/s |
| simpleclient | 4.50K | ± 46.01 | ops/s |
| prometheusNative | 3.00K | ± 278.93 | ops/s |
| openTelemetryExponential | 788.72 | ± 151.59 | ops/s |
| openTelemetryClassic | 779.81 | ± 40.05 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 24.51K | ± 431.69 | ops/s |
| openMetricsWriteToNull | 24.27K | ± 240.12 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 486.39K | ± 3.47K | ops/s |
| prometheusWriteToByteArray | 480.78K | ± 4.08K | ops/s |
| openMetricsWriteToByteArray | 466.08K | ± 4.28K | ops/s |
| openMetricsWriteToNull | 464.04K | ± 2.09K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47159.094    ± 787.643  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12743.452    ± 158.051  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15079.837    ± 175.593  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18563.541     ± 50.143  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51060.324    ± 670.416  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66082.676    ± 283.336  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56632.780    ± 366.825  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6125.594    ± 345.740  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6502.522      ± 8.346  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6416.369    ± 135.950  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        779.809     ± 40.047  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        788.724    ± 151.590  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6502.733    ± 783.134  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11973.456     ± 37.415  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4543.014    ± 136.975  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2999.438    ± 278.929  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4503.596     ± 46.008  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24266.709    ± 240.117  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24509.854    ± 431.691  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     466075.147   ± 4280.516  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     464043.539   ± 2089.326  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     480775.796   ± 4083.462  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     486385.612   ± 3474.005  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

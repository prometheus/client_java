# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-30T03:52:53Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 62.62K | ± 3.53K | ops/s |
| prometheusNoLabelsInc | 56.41K | ± 864.24 | ops/s |
| prometheusAdd | 50.82K | ± 426.32 | ops/s |
| codahaleIncNoLabels | 48.84K | ± 1.46K | ops/s |
| openTelemetryIncNoLabels | 18.66K | ± 208.28 | ops/s |
| openTelemetryInc | 14.86K | ± 286.94 | ops/s |
| openTelemetryAdd | 12.88K | ± 87.33 | ops/s |
| simpleclientInc | 6.57K | ± 36.56 | ops/s |
| simpleclientNoLabelsInc | 6.43K | ± 132.02 | ops/s |
| simpleclientAdd | 6.24K | ± 335.54 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.27K | ± 46.44 | ops/s |
| prometheusClassic | 5.46K | ± 1.44K | ops/s |
| prometheusClassicSingleThread | 4.58K | ± 42.38 | ops/s |
| simpleclient | 4.42K | ± 87.73 | ops/s |
| prometheusNative | 2.78K | ± 346.70 | ops/s |
| openTelemetryClassic | 768.03 | ± 4.27 | ops/s |
| openTelemetryExponential | 733.83 | ± 27.52 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.54K | ± 392.93 | ops/s |
| openMetricsWriteToNull | 23.24K | ± 714.09 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 509.24K | ± 5.24K | ops/s |
| prometheusWriteToByteArray | 502.47K | ± 8.39K | ops/s |
| openMetricsWriteToByteArray | 490.97K | ± 6.87K | ops/s |
| openMetricsWriteToNull | 489.43K | ± 3.51K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48835.330   ± 1464.432  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12880.433     ± 87.328  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14860.171    ± 286.936  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18662.256    ± 208.283  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50823.457    ± 426.320  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62621.943   ± 3531.790  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56411.954    ± 864.238  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6237.448    ± 335.537  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6568.203     ± 36.558  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6429.279    ± 132.018  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        768.029      ± 4.268  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        733.834     ± 27.524  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5455.781   ± 1438.917  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12274.225     ± 46.437  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4576.730     ± 42.381  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2784.334    ± 346.699  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4417.987     ± 87.727  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23237.516    ± 714.087  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23540.642    ± 392.926  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     490968.659   ± 6871.111  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489426.857   ± 3505.364  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     502470.883   ± 8386.853  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     509235.469   ± 5238.494  ops/s
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

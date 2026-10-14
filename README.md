# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-14T04:23:13Z
- **Commit:** [`3815993`](https://github.com/prometheus/client_java/commit/3815993364e563882f0a6b0741685b8a1a740e45)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 699.23M | ± 61198.51K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 222.58M | ± 1702.87K | ops/s |
| prometheusLabelValuesInc | 206.84M | ± 4919.62K | ops/s |
| prometheusLabelValuesIncSingleThread | 98.70M | ± 1119.38K | ops/s |
| prometheusInc | 69.45K | ± 502.19 | ops/s |
| prometheusNoLabelsInc | 66.54K | ± 846.99 | ops/s |
| codahaleIncNoLabels | 61.77K | ± 585.79 | ops/s |
| prometheusAdd | 60.28K | ± 3.99K | ops/s |
| openTelemetryBoundInc | 52.96K | ± 1.07K | ops/s |
| openTelemetryBoundAdd | 47.32K | ± 422.16 | ops/s |
| openTelemetryIncNoLabels | 35.34K | ± 496.18 | ops/s |
| openTelemetryInc | 33.73K | ± 2.13K | ops/s |
| openTelemetryAdd | 28.43K | ± 2.27K | ops/s |
| simpleclientInc | 10.91K | ± 446.21 | ops/s |
| simpleclientNoLabelsInc | 10.78K | ± 425.51 | ops/s |
| simpleclientAdd | 10.67K | ± 108.88 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.52K | ± 100.50 | ops/s |
| prometheusClassic | 8.54K | ± 2.62K | ops/s |
| openTelemetryClassic | 7.78K | ± 1.43K | ops/s |
| simpleclient | 7.12K | ± 114.55 | ops/s |
| openTelemetryBoundClassic | 6.77K | ± 1.58K | ops/s |
| prometheusNative | 4.47K | ± 344.79 | ops/s |
| prometheusClassicSingleThread | 4.42K | ± 96.02 | ops/s |
| openTelemetryBoundExponential | 1.06K | ± 38.03 | ops/s |
| openTelemetryExponential | 979.87 | ± 29.00 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 33.89K | ± 446.82 | ops/s |
| openMetricsWriteToNull | 33.56K | ± 680.64 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 847.96K | ± 33.82K | ops/s |
| prometheusWriteToByteArray | 840.53K | ± 49.25K | ops/s |
| openMetricsWriteToNull | 762.39K | ± 38.81K | ops/s |
| openMetricsWriteToByteArray | 752.57K | ± 39.00K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.033 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.020 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.018 | — | — |
| CounterBenchmark.openTelemetryInc | 0.028 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.027 | — | — |
| CounterBenchmark.prometheusAdd | 0.061 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.053 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.055 | — | — |
| CounterBenchmark.simpleclientAdd | 0.089 | — | — |
| CounterBenchmark.simpleclientInc | 0.087 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.088 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.895 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.126 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.966 | — | — |
| HistogramBenchmark.prometheusClassic | 0.471 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.514 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.623 | — | — |
| HistogramBenchmark.prometheusNative | 335792.859 | — | — |
| HistogramBenchmark.simpleclient | 0.133 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.105 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.104 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      61769.689    ± 585.786  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      28427.286   ± 2266.311  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      47316.091    ± 422.156  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      52964.142   ± 1066.155  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      33729.706   ± 2129.890  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      35343.166    ± 496.178  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      60277.480   ± 3990.593  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  699233320.124 ± 61198509.090  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  222578617.064 ± 1702870.523  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      69451.305    ± 502.192  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  206835205.183 ± 4919618.628  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   98697308.768 ± 1119377.592  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66538.133    ± 846.990  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10672.941    ± 108.875  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10906.902    ± 446.207  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      10775.110    ± 425.510  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6766.898   ± 1577.646  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1061.440     ± 38.027  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       7779.050   ± 1432.936  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        979.867     ± 29.004  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8540.823   ± 2622.940  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15519.126    ± 100.495  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4421.569     ± 96.016  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4473.486    ± 344.789  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       7117.488    ± 114.550  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      33563.121    ± 680.642  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      33886.634    ± 446.821  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     752572.324  ± 39001.875  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     762390.856  ± 38814.532  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     840533.148  ± 49247.570  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     847957.779  ± 33817.240  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter updates and label-value lookup (selected methods only) |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

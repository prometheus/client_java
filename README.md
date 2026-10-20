# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-20T04:22:53Z
- **Commit:** [`16ee497`](https://github.com/prometheus/client_java/commit/16ee4972d1e960a6bcd7a868bc337e6943fd8c00)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 557.32M | ± 11551.11K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 332.96M | ± 1515.79K | ops/s |
| prometheusLabelValuesInc | 116.61M | ± 2145.29K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.38M | ± 183.08K | ops/s |
| prometheusInc | 63.14K | ± 1.93K | ops/s |
| prometheusNoLabelsInc | 55.98K | ± 1.47K | ops/s |
| prometheusAdd | 50.99K | ± 720.76 | ops/s |
| codahaleIncNoLabels | 48.44K | ± 1.63K | ops/s |
| openTelemetryBoundInc | 37.51K | ± 626.84 | ops/s |
| openTelemetryBoundAdd | 31.75K | ± 794.93 | ops/s |
| openTelemetryIncNoLabels | 23.27K | ± 1.18K | ops/s |
| openTelemetryInc | 23.04K | ± 344.76 | ops/s |
| openTelemetryAdd | 19.26K | ± 319.44 | ops/s |
| simpleclientInc | 6.55K | ± 60.26 | ops/s |
| simpleclientAdd | 6.27K | ± 228.58 | ops/s |
| simpleclientNoLabelsInc | 6.26K | ± 115.76 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.99K | ± 180.44 | ops/s |
| prometheusClassic | 7.08K | ± 614.63 | ops/s |
| openTelemetryBoundClassic | 6.19K | ± 359.82 | ops/s |
| openTelemetryClassic | 4.61K | ± 1.80K | ops/s |
| simpleclient | 4.43K | ± 42.85 | ops/s |
| prometheusClassicSingleThread | 4.40K | ± 74.37 | ops/s |
| prometheusNative | 2.50K | ± 63.16 | ops/s |
| openTelemetryBoundExponential | 1.12K | ± 44.30 | ops/s |
| openTelemetryExponential | 948.23 | ± 91.66 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 30.87K | ± 914.97 | ops/s |
| openMetricsWriteToNull | 30.28K | ± 813.26 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 502.44K | ± 5.17K | ops/s |
| prometheusWriteToByteArray | 498.93K | ± 5.14K | ops/s |
| openMetricsWriteToNull | 486.55K | ± 5.99K | ops/s |
| openMetricsWriteToByteArray | 481.61K | ± 3.21K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.150 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.150 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.153 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.840 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.226 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.997 | — | — |
| HistogramBenchmark.prometheusClassic | 0.523 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.679 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.663 | — | — |
| HistogramBenchmark.prometheusNative | 335793.476 | — | — |
| HistogramBenchmark.simpleclient | 0.214 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.117 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.114 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48441.343   ± 1626.845  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19261.672    ± 319.442  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31751.207    ± 794.931  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37506.783    ± 626.843  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23038.379    ± 344.757  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23271.036   ± 1180.582  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50993.200    ± 720.763  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  557323263.244 ± 11551111.424  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  332957648.951 ± 1515787.814  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63141.486   ± 1931.399  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116608206.345 ± 2145290.824  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58380020.438 ± 183080.341  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55982.773   ± 1469.580  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6272.885    ± 228.582  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6549.729     ± 60.265  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6255.076    ± 115.765  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6187.680    ± 359.815  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1119.426     ± 44.295  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4612.938   ± 1796.385  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        948.233     ± 91.661  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7081.326    ± 614.627  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11988.109    ± 180.444  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4404.511     ± 74.372  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2503.031     ± 63.158  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4433.168     ± 42.846  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      30284.015    ± 813.259  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30865.468    ± 914.968  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481611.958   ± 3211.023  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486549.517   ± 5991.603  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     498927.054   ± 5135.775  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502442.706   ± 5166.117  ops/s
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

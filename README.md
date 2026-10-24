# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-24T04:22:09Z
- **Commit:** [`533375f`](https://github.com/prometheus/client_java/commit/533375f65bc9130d9ea88ca139784bfb966b04f7)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 553.24M | ± 10939.72K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.22M | ± 380.93K | ops/s |
| prometheusLabelValuesInc | 115.55M | ± 1952.06K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.45M | ± 371.71K | ops/s |
| prometheusInc | 65.45K | ± 1.13K | ops/s |
| prometheusNoLabelsInc | 56.45K | ± 1.06K | ops/s |
| prometheusAdd | 50.99K | ± 859.62 | ops/s |
| codahaleIncNoLabels | 49.93K | ± 690.03 | ops/s |
| openTelemetryBoundInc | 37.90K | ± 596.47 | ops/s |
| openTelemetryBoundAdd | 31.56K | ± 682.43 | ops/s |
| openTelemetryInc | 22.97K | ± 351.14 | ops/s |
| openTelemetryIncNoLabels | 21.74K | ± 309.44 | ops/s |
| openTelemetryAdd | 19.39K | ± 338.06 | ops/s |
| simpleclientInc | 6.49K | ± 114.91 | ops/s |
| simpleclientAdd | 6.37K | ± 110.53 | ops/s |
| simpleclientNoLabelsInc | 6.34K | ± 171.22 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.00K | ± 156.36 | ops/s |
| prometheusClassic | 8.37K | ± 1.39K | ops/s |
| simpleclient | 4.38K | ± 63.70 | ops/s |
| prometheusClassicSingleThread | 4.19K | ± 499.71 | ops/s |
| openTelemetryClassic | 3.79K | ± 58.81 | ops/s |
| openTelemetryBoundClassic | 2.86K | ± 873.65 | ops/s |
| prometheusNative | 2.85K | ± 208.31 | ops/s |
| openTelemetryBoundExponential | 1.04K | ± 40.05 | ops/s |
| openTelemetryExponential | 981.15 | ± 63.65 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 31.79K | ± 308.92 | ops/s |
| openMetricsWriteToNull | 31.08K | ± 423.15 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 510.01K | ± 4.79K | ops/s |
| openMetricsWriteToByteArray | 500.58K | ± 3.96K | ops/s |
| prometheusWriteToByteArray | 491.75K | ± 8.72K | ops/s |
| openMetricsWriteToNull | 489.07K | ± 2.61K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.353 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.907 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.247 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.958 | — | — |
| HistogramBenchmark.prometheusClassic | 0.452 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.671 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.705 | — | — |
| HistogramBenchmark.prometheusNative | 335793.302 | — | — |
| HistogramBenchmark.simpleclient | 0.215 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.113 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.111 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49932.656    ± 690.028  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19385.589    ± 338.063  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31557.999    ± 682.430  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37897.685    ± 596.474  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22966.863    ± 351.136  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21740.370    ± 309.441  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50986.529    ± 859.623  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  553243440.600 ± 10939723.152  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334221970.959 ± 380931.705  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65452.525   ± 1126.161  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115553844.806 ± 1952064.440  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58454269.301 ± 371706.637  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56445.170   ± 1056.467  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6368.938    ± 110.533  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6488.495    ± 114.909  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6336.408    ± 171.224  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2863.928    ± 873.654  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1037.702     ± 40.055  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3786.147     ± 58.814  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        981.146     ± 63.652  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8370.079   ± 1387.378  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12004.126    ± 156.363  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4193.953    ± 499.714  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2848.274    ± 208.306  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4375.903     ± 63.695  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31079.171    ± 423.152  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31786.250    ± 308.921  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     500575.031   ± 3960.256  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489065.003   ± 2612.250  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491754.286   ± 8717.663  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     510005.098   ± 4790.289  ops/s
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

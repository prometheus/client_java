# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-07T04:33:13Z
- **Commit:** [`fc5d0ba`](https://github.com/prometheus/client_java/commit/fc5d0ba6fad6c9cb26e455139accd446d250ac08)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 553.94M | ± 11013.27K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.53M | ± 518.95K | ops/s |
| prometheusLabelValuesInc | 115.49M | ± 1541.85K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.71M | ± 441.01K | ops/s |
| prometheusInc | 66.02K | ± 1.07K | ops/s |
| prometheusNoLabelsInc | 54.78K | ± 2.48K | ops/s |
| prometheusAdd | 50.94K | ± 922.67 | ops/s |
| codahaleIncNoLabels | 48.85K | ± 1.96K | ops/s |
| openTelemetryBoundInc | 37.84K | ± 733.04 | ops/s |
| openTelemetryBoundAdd | 31.07K | ± 737.05 | ops/s |
| openTelemetryIncNoLabels | 22.49K | ± 1.28K | ops/s |
| openTelemetryInc | 18.10K | ± 309.74 | ops/s |
| openTelemetryAdd | 15.06K | ± 849.15 | ops/s |
| simpleclientInc | 6.57K | ± 99.17 | ops/s |
| simpleclientNoLabelsInc | 6.28K | ± 100.74 | ops/s |
| simpleclientAdd | 6.27K | ± 176.68 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.99K | ± 187.89 | ops/s |
| prometheusClassic | 7.22K | ± 622.82 | ops/s |
| openTelemetryBoundClassic | 4.77K | ± 313.76 | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 13.81 | ops/s |
| simpleclient | 4.39K | ± 85.45 | ops/s |
| openTelemetryClassic | 3.72K | ± 244.99 | ops/s |
| prometheusNative | 2.88K | ± 192.19 | ops/s |
| openTelemetryBoundExponential | 998.67 | ± 135.63 | ops/s |
| openTelemetryExponential | 884.80 | ± 48.06 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.75K | ± 212.65 | ops/s |
| openMetricsWriteToNull | 23.45K | ± 433.34 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 501.96K | ± 4.20K | ops/s |
| prometheusWriteToByteArray | 498.18K | ± 3.50K | ops/s |
| openMetricsWriteToNull | 490.39K | ± 3.06K | ops/s |
| openMetricsWriteToByteArray | 479.98K | ± 5.34K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.062 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.067 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.197 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.945 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.253 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.052 | — | — |
| HistogramBenchmark.prometheusClassic | 0.513 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.680 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.642 | — | — |
| HistogramBenchmark.prometheusNative | 417713.299 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.149 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.147 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48852.138   ± 1958.930  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15058.241    ± 849.154  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31069.135    ± 737.047  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37841.570    ± 733.044  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18103.665    ± 309.743  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22485.600   ± 1278.984  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50940.347    ± 922.675  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  553941130.426 ± 11013269.116  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334526090.343 ± 518951.327  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66015.240   ± 1066.758  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115489401.325 ± 1541850.035  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58708369.281 ± 441010.204  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54782.312   ± 2483.056  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6271.458    ± 176.676  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6565.043     ± 99.169  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6276.025    ± 100.741  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4773.606    ± 313.763  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        998.675    ± 135.631  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3717.872    ± 244.994  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        884.801     ± 48.063  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7222.689    ± 622.816  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11985.567    ± 187.892  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4537.238     ± 13.808  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2881.529    ± 192.194  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4392.361     ± 85.454  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23451.496    ± 433.341  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23745.213    ± 212.653  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479982.964   ± 5342.259  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490385.555   ± 3056.386  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     498179.541   ± 3499.095  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     501960.273   ± 4199.462  ops/s
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

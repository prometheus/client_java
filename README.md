# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-17T04:15:00Z
- **Commit:** [`3ea508f`](https://github.com/prometheus/client_java/commit/3ea508f61ddaa1f8b43266e3bf378d7f60e14f25)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 259.94M | ± 8512.76K | ops/s |
| prometheusLabelValuesInc | 121.66M | ± 1011.65K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 92.17M | ± 86.71K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.78M | ± 357.26K | ops/s |
| prometheusInc | 25.70K | ± 88.75 | ops/s |
| codahaleIncNoLabels | 25.60K | ± 1.39K | ops/s |
| prometheusNoLabelsInc | 25.57K | ± 201.85 | ops/s |
| openTelemetryBoundInc | 25.11K | ± 362.97 | ops/s |
| prometheusAdd | 24.95K | ± 95.68 | ops/s |
| openTelemetryBoundAdd | 23.14K | ± 131.47 | ops/s |
| openTelemetryIncNoLabels | 20.37K | ± 539.40 | ops/s |
| openTelemetryInc | 20.23K | ± 686.59 | ops/s |
| openTelemetryAdd | 17.14K | ± 230.65 | ops/s |
| simpleclientInc | 6.47K | ± 74.11 | ops/s |
| simpleclientNoLabelsInc | 6.26K | ± 62.94 | ops/s |
| simpleclientAdd | 6.23K | ± 101.68 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 6.56K | ± 11.73 | ops/s |
| openTelemetryBoundClassic | 4.37K | ± 2.51K | ops/s |
| simpleclient | 4.10K | ± 70.25 | ops/s |
| prometheusClassic | 3.33K | ± 119.33 | ops/s |
| openTelemetryClassic | 2.85K | ± 985.41 | ops/s |
| prometheusClassicSingleThread | 2.84K | ± 63.98 | ops/s |
| prometheusNative | 2.10K | ± 175.24 | ops/s |
| openTelemetryBoundExponential | 691.98 | ± 137.46 | ops/s |
| openTelemetryExponential | 596.24 | ± 33.74 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 18.84K | ± 52.35 | ops/s |
| openMetricsWriteToNull | 18.80K | ± 62.51 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 319.48K | ± 3.82K | ops/s |
| prometheusWriteToByteArray | 316.08K | ± 2.01K | ops/s |
| openMetricsWriteToNull | 294.35K | ± 1.01K | ops/s |
| openMetricsWriteToByteArray | 291.49K | ± 1.09K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.037 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.054 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.040 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.037 | — | — |
| CounterBenchmark.openTelemetryInc | 0.046 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.046 | — | — |
| CounterBenchmark.prometheusAdd | 0.148 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.144 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.145 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.145 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.289 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.398 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.355 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.576 | — | — |
| HistogramBenchmark.prometheusClassic | 1.112 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.237 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.973 | — | — |
| HistogramBenchmark.prometheusNative | 417713.785 | — | — |
| HistogramBenchmark.simpleclient | 0.230 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.188 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.187 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18458.669 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.669 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      25600.403   ± 1385.741  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      17143.909    ± 230.649  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      23138.049    ± 131.475  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      25112.178    ± 362.967  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      20229.758    ± 686.590  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      20374.133    ± 539.399  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      24953.404     ± 95.680  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  259937423.434 ± 8512764.676  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   92172742.291  ± 86707.631  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      25704.625     ± 88.752  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  121655576.483 ± 1011650.261  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58783606.337 ± 357259.102  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      25574.728    ± 201.853  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6227.189    ± 101.677  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6472.611     ± 74.107  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6264.506     ± 62.942  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4367.410   ± 2508.040  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        691.981    ± 137.458  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2848.733    ± 985.408  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        596.236     ± 33.744  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3331.391    ± 119.332  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6562.914     ± 11.732  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2839.007     ± 63.977  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2101.963    ± 175.239  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4102.264     ± 70.250  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18796.179     ± 62.515  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18838.825     ± 52.346  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     291487.929   ± 1093.693  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     294352.229   ± 1014.168  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     316079.543   ± 2009.237  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     319479.956   ± 3818.932  ops/s
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

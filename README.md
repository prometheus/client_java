# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-30T04:29:09Z
- **Commit:** [`ba050b0`](https://github.com/prometheus/client_java/commit/ba050b022f6e06203df318a7871b744cc4d6a167)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 294.84M | ± 5593.89K | ops/s |
| prometheusLabelValuesInc | 94.39M | ± 1476.48K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 93.39M | ± 22.69K | ops/s |
| prometheusLabelValuesIncSingleThread | 57.49M | ± 1694.23K | ops/s |
| prometheusInc | 31.56K | ± 34.49 | ops/s |
| prometheusNoLabelsInc | 31.14K | ± 376.94 | ops/s |
| codahaleIncNoLabels | 29.63K | ± 1.23K | ops/s |
| openTelemetryBoundInc | 29.23K | ± 74.04 | ops/s |
| prometheusAdd | 28.55K | ± 98.21 | ops/s |
| openTelemetryBoundAdd | 27.66K | ± 147.33 | ops/s |
| openTelemetryIncNoLabels | 22.44K | ± 1.04K | ops/s |
| openTelemetryInc | 16.89K | ± 205.91 | ops/s |
| openTelemetryAdd | 15.05K | ± 304.58 | ops/s |
| simpleclientInc | 6.98K | ± 40.11 | ops/s |
| simpleclientAdd | 6.56K | ± 218.64 | ops/s |
| simpleclientNoLabelsInc | 6.52K | ± 153.79 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.84K | ± 103.09 | ops/s |
| prometheusClassic | 4.65K | ± 1.68K | ops/s |
| simpleclient | 4.42K | ± 91.20 | ops/s |
| openTelemetryBoundClassic | 4.12K | ± 2.18K | ops/s |
| prometheusClassicSingleThread | 3.24K | ± 80.71 | ops/s |
| openTelemetryClassic | 2.73K | ± 529.85 | ops/s |
| prometheusNative | 2.04K | ± 119.77 | ops/s |
| openTelemetryBoundExponential | 697.32 | ± 100.50 | ops/s |
| openTelemetryExponential | 465.79 | ± 44.95 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 18.32K | ± 71.69 | ops/s |
| openMetricsWriteToNull | 18.18K | ± 154.77 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 345.17K | ± 1.88K | ops/s |
| prometheusWriteToByteArray | 339.35K | ± 2.56K | ops/s |
| openMetricsWriteToNull | 318.27K | ± 1.81K | ops/s |
| openMetricsWriteToByteArray | 315.98K | ± 1.55K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.031 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.062 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.034 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.032 | — | — |
| CounterBenchmark.openTelemetryInc | 0.055 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.129 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.117 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.118 | — | — |
| CounterBenchmark.simpleclientAdd | 0.142 | — | — |
| CounterBenchmark.simpleclientInc | 0.133 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.142 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.268 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.363 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.358 | — | — |
| HistogramBenchmark.openTelemetryExponential | 2.013 | — | — |
| HistogramBenchmark.prometheusClassic | 0.873 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.036 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.881 | — | — |
| HistogramBenchmark.prometheusNative | 335793.868 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.192 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.191 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.669 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.002 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29634.192   ± 1232.058  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15050.498    ± 304.585  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      27660.412    ± 147.331  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      29227.574     ± 74.043  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      16888.574    ± 205.915  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22437.614   ± 1035.659  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28550.654     ± 98.211  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  294843708.653 ± 5593894.086  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   93394523.958  ± 22691.067  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31564.232     ± 34.490  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15   94390071.620 ± 1476483.057  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   57488879.637 ± 1694233.592  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31141.270    ± 376.935  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6561.425    ± 218.637  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6976.689     ± 40.110  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6520.040    ± 153.787  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4122.391   ± 2180.271  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        697.325    ± 100.503  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2731.395    ± 529.852  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        465.794     ± 44.947  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4645.904   ± 1680.060  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7843.466    ± 103.089  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3236.886     ± 80.712  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2044.963    ± 119.770  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4417.642     ± 91.199  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18176.025    ± 154.771  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18316.334     ± 71.687  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     315983.962   ± 1545.238  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     318268.514   ± 1810.206  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     339346.812   ± 2563.857  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     345172.810   ± 1876.437  ops/s
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

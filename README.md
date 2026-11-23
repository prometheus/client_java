# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-23T04:16:51Z
- **Commit:** [`976ef13`](https://github.com/prometheus/client_java/commit/976ef13e6831424335b6f58f7301152d745a5346)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 556.37M | ± 11920.99K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.88M | ± 450.31K | ops/s |
| prometheusLabelValuesInc | 118.27M | ± 2175.34K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.02M | ± 426.65K | ops/s |
| prometheusInc | 64.12K | ± 1.99K | ops/s |
| prometheusNoLabelsInc | 56.27K | ± 760.09 | ops/s |
| prometheusAdd | 51.06K | ± 1.06K | ops/s |
| codahaleIncNoLabels | 49.14K | ± 1.31K | ops/s |
| openTelemetryBoundInc | 37.67K | ± 661.34 | ops/s |
| openTelemetryBoundAdd | 31.52K | ± 578.75 | ops/s |
| openTelemetryInc | 22.75K | ± 449.80 | ops/s |
| openTelemetryIncNoLabels | 22.53K | ± 1.26K | ops/s |
| openTelemetryAdd | 18.99K | ± 425.21 | ops/s |
| simpleclientInc | 6.56K | ± 103.23 | ops/s |
| simpleclientNoLabelsInc | 6.44K | ± 192.83 | ops/s |
| simpleclientAdd | 6.13K | ± 281.72 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.89K | ± 234.43 | ops/s |
| prometheusClassic | 7.46K | ± 890.70 | ops/s |
| openTelemetryClassic | 4.61K | ± 911.83 | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 19.07 | ops/s |
| simpleclient | 4.47K | ± 87.28 | ops/s |
| openTelemetryBoundClassic | 3.97K | ± 1.36K | ops/s |
| prometheusNative | 2.49K | ± 138.37 | ops/s |
| openTelemetryBoundExponential | 1.07K | ± 36.27 | ops/s |
| openTelemetryExponential | 912.99 | ± 51.81 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 31.34K | ± 571.25 | ops/s |
| openMetricsWriteToNull | 29.71K | ± 856.15 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 477.22K | ± 6.16K | ops/s |
| prometheusWriteToByteArray | 470.44K | ± 8.03K | ops/s |
| openMetricsWriteToByteArray | 462.23K | ± 7.89K | ops/s |
| openMetricsWriteToNull | 459.57K | ± 11.81K | ops/s |

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
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.153 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.145 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.262 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.876 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.211 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.034 | — | — |
| HistogramBenchmark.prometheusClassic | 0.500 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.686 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.647 | — | — |
| HistogramBenchmark.prometheusNative | 417713.490 | — | — |
| HistogramBenchmark.simpleclient | 0.211 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.119 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.112 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18496.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49140.689   ± 1314.481  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      18991.048    ± 425.211  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31521.647    ± 578.747  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37666.078    ± 661.339  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22747.897    ± 449.797  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22527.575   ± 1255.060  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51056.154   ± 1055.076  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  556369735.591 ± 11920991.040  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334876575.442 ± 450307.594  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64115.742   ± 1990.447  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  118272989.532 ± 2175335.585  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58021147.456 ± 426649.259  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56272.710    ± 760.085  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6129.667    ± 281.725  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6563.104    ± 103.235  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6435.511    ± 192.827  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3967.484   ± 1355.826  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1073.567     ± 36.268  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4605.237    ± 911.834  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        912.993     ± 51.814  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7464.044    ± 890.696  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11887.285    ± 234.428  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4507.984     ± 19.070  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2491.797    ± 138.368  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4470.952     ± 87.279  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29706.331    ± 856.149  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31337.868    ± 571.249  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     462229.363   ± 7885.205  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     459572.748  ± 11809.062  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     470436.326   ± 8028.319  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     477215.841   ± 6163.634  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-04T05:03:02Z
- **Commit:** [`9e9deb6`](https://github.com/prometheus/client_java/commit/9e9deb6b9e591c2a62b12882d8a987f120661b71)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 557.02M | ± 6214.65K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.61M | ± 671.22K | ops/s |
| prometheusLabelValuesInc | 117.15M | ± 1205.02K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.49M | ± 989.89K | ops/s |
| prometheusInc | 65.14K | ± 1.39K | ops/s |
| prometheusNoLabelsInc | 56.62K | ± 264.29 | ops/s |
| prometheusAdd | 51.37K | ± 300.90 | ops/s |
| codahaleIncNoLabels | 47.54K | ± 3.30K | ops/s |
| openTelemetryBoundInc | 37.81K | ± 169.89 | ops/s |
| openTelemetryBoundAdd | 31.54K | ± 129.84 | ops/s |
| openTelemetryIncNoLabels | 22.42K | ± 989.03 | ops/s |
| openTelemetryInc | 18.11K | ± 134.98 | ops/s |
| openTelemetryAdd | 15.36K | ± 343.56 | ops/s |
| simpleclientInc | 6.56K | ± 20.75 | ops/s |
| simpleclientNoLabelsInc | 6.43K | ± 127.39 | ops/s |
| simpleclientAdd | 6.30K | ± 278.21 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.06K | ± 21.84 | ops/s |
| prometheusClassic | 7.09K | ± 102.68 | ops/s |
| openTelemetryBoundClassic | 6.10K | ± 1.14K | ops/s |
| openTelemetryClassic | 4.76K | ± 1.51K | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 16.80 | ops/s |
| simpleclient | 4.38K | ± 35.13 | ops/s |
| prometheusNative | 3.08K | ± 90.73 | ops/s |
| openTelemetryBoundExponential | 1.06K | ± 77.82 | ops/s |
| openTelemetryExponential | 863.59 | ± 46.16 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.69K | ± 582.26 | ops/s |
| openMetricsWriteToNull | 23.67K | ± 509.59 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 544.77K | ± 4.30K | ops/s |
| prometheusWriteToByteArray | 532.17K | ± 7.59K | ops/s |
| openMetricsWriteToNull | 516.60K | ± 2.11K | ops/s |
| openMetricsWriteToByteArray | 514.38K | ± 3.66K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.020 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.144 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.156 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.885 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.210 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.079 | — | — |
| HistogramBenchmark.prometheusClassic | 0.519 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.667 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.640 | — | — |
| HistogramBenchmark.prometheusNative | 417708.826 | — | — |
| HistogramBenchmark.simpleclient | 0.214 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.148 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.148 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47542.223   ± 3296.503  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15360.499    ± 343.564  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31535.160    ± 129.839  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37811.586    ± 169.886  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18109.272    ± 134.976  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22417.240    ± 989.029  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51369.833    ± 300.895  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  557019819.552 ± 6214651.704  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334609995.058 ± 671224.779  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65143.610   ± 1394.305  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  117154454.644 ± 1205019.293  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58490564.316 ± 989887.812  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56622.660    ± 264.287  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6304.618    ± 278.206  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6563.719     ± 20.748  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6430.210    ± 127.390  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6098.293   ± 1135.009  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1056.405     ± 77.817  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4757.055   ± 1506.433  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        863.591     ± 46.164  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7093.319    ± 102.676  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12055.323     ± 21.841  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4542.855     ± 16.802  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3080.645     ± 90.729  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4376.733     ± 35.133  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23666.580    ± 509.585  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23685.609    ± 582.256  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     514384.080   ± 3661.043  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     516604.891   ± 2105.256  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     532167.447   ± 7587.049  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     544769.003   ± 4304.647  ops/s
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

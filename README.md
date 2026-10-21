# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-21T04:24:11Z
- **Commit:** [`16ee497`](https://github.com/prometheus/client_java/commit/16ee4972d1e960a6bcd7a868bc337e6943fd8c00)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 672.10M | ± 13131.92K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 370.60M | ± 373.51K | ops/s |
| prometheusLabelValuesInc | 137.30M | ± 3338.76K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.55M | ± 160.59K | ops/s |
| prometheusInc | 78.22K | ± 933.61 | ops/s |
| prometheusNoLabelsInc | 66.10K | ± 697.65 | ops/s |
| prometheusAdd | 62.84K | ± 1.06K | ops/s |
| codahaleIncNoLabels | 57.00K | ± 1.09K | ops/s |
| openTelemetryBoundInc | 45.15K | ± 419.43 | ops/s |
| openTelemetryBoundAdd | 38.46K | ± 109.11 | ops/s |
| openTelemetryInc | 27.81K | ± 165.98 | ops/s |
| openTelemetryIncNoLabels | 26.93K | ± 322.77 | ops/s |
| openTelemetryAdd | 24.39K | ± 407.45 | ops/s |
| simpleclientInc | 7.95K | ± 18.45 | ops/s |
| simpleclientAdd | 7.74K | ± 45.14 | ops/s |
| simpleclientNoLabelsInc | 7.69K | ± 182.84 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.56K | ± 51.72 | ops/s |
| prometheusClassic | 9.66K | ± 184.98 | ops/s |
| openTelemetryBoundClassic | 8.23K | ± 766.20 | ops/s |
| prometheusClassicSingleThread | 7.12K | ± 65.09 | ops/s |
| openTelemetryClassic | 6.44K | ± 1.05K | ops/s |
| simpleclient | 5.80K | ± 225.57 | ops/s |
| prometheusNative | 3.67K | ± 291.17 | ops/s |
| openTelemetryBoundExponential | 978.53 | ± 9.24 | ops/s |
| openTelemetryExponential | 911.31 | ± 26.64 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 40.70K | ± 438.08 | ops/s |
| openMetricsWriteToNull | 40.68K | ± 301.25 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 785.32K | ± 11.77K | ops/s |
| prometheusWriteToByteArray | 763.98K | ± 11.03K | ops/s |
| openMetricsWriteToByteArray | 711.77K | ± 15.87K | ops/s |
| openMetricsWriteToNull | 709.12K | ± 24.64K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.038 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.034 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.035 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.047 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.122 | — | — |
| CounterBenchmark.simpleclientInc | 0.119 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.123 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.117 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.962 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.152 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.035 | — | — |
| HistogramBenchmark.prometheusClassic | 0.382 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.460 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.411 | — | — |
| HistogramBenchmark.prometheusNative | 417695.705 | — | — |
| HistogramBenchmark.simpleclient | 0.165 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.087 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.087 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.668 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      56998.474   ± 1088.281  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      24386.963    ± 407.452  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      38459.446    ± 109.106  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      45146.229    ± 419.430  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      27811.093    ± 165.976  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      26927.801    ± 322.767  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62840.149   ± 1063.369  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  672103434.970 ± 13131924.551  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  370603683.278 ± 373512.016  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      78223.814    ± 933.612  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  137298381.911 ± 3338758.023  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68547151.300 ± 160587.541  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66104.583    ± 697.647  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7740.175     ± 45.139  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7945.268     ± 18.454  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7693.149    ± 182.845  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       8231.128    ± 766.204  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        978.529      ± 9.243  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6441.211   ± 1050.169  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        911.305     ± 26.645  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9658.926    ± 184.984  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17563.594     ± 51.724  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7117.814     ± 65.092  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3674.650    ± 291.175  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5804.555    ± 225.566  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      40677.291    ± 301.247  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      40698.344    ± 438.079  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     711765.692  ± 15868.939  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     709116.429  ± 24635.464  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     763981.484  ± 11027.460  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     785320.749  ± 11765.580  ops/s
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

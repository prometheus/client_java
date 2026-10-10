# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-10T04:31:10Z
- **Commit:** [`d8089b2`](https://github.com/prometheus/client_java/commit/d8089b219004512f136a5e3d6153df74ddfbe748)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 672.71M | ± 4282.61K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 369.97M | ± 688.87K | ops/s |
| prometheusLabelValuesInc | 141.04M | ± 1489.17K | ops/s |
| prometheusLabelValuesIncSingleThread | 67.73M | ± 1070.62K | ops/s |
| prometheusInc | 73.57K | ± 4.17K | ops/s |
| prometheusNoLabelsInc | 64.57K | ± 2.40K | ops/s |
| prometheusAdd | 62.07K | ± 634.87 | ops/s |
| codahaleIncNoLabels | 56.48K | ± 513.36 | ops/s |
| openTelemetryBoundInc | 44.99K | ± 388.76 | ops/s |
| openTelemetryBoundAdd | 39.18K | ± 538.09 | ops/s |
| openTelemetryIncNoLabels | 27.81K | ± 758.47 | ops/s |
| openTelemetryInc | 21.53K | ± 36.44 | ops/s |
| openTelemetryAdd | 18.95K | ± 133.38 | ops/s |
| simpleclientInc | 7.91K | ± 172.31 | ops/s |
| simpleclientNoLabelsInc | 7.52K | ± 89.97 | ops/s |
| simpleclientAdd | 7.48K | ± 246.86 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.25K | ± 133.97 | ops/s |
| prometheusClassic | 8.98K | ± 3.09K | ops/s |
| openTelemetryBoundClassic | 8.44K | ± 2.10K | ops/s |
| prometheusClassicSingleThread | 7.10K | ± 149.31 | ops/s |
| simpleclient | 5.80K | ± 82.82 | ops/s |
| openTelemetryClassic | 4.94K | ± 158.41 | ops/s |
| prometheusNative | 3.54K | ± 84.82 | ops/s |
| openTelemetryBoundExponential | 969.76 | ± 13.77 | ops/s |
| openTelemetryExponential | 818.02 | ± 62.86 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 35.13K | ± 324.42 | ops/s |
| openMetricsWriteToNull | 34.88K | ± 235.86 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 729.67K | ± 12.98K | ops/s |
| prometheusWriteToByteArray | 713.71K | ± 7.57K | ops/s |
| openMetricsWriteToByteArray | 699.14K | ± 4.32K | ops/s |
| openMetricsWriteToNull | 694.37K | ± 10.35K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.017 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.043 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.034 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.050 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.057 | — | — |
| CounterBenchmark.simpleclientAdd | 0.125 | — | — |
| CounterBenchmark.simpleclientInc | 0.119 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.124 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.117 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.966 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.190 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.153 | — | — |
| HistogramBenchmark.prometheusClassic | 0.460 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.468 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.411 | — | — |
| HistogramBenchmark.prometheusNative | 417713.046 | — | — |
| HistogramBenchmark.simpleclient | 0.161 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.100 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.099 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      56484.983    ± 513.356  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      18952.638    ± 133.379  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      39178.538    ± 538.095  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      44993.760    ± 388.756  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21531.460     ± 36.440  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      27811.634    ± 758.471  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62069.618    ± 634.868  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  672712439.465 ± 4282606.276  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  369974291.782 ± 688872.386  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      73572.966   ± 4171.064  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  141035141.446 ± 1489174.063  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   67731320.682 ± 1070620.727  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      64570.799   ± 2400.512  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7482.302    ± 246.863  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7905.851    ± 172.308  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7521.091     ± 89.973  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       8440.480   ± 2095.614  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        969.760     ± 13.768  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4942.504    ± 158.408  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        818.017     ± 62.855  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8975.042   ± 3087.222  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17254.050    ± 133.974  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7100.708    ± 149.314  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3537.197     ± 84.817  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5798.699     ± 82.818  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      34883.639    ± 235.859  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35134.074    ± 324.421  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     699141.614   ± 4317.296  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     694365.016  ± 10346.368  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     713710.326   ± 7565.141  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     729665.392  ± 12982.576  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-23T04:25:21Z
- **Commit:** [`5159d5d`](https://github.com/prometheus/client_java/commit/5159d5d7a2095e0454079b2dd362d3a99a42c524)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 562.41M | ± 4999.05K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.89M | ± 402.06K | ops/s |
| prometheusLabelValuesInc | 116.29M | ± 1270.51K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.27M | ± 416.08K | ops/s |
| prometheusInc | 65.79K | ± 467.19 | ops/s |
| prometheusNoLabelsInc | 56.22K | ± 1.27K | ops/s |
| prometheusAdd | 50.38K | ± 1.70K | ops/s |
| codahaleIncNoLabels | 48.37K | ± 1.40K | ops/s |
| openTelemetryBoundInc | 38.05K | ± 347.60 | ops/s |
| openTelemetryBoundAdd | 31.90K | ± 331.05 | ops/s |
| openTelemetryIncNoLabels | 21.70K | ± 162.77 | ops/s |
| openTelemetryInc | 17.97K | ± 109.93 | ops/s |
| openTelemetryAdd | 15.57K | ± 24.55 | ops/s |
| simpleclientInc | 6.56K | ± 40.36 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 37.19 | ops/s |
| simpleclientAdd | 5.95K | ± 425.66 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.07K | ± 41.70 | ops/s |
| prometheusClassic | 7.31K | ± 1.14K | ops/s |
| openTelemetryBoundClassic | 7.03K | ± 1.72K | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 19.32 | ops/s |
| simpleclient | 4.43K | ± 6.97 | ops/s |
| openTelemetryClassic | 4.07K | ± 215.28 | ops/s |
| prometheusNative | 2.86K | ± 333.86 | ops/s |
| openTelemetryBoundExponential | 1.09K | ± 19.48 | ops/s |
| openTelemetryExponential | 894.40 | ± 70.78 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 23.94K | ± 482.56 | ops/s |
| prometheusWriteToNull | 23.86K | ± 1.15K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 556.89K | ± 3.77K | ops/s |
| prometheusWriteToByteArray | 546.94K | ± 4.82K | ops/s |
| openMetricsWriteToNull | 529.41K | ± 6.41K | ops/s |
| openMetricsWriteToByteArray | 524.75K | ± 4.28K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.024 | — | — |
| CounterBenchmark.openTelemetryInc | 0.052 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.157 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.146 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.139 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.853 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.232 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.046 | — | — |
| HistogramBenchmark.prometheusClassic | 0.513 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.674 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.641 | — | — |
| HistogramBenchmark.prometheusNative | 335793.320 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.146 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.147 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48369.127   ± 1402.335  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15573.010     ± 24.552  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31895.925    ± 331.050  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      38049.115    ± 347.600  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17969.835    ± 109.935  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21700.692    ± 162.774  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50383.176   ± 1696.511  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  562406483.975 ± 4999045.284  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334890256.462 ± 402064.631  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65787.021    ± 467.190  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116291717.525 ± 1270510.488  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58265389.662 ± 416075.416  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56217.747   ± 1265.319  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5951.778    ± 425.665  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6559.809     ± 40.357  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6354.924     ± 37.188  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       7034.728   ± 1715.616  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1089.726     ± 19.482  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4067.323    ± 215.277  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        894.398     ± 70.776  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7311.598   ± 1137.151  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12073.449     ± 41.696  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4542.385     ± 19.319  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2860.380    ± 333.857  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4433.138      ± 6.971  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23936.486    ± 482.556  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23856.308   ± 1154.731  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     524753.289   ± 4276.195  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     529406.527   ± 6407.671  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     546938.534   ± 4815.572  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     556885.397   ± 3773.129  ops/s
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

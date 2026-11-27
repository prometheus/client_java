# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-27T04:15:57Z
- **Commit:** [`ffabc10`](https://github.com/prometheus/client_java/commit/ffabc1000eaec178e027b95dcfc42559fc871404)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 514.96M | ± 13307.36K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 287.28M | ± 348.88K | ops/s |
| prometheusLabelValuesInc | 106.42M | ± 1966.14K | ops/s |
| prometheusLabelValuesIncSingleThread | 53.00M | ± 154.05K | ops/s |
| prometheusInc | 58.50K | ± 2.04K | ops/s |
| prometheusNoLabelsInc | 51.07K | ± 476.45 | ops/s |
| prometheusAdd | 48.36K | ± 1.04K | ops/s |
| codahaleIncNoLabels | 43.91K | ± 2.31K | ops/s |
| openTelemetryBoundInc | 35.31K | ± 113.97 | ops/s |
| openTelemetryBoundAdd | 29.63K | ± 196.89 | ops/s |
| openTelemetryInc | 21.41K | ± 186.86 | ops/s |
| openTelemetryIncNoLabels | 21.21K | ± 606.66 | ops/s |
| openTelemetryAdd | 18.86K | ± 338.01 | ops/s |
| simpleclientInc | 6.02K | ± 87.63 | ops/s |
| simpleclientNoLabelsInc | 5.86K | ± 19.95 | ops/s |
| simpleclientAdd | 5.85K | ± 250.37 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.58K | ± 53.43 | ops/s |
| prometheusClassic | 6.95K | ± 1.69K | ops/s |
| prometheusClassicSingleThread | 5.57K | ± 7.71 | ops/s |
| openTelemetryClassic | 4.96K | ± 663.95 | ops/s |
| simpleclient | 4.48K | ± 72.94 | ops/s |
| openTelemetryBoundClassic | 3.74K | ± 1.85K | ops/s |
| prometheusNative | 2.91K | ± 176.84 | ops/s |
| openTelemetryBoundExponential | 695.06 | ± 60.18 | ops/s |
| openTelemetryExponential | 646.16 | ± 40.71 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 32.04K | ± 193.74 | ops/s |
| openMetricsWriteToNull | 31.57K | ± 127.02 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 660.21K | ± 12.63K | ops/s |
| prometheusWriteToByteArray | 646.90K | ± 24.98K | ops/s |
| openMetricsWriteToNull | 610.80K | ± 7.21K | ops/s |
| openMetricsWriteToByteArray | 590.48K | ± 6.03K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.021 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.050 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.032 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.027 | — | — |
| CounterBenchmark.openTelemetryInc | 0.044 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.044 | — | — |
| CounterBenchmark.prometheusAdd | 0.077 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.063 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.072 | — | — |
| CounterBenchmark.simpleclientAdd | 0.160 | — | — |
| CounterBenchmark.simpleclientInc | 0.156 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.160 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.299 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.356 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.194 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.461 | — | — |
| HistogramBenchmark.prometheusClassic | 0.553 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.598 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.524 | — | — |
| HistogramBenchmark.prometheusNative | 417713.276 | — | — |
| HistogramBenchmark.simpleclient | 0.209 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.112 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.110 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43906.260   ± 2313.589  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      18860.077    ± 338.014  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      29629.515    ± 196.887  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      35307.766    ± 113.969  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21414.629    ± 186.865  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21205.209    ± 606.662  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48356.710   ± 1043.280  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  514963785.160 ± 13307364.068  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  287277244.294 ± 348875.543  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58502.423   ± 2044.852  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  106420364.125 ± 1966140.340  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   53004098.650 ± 154049.886  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51074.563    ± 476.448  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5853.811    ± 250.371  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6024.998     ± 87.625  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5857.932     ± 19.954  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3743.375   ± 1850.889  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        695.063     ± 60.175  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4957.793    ± 663.953  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        646.157     ± 40.713  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6952.904   ± 1691.538  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13581.281     ± 53.429  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5570.412      ± 7.711  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2914.959    ± 176.837  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4480.807     ± 72.936  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31565.842    ± 127.017  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      32043.949    ± 193.740  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     590484.366   ± 6034.006  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     610803.234   ± 7212.678  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     646899.318  ± 24983.393  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     660205.835  ± 12626.053  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-28T04:14:10Z
- **Commit:** [`2272f18`](https://github.com/prometheus/client_java/commit/2272f18aeb1dbf446e488cf4803766e7dca74122)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 556.61M | ± 7945.88K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.67M | ± 568.10K | ops/s |
| prometheusLabelValuesInc | 117.45M | ± 2994.09K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.17M | ± 506.43K | ops/s |
| prometheusInc | 66.40K | ± 1.20K | ops/s |
| prometheusNoLabelsInc | 56.57K | ± 1.08K | ops/s |
| prometheusAdd | 51.01K | ± 795.12 | ops/s |
| codahaleIncNoLabels | 48.04K | ± 1.87K | ops/s |
| openTelemetryBoundInc | 37.71K | ± 650.89 | ops/s |
| openTelemetryBoundAdd | 32.22K | ± 453.46 | ops/s |
| openTelemetryIncNoLabels | 22.63K | ± 1.16K | ops/s |
| openTelemetryInc | 21.79K | ± 2.09K | ops/s |
| openTelemetryAdd | 19.41K | ± 301.87 | ops/s |
| simpleclientInc | 6.60K | ± 125.06 | ops/s |
| simpleclientNoLabelsInc | 6.32K | ± 117.39 | ops/s |
| simpleclientAdd | 6.17K | ± 286.33 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.01K | ± 147.17 | ops/s |
| prometheusClassic | 6.76K | ± 1.25K | ops/s |
| openTelemetryClassic | 4.64K | ± 1.02K | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 14.97 | ops/s |
| simpleclient | 4.42K | ± 91.77 | ops/s |
| prometheusNative | 3.05K | ± 102.63 | ops/s |
| openTelemetryBoundClassic | 2.63K | ± 243.04 | ops/s |
| openTelemetryBoundExponential | 1.08K | ± 34.51 | ops/s |
| openTelemetryExponential | 996.56 | ± 64.98 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 30.24K | ± 1.93K | ops/s |
| openMetricsWriteToNull | 29.97K | ± 646.17 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 507.03K | ± 4.41K | ops/s |
| openMetricsWriteToNull | 494.45K | ± 4.03K | ops/s |
| prometheusWriteToByteArray | 491.80K | ± 29.56K | ops/s |
| openMetricsWriteToByteArray | 489.91K | ± 4.50K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.020 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.043 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.055 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.359 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.870 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.212 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.947 | — | — |
| HistogramBenchmark.prometheusClassic | 0.560 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.681 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.647 | — | — |
| HistogramBenchmark.prometheusNative | 417713.226 | — | — |
| HistogramBenchmark.simpleclient | 0.215 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.118 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.117 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48044.073   ± 1868.423  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19408.547    ± 301.874  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      32218.840    ± 453.457  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37712.240    ± 650.892  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21788.830   ± 2087.142  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22627.902   ± 1159.066  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51013.512    ± 795.122  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  556610585.575 ± 7945884.877  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334674581.562 ± 568103.056  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66401.327   ± 1199.732  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  117452686.027 ± 2994089.496  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58173033.317 ± 506428.049  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56571.372   ± 1077.432  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6172.454    ± 286.329  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6597.561    ± 125.060  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6319.948    ± 117.385  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2634.275    ± 243.038  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1084.893     ± 34.514  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4638.619   ± 1021.677  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        996.562     ± 64.975  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6762.152   ± 1249.185  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12014.256    ± 147.171  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4513.942     ± 14.969  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3051.549    ± 102.626  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4417.745     ± 91.771  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29966.893    ± 646.173  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30239.033   ± 1934.638  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     489911.974   ± 4501.839  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     494447.619   ± 4027.454  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491803.150  ± 29557.160  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507028.629   ± 4414.896  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-25T04:15:51Z
- **Commit:** [`9dba0e1`](https://github.com/prometheus/client_java/commit/9dba0e1c6af9eadca2db23ecbdcb7fb151c11cdb)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 729.85M | ± 22013.49K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 221.88M | ± 1560.46K | ops/s |
| prometheusLabelValuesInc | 208.69M | ± 2376.97K | ops/s |
| prometheusLabelValuesIncSingleThread | 98.43M | ± 1095.25K | ops/s |
| prometheusInc | 68.20K | ± 800.13 | ops/s |
| prometheusNoLabelsInc | 66.20K | ± 1.62K | ops/s |
| codahaleIncNoLabels | 63.31K | ± 4.26K | ops/s |
| prometheusAdd | 56.87K | ± 837.18 | ops/s |
| openTelemetryBoundInc | 52.83K | ± 576.29 | ops/s |
| openTelemetryBoundAdd | 48.39K | ± 695.03 | ops/s |
| openTelemetryIncNoLabels | 35.48K | ± 466.95 | ops/s |
| openTelemetryInc | 34.61K | ± 1.18K | ops/s |
| openTelemetryAdd | 27.89K | ± 741.10 | ops/s |
| simpleclientNoLabelsInc | 11.11K | ± 226.93 | ops/s |
| simpleclientInc | 10.76K | ± 264.95 | ops/s |
| simpleclientAdd | 10.31K | ± 264.15 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.48K | ± 96.41 | ops/s |
| prometheusClassic | 10.08K | ± 1.12K | ops/s |
| simpleclient | 6.95K | ± 215.38 | ops/s |
| openTelemetryClassic | 6.07K | ± 1.55K | ops/s |
| openTelemetryBoundClassic | 5.54K | ± 2.24K | ops/s |
| prometheusNative | 4.58K | ± 503.19 | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 59.99 | ops/s |
| openTelemetryBoundExponential | 1.16K | ± 97.13 | ops/s |
| openTelemetryExponential | 1.01K | ± 20.11 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 36.11K | ± 409.64 | ops/s |
| prometheusWriteToNull | 35.13K | ± 882.61 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 837.48K | ± 78.73K | ops/s |
| prometheusWriteToByteArray | 788.73K | ± 30.53K | ops/s |
| openMetricsWriteToByteArray | 768.47K | ± 33.93K | ops/s |
| openMetricsWriteToNull | 707.73K | ± 44.85K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.034 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.019 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.018 | — | — |
| CounterBenchmark.openTelemetryInc | 0.027 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.027 | — | — |
| CounterBenchmark.prometheusAdd | 0.065 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.054 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.092 | — | — |
| CounterBenchmark.simpleclientInc | 0.088 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.085 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.195 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.820 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.164 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.934 | — | — |
| HistogramBenchmark.prometheusClassic | 0.371 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.530 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.617 | — | — |
| HistogramBenchmark.prometheusNative | 417712.835 | — | — |
| HistogramBenchmark.simpleclient | 0.136 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.098 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.100 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.668 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      63312.427   ± 4259.275  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      27892.001    ± 741.105  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      48391.100    ± 695.028  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      52828.554    ± 576.293  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      34609.125   ± 1180.575  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      35476.454    ± 466.955  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      56867.449    ± 837.182  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  729848318.805 ± 22013490.757  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  221883924.950 ± 1560457.272  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      68196.315    ± 800.129  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  208690253.569 ± 2376966.874  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   98432584.412 ± 1095251.657  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66197.400   ± 1616.423  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10309.816    ± 264.150  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10762.700    ± 264.952  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      11114.330    ± 226.935  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5535.552   ± 2240.076  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1159.314     ± 97.127  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6072.015   ± 1551.011  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1012.715     ± 20.111  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15      10083.489   ± 1117.485  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15481.551     ± 96.409  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4507.898     ± 59.990  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4582.211    ± 503.190  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       6945.116    ± 215.377  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      36111.528    ± 409.640  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35130.768    ± 882.608  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     768470.288  ± 33930.533  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     707734.149  ± 44846.551  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     788727.149  ± 30528.701  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     837482.171  ± 78728.304  ops/s
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

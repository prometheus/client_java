# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-10T04:14:01Z
- **Commit:** [`6b84b7c`](https://github.com/prometheus/client_java/commit/6b84b7c1ba0fa6eec7463c75b184a996050d72d1)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 724.28M | ± 28567.02K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 222.81M | ± 767.95K | ops/s |
| prometheusLabelValuesInc | 210.36M | ± 2344.31K | ops/s |
| prometheusLabelValuesIncSingleThread | 98.46M | ± 787.00K | ops/s |
| prometheusInc | 69.63K | ± 228.18 | ops/s |
| prometheusNoLabelsInc | 67.58K | ± 637.44 | ops/s |
| codahaleIncNoLabels | 62.72K | ± 643.46 | ops/s |
| prometheusAdd | 61.49K | ± 3.27K | ops/s |
| openTelemetryBoundInc | 54.58K | ± 214.82 | ops/s |
| openTelemetryBoundAdd | 48.71K | ± 511.36 | ops/s |
| openTelemetryIncNoLabels | 36.26K | ± 839.19 | ops/s |
| openTelemetryInc | 34.42K | ± 2.40K | ops/s |
| openTelemetryAdd | 29.61K | ± 2.23K | ops/s |
| simpleclientNoLabelsInc | 11.22K | ± 41.03 | ops/s |
| simpleclientInc | 11.13K | ± 200.66 | ops/s |
| simpleclientAdd | 10.73K | ± 439.17 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.35K | ± 153.32 | ops/s |
| prometheusClassic | 10.42K | ± 1.36K | ops/s |
| simpleclient | 6.97K | ± 105.71 | ops/s |
| openTelemetryClassic | 6.96K | ± 1.81K | ops/s |
| openTelemetryBoundClassic | 4.55K | ± 615.43 | ops/s |
| prometheusClassicSingleThread | 4.45K | ± 70.24 | ops/s |
| prometheusNative | 4.12K | ± 65.36 | ops/s |
| openTelemetryBoundExponential | 1.16K | ± 64.88 | ops/s |
| openTelemetryExponential | 1.10K | ± 37.38 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 36.47K | ± 368.12 | ops/s |
| openMetricsWriteToNull | 36.25K | ± 481.13 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 838.51K | ± 55.28K | ops/s |
| prometheusWriteToNull | 782.75K | ± 52.51K | ops/s |
| openMetricsWriteToByteArray | 724.69K | ± 10.32K | ops/s |
| openMetricsWriteToNull | 710.75K | ± 25.50K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.032 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.019 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.017 | — | — |
| CounterBenchmark.openTelemetryInc | 0.028 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.026 | — | — |
| CounterBenchmark.prometheusAdd | 0.060 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.053 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.055 | — | — |
| CounterBenchmark.simpleclientAdd | 0.088 | — | — |
| CounterBenchmark.simpleclientInc | 0.085 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.085 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.211 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.815 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.146 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.858 | — | — |
| HistogramBenchmark.prometheusClassic | 0.361 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.528 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.609 | — | — |
| HistogramBenchmark.prometheusNative | 417712.897 | — | — |
| HistogramBenchmark.simpleclient | 0.136 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.097 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.097 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      62720.260    ± 643.461  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      29607.141   ± 2230.326  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      48706.972    ± 511.363  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      54582.508    ± 214.824  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      34416.515   ± 2395.177  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      36264.538    ± 839.188  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      61487.727   ± 3273.238  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  724280036.153 ± 28567019.577  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  222808665.210 ± 767952.660  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      69629.303    ± 228.179  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  210359327.547 ± 2344306.201  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   98457421.252 ± 786998.362  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      67583.053    ± 637.436  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10733.889    ± 439.170  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      11133.766    ± 200.659  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      11223.486     ± 41.035  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4549.940    ± 615.429  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1163.032     ± 64.877  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6961.662   ± 1809.979  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1101.311     ± 37.382  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15      10424.123   ± 1361.964  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15354.816    ± 153.319  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4447.428     ± 70.240  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4123.761     ± 65.363  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       6966.799    ± 105.708  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      36252.116    ± 481.131  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      36470.563    ± 368.115  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     724691.143  ± 10317.489  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     710748.200  ± 25504.629  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     838509.445  ± 55275.053  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     782752.837  ± 52514.567  ops/s
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

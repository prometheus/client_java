# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-25T04:24:08Z
- **Commit:** [`533375f`](https://github.com/prometheus/client_java/commit/533375f65bc9130d9ea88ca139784bfb966b04f7)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 700.16M | ± 22600.71K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 217.43M | ± 4723.11K | ops/s |
| prometheusLabelValuesInc | 190.55M | ± 3926.48K | ops/s |
| prometheusLabelValuesIncSingleThread | 92.31M | ± 2384.77K | ops/s |
| prometheusInc | 67.88K | ± 1.97K | ops/s |
| prometheusNoLabelsInc | 64.83K | ± 1.05K | ops/s |
| codahaleIncNoLabels | 64.02K | ± 2.79K | ops/s |
| prometheusAdd | 58.92K | ± 3.39K | ops/s |
| openTelemetryBoundInc | 54.06K | ± 219.83 | ops/s |
| openTelemetryBoundAdd | 47.92K | ± 849.13 | ops/s |
| openTelemetryIncNoLabels | 35.97K | ± 358.96 | ops/s |
| openTelemetryInc | 32.52K | ± 2.62K | ops/s |
| openTelemetryAdd | 28.51K | ± 1.96K | ops/s |
| simpleclientInc | 10.91K | ± 596.63 | ops/s |
| simpleclientAdd | 10.45K | ± 75.55 | ops/s |
| simpleclientNoLabelsInc | 10.40K | ± 123.86 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 14.67K | ± 200.04 | ops/s |
| prometheusClassic | 9.36K | ± 752.18 | ops/s |
| simpleclient | 7.11K | ± 138.07 | ops/s |
| openTelemetryClassic | 5.57K | ± 767.53 | ops/s |
| openTelemetryBoundClassic | 5.10K | ± 2.32K | ops/s |
| prometheusNative | 4.49K | ± 371.00 | ops/s |
| prometheusClassicSingleThread | 4.34K | ± 17.66 | ops/s |
| openTelemetryBoundExponential | 1.07K | ± 22.25 | ops/s |
| openTelemetryExponential | 1.02K | ± 34.84 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 35.49K | ± 716.42 | ops/s |
| prometheusWriteToNull | 34.26K | ± 806.40 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 753.09K | ± 53.21K | ops/s |
| prometheusWriteToNull | 731.23K | ± 26.17K | ops/s |
| openMetricsWriteToByteArray | 683.20K | ± 34.94K | ops/s |
| openMetricsWriteToNull | 680.76K | ± 27.56K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.033 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.020 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.017 | — | — |
| CounterBenchmark.openTelemetryInc | 0.029 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.026 | — | — |
| CounterBenchmark.prometheusAdd | 0.063 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.055 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.057 | — | — |
| CounterBenchmark.simpleclientAdd | 0.090 | — | — |
| CounterBenchmark.simpleclientInc | 0.086 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.091 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.213 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.884 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.173 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.926 | — | — |
| HistogramBenchmark.prometheusClassic | 0.399 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.545 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.633 | — | — |
| HistogramBenchmark.prometheusNative | 417712.843 | — | — |
| HistogramBenchmark.simpleclient | 0.134 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.099 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.103 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      64017.981   ± 2791.859  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      28512.450   ± 1958.829  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      47918.908    ± 849.127  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      54056.560    ± 219.825  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      32517.784   ± 2620.345  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      35969.716    ± 358.962  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      58922.724   ± 3394.432  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  700164011.941 ± 22600712.429  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  217427587.936 ± 4723105.163  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67881.057   ± 1968.471  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  190547798.485 ± 3926476.673  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   92309573.896 ± 2384766.335  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      64826.079   ± 1050.809  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10447.985     ± 75.549  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10912.769    ± 596.627  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      10403.021    ± 123.859  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5099.767   ± 2320.669  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1071.076     ± 22.253  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5565.538    ± 767.526  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1022.266     ± 34.839  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9356.253    ± 752.182  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14672.194    ± 200.043  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4344.183     ± 17.658  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4485.447    ± 371.002  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       7108.257    ± 138.074  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35494.553    ± 716.424  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      34257.981    ± 806.400  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     683201.646  ± 34935.855  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     680762.299  ± 27557.960  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     753090.993  ± 53205.978  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     731230.805  ± 26172.231  ops/s
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

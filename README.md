# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-27T04:28:16Z
- **Commit:** [`cce26b8`](https://github.com/prometheus/client_java/commit/cce26b87ccc0d8dbb83e8532ce7adba8f26c5372)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 558.70M | ± 7794.75K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.73M | ± 498.38K | ops/s |
| prometheusLabelValuesInc | 116.40M | ± 1175.75K | ops/s |
| prometheusLabelValuesIncSingleThread | 59.01M | ± 569.76K | ops/s |
| prometheusInc | 62.65K | ± 3.48K | ops/s |
| prometheusNoLabelsInc | 56.13K | ± 945.22 | ops/s |
| prometheusAdd | 50.41K | ± 1.55K | ops/s |
| codahaleIncNoLabels | 49.20K | ± 1.50K | ops/s |
| openTelemetryBoundInc | 38.02K | ± 128.06 | ops/s |
| openTelemetryBoundAdd | 30.18K | ± 2.04K | ops/s |
| openTelemetryIncNoLabels | 22.61K | ± 1.25K | ops/s |
| openTelemetryInc | 18.13K | ± 146.73 | ops/s |
| openTelemetryAdd | 15.59K | ± 23.74 | ops/s |
| simpleclientInc | 6.55K | ± 31.66 | ops/s |
| simpleclientNoLabelsInc | 6.20K | ± 194.77 | ops/s |
| simpleclientAdd | 6.20K | ± 387.05 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.07K | ± 38.79 | ops/s |
| prometheusClassic | 7.20K | ± 1.55K | ops/s |
| openTelemetryBoundClassic | 6.57K | ± 2.09K | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 18.30 | ops/s |
| simpleclient | 4.36K | ± 18.74 | ops/s |
| openTelemetryClassic | 4.20K | ± 772.06 | ops/s |
| prometheusNative | 2.84K | ± 187.69 | ops/s |
| openTelemetryBoundExponential | 973.74 | ± 54.87 | ops/s |
| openTelemetryExponential | 827.11 | ± 23.90 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.99K | ± 523.39 | ops/s |
| openMetricsWriteToNull | 23.82K | ± 1.14K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 567.39K | ± 3.95K | ops/s |
| prometheusWriteToByteArray | 549.19K | ± 6.70K | ops/s |
| openMetricsWriteToNull | 529.94K | ± 2.65K | ops/s |
| openMetricsWriteToByteArray | 509.87K | ± 5.82K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.059 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.031 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.024 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.059 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.150 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.154 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.954 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.227 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.121 | — | — |
| HistogramBenchmark.prometheusClassic | 0.528 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.665 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.641 | — | — |
| HistogramBenchmark.prometheusNative | 417713.301 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.147 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.146 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18442.668 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49204.873   ± 1501.513  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15587.555     ± 23.742  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      30176.045   ± 2036.399  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      38021.973    ± 128.056  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18127.553    ± 146.730  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22608.978   ± 1250.064  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50405.798   ± 1549.470  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  558702750.829 ± 7794747.334  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334726704.483 ± 498376.441  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62645.260   ± 3477.029  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116404302.848 ± 1175751.941  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   59009682.158 ± 569760.981  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56129.948    ± 945.219  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6201.889    ± 387.048  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6552.934     ± 31.662  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6204.545    ± 194.771  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6571.119   ± 2091.960  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        973.737     ± 54.865  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4199.450    ± 772.063  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        827.111     ± 23.899  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7203.334   ± 1549.075  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12072.380     ± 38.794  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4542.090     ± 18.305  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2835.297    ± 187.692  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4362.954     ± 18.744  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23821.747   ± 1140.811  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23989.451    ± 523.391  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     509869.406   ± 5818.375  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     529941.374   ± 2650.996  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     549188.995   ± 6699.110  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     567388.773   ± 3948.313  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-08T04:14:25Z
- **Commit:** [`85d9fb0`](https://github.com/prometheus/client_java/commit/85d9fb0dca9f064c746470e91b924d253abf8d30)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 554.46M | ± 12028.02K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 335.08M | ± 339.05K | ops/s |
| prometheusLabelValuesInc | 116.46M | ± 2509.79K | ops/s |
| prometheusLabelValuesIncSingleThread | 59.01M | ± 368.31K | ops/s |
| prometheusInc | 66.25K | ± 661.79 | ops/s |
| prometheusNoLabelsInc | 56.82K | ± 852.95 | ops/s |
| prometheusAdd | 50.60K | ± 1.03K | ops/s |
| codahaleIncNoLabels | 44.15K | ± 7.13K | ops/s |
| openTelemetryBoundInc | 37.83K | ± 836.75 | ops/s |
| openTelemetryBoundAdd | 32.21K | ± 534.02 | ops/s |
| openTelemetryInc | 22.98K | ± 418.10 | ops/s |
| openTelemetryIncNoLabels | 21.21K | ± 876.23 | ops/s |
| openTelemetryAdd | 17.51K | ± 2.77K | ops/s |
| simpleclientInc | 6.61K | ± 88.38 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 105.66 | ops/s |
| simpleclientAdd | 6.26K | ± 227.34 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.01K | ± 185.26 | ops/s |
| prometheusClassic | 7.13K | ± 1.16K | ops/s |
| openTelemetryClassic | 5.30K | ± 939.13 | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 12.58 | ops/s |
| simpleclient | 4.42K | ± 69.75 | ops/s |
| openTelemetryBoundClassic | 4.13K | ± 1.76K | ops/s |
| prometheusNative | 2.60K | ± 227.22 | ops/s |
| openTelemetryExponential | 997.27 | ± 36.14 | ops/s |
| openTelemetryBoundExponential | 995.71 | ± 53.20 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 31.29K | ± 631.87 | ops/s |
| openMetricsWriteToNull | 29.76K | ± 411.86 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 517.87K | ± 6.24K | ops/s |
| prometheusWriteToByteArray | 510.04K | ± 9.62K | ops/s |
| openMetricsWriteToNull | 507.12K | ± 6.15K | ops/s |
| openMetricsWriteToByteArray | 506.18K | ± 11.86K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.022 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.055 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.044 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.150 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.255 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.944 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.182 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.942 | — | — |
| HistogramBenchmark.prometheusClassic | 0.527 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.692 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.643 | — | — |
| HistogramBenchmark.prometheusNative | 417713.436 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.119 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.113 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18496.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44153.988   ± 7127.583  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      17514.413   ± 2766.180  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      32209.570    ± 534.020  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37830.956    ± 836.751  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22981.800    ± 418.096  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21208.187    ± 876.229  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50600.860   ± 1034.170  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  554458783.904 ± 12028024.558  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  335076369.849 ± 339051.754  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66245.591    ± 661.787  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116455155.106 ± 2509785.893  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   59010690.800 ± 368305.829  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56821.939    ± 852.949  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6262.338    ± 227.337  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6612.567     ± 88.379  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6352.307    ± 105.661  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4125.312   ± 1761.305  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        995.709     ± 53.204  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5295.625    ± 939.133  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        997.274     ± 36.142  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7131.986   ± 1155.556  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12005.338    ± 185.257  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4535.598     ± 12.579  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2604.511    ± 227.224  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4421.516     ± 69.754  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29758.624    ± 411.862  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      31286.505    ± 631.869  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     506176.483  ± 11860.760  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     507115.962   ± 6152.070  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     510035.843   ± 9618.720  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     517869.951   ± 6242.852  ops/s
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

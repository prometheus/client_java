# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-04T04:13:32Z
- **Commit:** [`7e30a7e`](https://github.com/prometheus/client_java/commit/7e30a7e29451d448c20bdc974396126af7ac7ff3)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 297.36M | ± 9048.58K | ops/s |
| prometheusLabelValuesInc | 146.97M | ± 458.64K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 110.28M | ± 209.16K | ops/s |
| prometheusLabelValuesIncSingleThread | 70.81M | ± 391.62K | ops/s |
| prometheusInc | 31.39K | ± 951.13 | ops/s |
| codahaleIncNoLabels | 31.34K | ± 996.88 | ops/s |
| prometheusNoLabelsInc | 30.71K | ± 94.51 | ops/s |
| openTelemetryBoundInc | 30.23K | ± 404.68 | ops/s |
| prometheusAdd | 29.64K | ± 191.58 | ops/s |
| openTelemetryBoundAdd | 27.71K | ± 243.98 | ops/s |
| openTelemetryIncNoLabels | 24.99K | ± 106.86 | ops/s |
| openTelemetryInc | 23.26K | ± 605.92 | ops/s |
| openTelemetryAdd | 19.73K | ± 1.05K | ops/s |
| simpleclientInc | 7.49K | ± 191.58 | ops/s |
| simpleclientNoLabelsInc | 7.47K | ± 106.07 | ops/s |
| simpleclientAdd | 7.41K | ± 37.01 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.60K | ± 35.09 | ops/s |
| simpleclient | 4.71K | ± 100.00 | ops/s |
| prometheusClassic | 3.82K | ± 1.23K | ops/s |
| prometheusClassicSingleThread | 3.32K | ± 84.54 | ops/s |
| openTelemetryClassic | 3.05K | ± 831.24 | ops/s |
| openTelemetryBoundClassic | 2.51K | ± 170.51 | ops/s |
| prometheusNative | 1.93K | ± 111.01 | ops/s |
| openTelemetryBoundExponential | 818.22 | ± 114.25 | ops/s |
| openTelemetryExponential | 604.35 | ± 14.50 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 21.57K | ± 177.69 | ops/s |
| prometheusWriteToNull | 21.29K | ± 304.21 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 339.25K | ± 9.07K | ops/s |
| prometheusWriteToByteArray | 328.94K | ± 2.84K | ops/s |
| openMetricsWriteToNull | 310.82K | ± 2.82K | ops/s |
| openMetricsWriteToByteArray | 307.92K | ± 3.83K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.030 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.034 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.031 | — | — |
| CounterBenchmark.openTelemetryInc | 0.040 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.037 | — | — |
| CounterBenchmark.prometheusAdd | 0.125 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.118 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.121 | — | — |
| CounterBenchmark.simpleclientAdd | 0.126 | — | — |
| CounterBenchmark.simpleclientInc | 0.126 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.125 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.376 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.167 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.333 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.553 | — | — |
| HistogramBenchmark.prometheusClassic | 1.033 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.076 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.838 | — | — |
| HistogramBenchmark.prometheusNative | 417713.929 | — | — |
| HistogramBenchmark.simpleclient | 0.201 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.163 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.166 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      31335.589    ± 996.881  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19729.455   ± 1054.224  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      27711.311    ± 243.984  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      30229.244    ± 404.681  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23264.737    ± 605.919  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      24991.359    ± 106.859  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      29636.928    ± 191.576  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  297359962.648 ± 9048582.995  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  110278164.366 ± 209158.715  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31387.541    ± 951.132  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  146967005.100 ± 458635.320  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   70808288.147 ± 391621.488  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30711.619     ± 94.511  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7412.829     ± 37.013  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7489.425    ± 191.579  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7471.335    ± 106.066  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2507.457    ± 170.506  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        818.219    ± 114.252  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3046.337    ± 831.245  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        604.350     ± 14.496  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3816.598   ± 1226.231  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7600.605     ± 35.092  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3316.537     ± 84.536  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1933.372    ± 111.010  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4708.766     ± 99.995  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      21572.822    ± 177.688  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      21285.550    ± 304.212  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     307922.075   ± 3828.484  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     310819.901   ± 2815.516  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     328938.513   ± 2841.815  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     339246.907   ± 9066.068  ops/s
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

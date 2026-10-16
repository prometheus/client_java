# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-16T04:23:17Z
- **Commit:** [`b1aa8b3`](https://github.com/prometheus/client_java/commit/b1aa8b38be304b7c935da77014afb02267ed01a6)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 255.80M | ± 17491.60K | ops/s |
| prometheusLabelValuesInc | 122.64M | ± 869.85K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 92.54M | ± 140.48K | ops/s |
| prometheusLabelValuesIncSingleThread | 59.91M | ± 215.44K | ops/s |
| prometheusInc | 25.78K | ± 78.04 | ops/s |
| codahaleIncNoLabels | 25.64K | ± 1.13K | ops/s |
| prometheusNoLabelsInc | 25.64K | ± 226.32 | ops/s |
| openTelemetryBoundInc | 25.36K | ± 362.73 | ops/s |
| prometheusAdd | 25.03K | ± 97.08 | ops/s |
| openTelemetryBoundAdd | 23.38K | ± 81.94 | ops/s |
| openTelemetryIncNoLabels | 20.50K | ± 320.05 | ops/s |
| openTelemetryInc | 19.65K | ± 819.18 | ops/s |
| openTelemetryAdd | 17.41K | ± 219.22 | ops/s |
| simpleclientInc | 6.52K | ± 23.70 | ops/s |
| simpleclientAdd | 6.40K | ± 125.49 | ops/s |
| simpleclientNoLabelsInc | 6.39K | ± 19.61 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 6.47K | ± 66.19 | ops/s |
| simpleclient | 4.11K | ± 130.28 | ops/s |
| prometheusClassic | 3.58K | ± 306.67 | ops/s |
| openTelemetryBoundClassic | 2.91K | ± 371.75 | ops/s |
| prometheusClassicSingleThread | 2.82K | ± 64.98 | ops/s |
| openTelemetryClassic | 2.64K | ± 208.50 | ops/s |
| prometheusNative | 1.87K | ± 152.45 | ops/s |
| openTelemetryBoundExponential | 628.11 | ± 14.82 | ops/s |
| openTelemetryExponential | 545.09 | ± 53.51 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 17.45K | ± 54.96 | ops/s |
| openMetricsWriteToNull | 17.43K | ± 36.16 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 311.10K | ± 3.03K | ops/s |
| prometheusWriteToByteArray | 308.60K | ± 3.00K | ops/s |
| openMetricsWriteToNull | 287.80K | ± 975.92 | ops/s |
| openMetricsWriteToByteArray | 284.38K | ± 1.92K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.037 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.054 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.040 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.037 | — | — |
| CounterBenchmark.openTelemetryInc | 0.048 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.046 | — | — |
| CounterBenchmark.prometheusAdd | 0.148 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.144 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.144 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.145 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.330 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.499 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.362 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.733 | — | — |
| HistogramBenchmark.prometheusClassic | 1.043 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.253 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.986 | — | — |
| HistogramBenchmark.prometheusNative | 253874.001 | — | — |
| HistogramBenchmark.simpleclient | 0.230 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.203 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.203 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.336 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      25644.141   ± 1127.513  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      17405.561    ± 219.218  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      23383.991     ± 81.941  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      25356.079    ± 362.734  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      19650.615    ± 819.175  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      20497.475    ± 320.055  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      25027.954     ± 97.083  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  255795539.806 ± 17491598.372  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   92541991.188 ± 140475.922  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      25777.677     ± 78.042  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  122641362.010 ± 869849.533  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   59911080.416 ± 215444.427  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      25639.927    ± 226.318  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6397.373    ± 125.492  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6519.046     ± 23.698  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6387.066     ± 19.610  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2911.896    ± 371.748  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        628.105     ± 14.817  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2642.472    ± 208.496  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        545.091     ± 53.515  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3579.184    ± 306.668  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6472.650     ± 66.186  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2824.116     ± 64.977  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1867.941    ± 152.447  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4106.242    ± 130.277  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      17432.300     ± 36.164  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17451.731     ± 54.960  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     284375.051   ± 1917.867  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     287795.757    ± 975.922  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     308598.128   ± 2998.063  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     311101.847   ± 3034.042  ops/s
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

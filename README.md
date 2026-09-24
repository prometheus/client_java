# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-24T04:24:50Z
- **Commit:** [`e87a1ca`](https://github.com/prometheus/client_java/commit/e87a1cac71af91bff7c8136c4cf077e5c5a1fff5)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 711.39M | ± 23315.83K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 222.64M | ± 807.85K | ops/s |
| prometheusLabelValuesInc | 204.93M | ± 3762.90K | ops/s |
| prometheusLabelValuesIncSingleThread | 98.79M | ± 1159.14K | ops/s |
| prometheusInc | 68.72K | ± 841.10 | ops/s |
| prometheusNoLabelsInc | 67.63K | ± 592.13 | ops/s |
| codahaleIncNoLabels | 61.97K | ± 345.65 | ops/s |
| prometheusAdd | 57.64K | ± 631.62 | ops/s |
| openTelemetryBoundInc | 54.43K | ± 230.70 | ops/s |
| openTelemetryBoundAdd | 47.93K | ± 752.51 | ops/s |
| openTelemetryIncNoLabels | 36.46K | ± 220.65 | ops/s |
| openTelemetryInc | 31.05K | ± 276.07 | ops/s |
| openTelemetryAdd | 26.31K | ± 314.74 | ops/s |
| simpleclientNoLabelsInc | 11.25K | ± 132.75 | ops/s |
| simpleclientInc | 10.90K | ± 304.44 | ops/s |
| simpleclientAdd | 10.64K | ± 150.47 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.38K | ± 131.20 | ops/s |
| prometheusClassic | 11.54K | ± 1.39K | ops/s |
| openTelemetryBoundClassic | 7.30K | ± 2.10K | ops/s |
| simpleclient | 7.23K | ± 122.98 | ops/s |
| openTelemetryClassic | 6.31K | ± 2.11K | ops/s |
| prometheusClassicSingleThread | 4.55K | ± 15.73 | ops/s |
| prometheusNative | 4.42K | ± 99.12 | ops/s |
| openTelemetryBoundExponential | 1.06K | ± 16.84 | ops/s |
| openTelemetryExponential | 989.29 | ± 16.96 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 34.00K | ± 231.51 | ops/s |
| openMetricsWriteToNull | 33.80K | ± 339.56 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 829.68K | ± 28.77K | ops/s |
| prometheusWriteToNull | 810.67K | ± 33.84K | ops/s |
| openMetricsWriteToByteArray | 757.64K | ± 41.35K | ops/s |
| openMetricsWriteToNull | 715.33K | ± 27.60K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.015 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.036 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.020 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.017 | — | — |
| CounterBenchmark.openTelemetryInc | 0.030 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.026 | — | — |
| CounterBenchmark.prometheusAdd | 0.064 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.053 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.055 | — | — |
| CounterBenchmark.simpleclientAdd | 0.088 | — | — |
| CounterBenchmark.simpleclientInc | 0.086 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.084 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.137 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.890 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.162 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.957 | — | — |
| HistogramBenchmark.prometheusClassic | 0.323 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.520 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.605 | — | — |
| HistogramBenchmark.prometheusNative | 417712.856 | — | — |
| HistogramBenchmark.simpleclient | 0.132 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.103 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.103 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      61965.572    ± 345.645  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      26313.541    ± 314.739  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      47925.848    ± 752.509  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      54430.210    ± 230.704  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      31052.696    ± 276.068  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      36462.631    ± 220.645  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      57639.549    ± 631.623  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  711386231.146 ± 23315825.106  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  222643537.076 ± 807854.777  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      68718.410    ± 841.101  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  204928569.619 ± 3762898.465  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   98793455.452 ± 1159137.298  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      67626.536    ± 592.130  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10636.244    ± 150.475  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10897.217    ± 304.443  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      11246.506    ± 132.746  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       7295.144   ± 2099.500  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1062.369     ± 16.838  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6308.758   ± 2106.012  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        989.294     ± 16.963  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15      11543.368   ± 1391.815  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15383.054    ± 131.201  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4547.432     ± 15.731  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4415.298     ± 99.119  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       7225.837    ± 122.976  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      33799.887    ± 339.558  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      34003.174    ± 231.508  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     757635.889  ± 41353.835  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     715329.176  ± 27600.620  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     829676.156  ± 28766.391  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     810672.841  ± 33839.467  ops/s
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

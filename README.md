# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-09T04:33:14Z
- **Commit:** [`d8089b2`](https://github.com/prometheus/client_java/commit/d8089b219004512f136a5e3d6153df74ddfbe748)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 289.63M | ± 7768.99K | ops/s |
| prometheusLabelValuesInc | 93.24M | ± 197.91K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 93.22M | ± 48.95K | ops/s |
| prometheusLabelValuesIncSingleThread | 56.15M | ± 2702.70K | ops/s |
| prometheusNoLabelsInc | 31.07K | ± 69.87 | ops/s |
| prometheusInc | 30.29K | ± 1.75K | ops/s |
| codahaleIncNoLabels | 29.41K | ± 702.25 | ops/s |
| openTelemetryBoundInc | 28.94K | ± 55.00 | ops/s |
| prometheusAdd | 27.50K | ± 790.74 | ops/s |
| openTelemetryBoundAdd | 27.32K | ± 397.61 | ops/s |
| openTelemetryIncNoLabels | 20.05K | ± 931.47 | ops/s |
| openTelemetryInc | 14.89K | ± 648.40 | ops/s |
| openTelemetryAdd | 13.66K | ± 705.62 | ops/s |
| simpleclientInc | 6.34K | ± 30.63 | ops/s |
| simpleclientAdd | 6.06K | ± 89.83 | ops/s |
| simpleclientNoLabelsInc | 5.91K | ± 158.64 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.86K | ± 22.42 | ops/s |
| prometheusClassic | 4.30K | ± 1.59K | ops/s |
| simpleclient | 4.08K | ± 123.08 | ops/s |
| openTelemetryBoundClassic | 3.73K | ± 1.91K | ops/s |
| prometheusClassicSingleThread | 3.23K | ± 80.62 | ops/s |
| openTelemetryClassic | 3.20K | ± 380.57 | ops/s |
| prometheusNative | 2.10K | ± 158.39 | ops/s |
| openTelemetryBoundExponential | 577.47 | ± 46.39 | ops/s |
| openTelemetryExponential | 490.05 | ± 21.63 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 18.10K | ± 179.54 | ops/s |
| openMetricsWriteToNull | 18.06K | ± 113.68 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 332.27K | ± 4.06K | ops/s |
| prometheusWriteToByteArray | 322.70K | ± 3.38K | ops/s |
| openMetricsWriteToNull | 304.68K | ± 4.02K | ops/s |
| openMetricsWriteToByteArray | 302.51K | ± 2.54K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.032 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.068 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.034 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.032 | — | — |
| CounterBenchmark.openTelemetryInc | 0.063 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.047 | — | — |
| CounterBenchmark.prometheusAdd | 0.134 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.122 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.119 | — | — |
| CounterBenchmark.simpleclientAdd | 0.153 | — | — |
| CounterBenchmark.simpleclientInc | 0.146 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.158 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.291 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.617 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.298 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.905 | — | — |
| HistogramBenchmark.prometheusClassic | 0.945 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.026 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.853 | — | — |
| HistogramBenchmark.prometheusNative | 335793.818 | — | — |
| HistogramBenchmark.simpleclient | 0.231 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.194 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.193 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.669 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29409.812    ± 702.247  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      13660.551    ± 705.619  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      27323.882    ± 397.615  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      28941.478     ± 55.002  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14892.140    ± 648.404  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      20051.360    ± 931.470  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      27504.264    ± 790.741  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  289633321.119 ± 7768991.640  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   93223291.137  ± 48946.803  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30293.799   ± 1753.083  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15   93241571.170 ± 197907.401  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   56147015.218 ± 2702702.264  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31072.284     ± 69.871  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6056.338     ± 89.826  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6342.093     ± 30.632  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5907.746    ± 158.636  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3727.492   ± 1911.824  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        577.472     ± 46.391  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3202.545    ± 380.569  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        490.048     ± 21.628  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4298.279   ± 1592.146  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7857.984     ± 22.422  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3229.182     ± 80.616  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2097.039    ± 158.394  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4076.497    ± 123.075  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18061.436    ± 113.680  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18098.837    ± 179.544  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     302513.125   ± 2536.988  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     304678.031   ± 4018.732  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     322699.256   ± 3377.666  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     332268.908   ± 4062.782  ops/s
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

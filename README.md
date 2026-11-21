# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-21T04:13:57Z
- **Commit:** [`976ef13`](https://github.com/prometheus/client_java/commit/976ef13e6831424335b6f58f7301152d745a5346)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 536.68M | ± 7158.01K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 335.02M | ± 264.21K | ops/s |
| prometheusLabelValuesInc | 116.34M | ± 3550.26K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.80M | ± 219.28K | ops/s |
| prometheusInc | 62.22K | ± 3.94K | ops/s |
| prometheusNoLabelsInc | 56.04K | ± 1.18K | ops/s |
| prometheusAdd | 51.13K | ± 929.59 | ops/s |
| codahaleIncNoLabels | 49.23K | ± 2.06K | ops/s |
| openTelemetryBoundInc | 37.54K | ± 875.79 | ops/s |
| openTelemetryBoundAdd | 31.59K | ± 801.58 | ops/s |
| openTelemetryIncNoLabels | 23.34K | ± 1.20K | ops/s |
| openTelemetryInc | 22.66K | ± 992.39 | ops/s |
| openTelemetryAdd | 19.01K | ± 467.54 | ops/s |
| simpleclientInc | 6.58K | ± 123.45 | ops/s |
| simpleclientNoLabelsInc | 6.39K | ± 237.49 | ops/s |
| simpleclientAdd | 6.36K | ± 189.23 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.00K | ± 225.12 | ops/s |
| prometheusClassic | 7.57K | ± 1.04K | ops/s |
| openTelemetryClassic | 5.28K | ± 1.02K | ops/s |
| prometheusClassicSingleThread | 4.52K | ± 19.53 | ops/s |
| simpleclient | 4.43K | ± 98.57 | ops/s |
| prometheusNative | 2.64K | ± 346.08 | ops/s |
| openTelemetryBoundClassic | 2.52K | ± 255.25 | ops/s |
| openTelemetryBoundExponential | 1.11K | ± 128.74 | ops/s |
| openTelemetryExponential | 988.14 | ± 82.95 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 31.17K | ± 1.23K | ops/s |
| prometheusWriteToNull | 30.80K | ± 1.46K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 533.68K | ± 2.83K | ops/s |
| prometheusWriteToByteArray | 525.03K | ± 3.44K | ops/s |
| openMetricsWriteToByteArray | 512.91K | ± 10.62K | ops/s |
| openMetricsWriteToNull | 508.47K | ± 9.74K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.059 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.146 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.380 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.854 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.186 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.952 | — | — |
| HistogramBenchmark.prometheusClassic | 0.494 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.684 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.644 | — | — |
| HistogramBenchmark.prometheusNative | 417713.432 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.113 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.115 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49232.305   ± 2057.105  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19009.171    ± 467.541  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31587.815    ± 801.577  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37543.361    ± 875.786  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22663.053    ± 992.389  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23344.189   ± 1196.036  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51126.666    ± 929.592  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  536677269.231 ± 7158010.060  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  335016782.598 ± 264210.078  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62220.308   ± 3937.940  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116344311.066 ± 3550259.392  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58801364.966 ± 219275.330  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56037.924   ± 1179.601  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6364.443    ± 189.234  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6580.240    ± 123.446  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6390.108    ± 237.486  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2518.975    ± 255.250  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1109.443    ± 128.735  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5279.360   ± 1018.280  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        988.136     ± 82.945  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7574.419   ± 1040.976  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11996.808    ± 225.122  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4518.536     ± 19.528  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2642.910    ± 346.079  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4426.344     ± 98.569  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      31167.931   ± 1227.596  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30802.067   ± 1457.469  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     512905.896  ± 10615.779  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     508466.529   ± 9739.024  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     525028.054   ± 3435.979  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     533684.224   ± 2832.233  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-19T04:26:37Z
- **Commit:** [`eadfb6d`](https://github.com/prometheus/client_java/commit/eadfb6d550809340c24ed2a78b33ce30587a0364)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 555.45M | ± 9629.34K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 333.56M | ± 793.06K | ops/s |
| prometheusLabelValuesInc | 114.52M | ± 2014.28K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.09M | ± 715.18K | ops/s |
| prometheusInc | 64.20K | ± 1.59K | ops/s |
| prometheusNoLabelsInc | 55.60K | ± 1.13K | ops/s |
| prometheusAdd | 50.82K | ± 808.23 | ops/s |
| codahaleIncNoLabels | 43.09K | ± 6.05K | ops/s |
| openTelemetryBoundInc | 37.65K | ± 527.99 | ops/s |
| openTelemetryBoundAdd | 31.35K | ± 525.57 | ops/s |
| openTelemetryInc | 23.28K | ± 314.22 | ops/s |
| openTelemetryIncNoLabels | 22.43K | ± 1.45K | ops/s |
| openTelemetryAdd | 19.28K | ± 285.32 | ops/s |
| simpleclientInc | 6.55K | ± 124.63 | ops/s |
| simpleclientNoLabelsInc | 6.26K | ± 109.51 | ops/s |
| simpleclientAdd | 6.06K | ± 236.79 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.02K | ± 162.95 | ops/s |
| prometheusClassic | 6.51K | ± 1.19K | ops/s |
| openTelemetryBoundClassic | 4.45K | ± 400.14 | ops/s |
| simpleclient | 4.43K | ± 73.33 | ops/s |
| prometheusClassicSingleThread | 4.40K | ± 78.45 | ops/s |
| openTelemetryClassic | 4.19K | ± 1.57K | ops/s |
| prometheusNative | 2.83K | ± 263.72 | ops/s |
| openTelemetryBoundExponential | 999.90 | ± 66.51 | ops/s |
| openTelemetryExponential | 949.63 | ± 76.85 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 30.72K | ± 719.47 | ops/s |
| openMetricsWriteToNull | 29.93K | ± 800.99 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 506.73K | ± 3.58K | ops/s |
| prometheusWriteToByteArray | 498.44K | ± 4.82K | ops/s |
| openMetricsWriteToNull | 494.63K | ± 3.15K | ops/s |
| openMetricsWriteToByteArray | 488.44K | ± 5.57K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.022 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.040 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.155 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.215 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.941 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.248 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.994 | — | — |
| HistogramBenchmark.prometheusClassic | 0.584 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.676 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.663 | — | — |
| HistogramBenchmark.prometheusNative | 335793.325 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.118 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.115 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43089.965   ± 6049.563  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19278.880    ± 285.323  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31354.501    ± 525.568  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37654.168    ± 527.987  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23276.065    ± 314.225  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22427.396   ± 1447.101  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50816.131    ± 808.235  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  555451647.435 ± 9629343.751  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  333556412.947 ± 793064.347  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64202.695   ± 1589.478  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  114523400.388 ± 2014282.527  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58093362.664 ± 715177.395  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55596.387   ± 1131.372  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6062.351    ± 236.794  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6547.783    ± 124.628  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6262.092    ± 109.506  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4453.698    ± 400.137  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        999.897     ± 66.514  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4185.024   ± 1573.010  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        949.632     ± 76.850  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6514.018   ± 1188.917  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12019.359    ± 162.946  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4403.430     ± 78.445  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2832.859    ± 263.725  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4430.282     ± 73.328  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29925.612    ± 800.994  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30715.992    ± 719.468  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     488437.397   ± 5565.080  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     494633.175   ± 3151.143  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     498435.811   ± 4821.385  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     506733.911   ± 3579.593  ops/s
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

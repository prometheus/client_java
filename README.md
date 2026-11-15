# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-15T04:14:32Z
- **Commit:** [`743bbab`](https://github.com/prometheus/client_java/commit/743bbab03410082675b1203566ed65a84a5512d6)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 556.32M | ± 11460.08K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 335.03M | ± 289.91K | ops/s |
| prometheusLabelValuesInc | 116.70M | ± 1784.49K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.73M | ± 309.21K | ops/s |
| prometheusInc | 65.55K | ± 937.54 | ops/s |
| prometheusNoLabelsInc | 56.10K | ± 1.10K | ops/s |
| prometheusAdd | 50.83K | ± 1.09K | ops/s |
| codahaleIncNoLabels | 48.91K | ± 1.66K | ops/s |
| openTelemetryBoundInc | 37.66K | ± 647.82 | ops/s |
| openTelemetryBoundAdd | 31.75K | ± 790.86 | ops/s |
| openTelemetryIncNoLabels | 23.08K | ± 1.15K | ops/s |
| openTelemetryInc | 23.00K | ± 398.69 | ops/s |
| openTelemetryAdd | 19.38K | ± 339.23 | ops/s |
| simpleclientInc | 6.48K | ± 99.25 | ops/s |
| simpleclientAdd | 6.44K | ± 87.91 | ops/s |
| simpleclientNoLabelsInc | 6.36K | ± 101.80 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.98K | ± 191.65 | ops/s |
| prometheusClassic | 6.70K | ± 2.36K | ops/s |
| openTelemetryClassic | 5.16K | ± 1.00K | ops/s |
| prometheusClassicSingleThread | 4.52K | ± 15.26 | ops/s |
| simpleclient | 4.43K | ± 91.04 | ops/s |
| openTelemetryBoundClassic | 4.00K | ± 175.01 | ops/s |
| prometheusNative | 2.54K | ± 119.12 | ops/s |
| openTelemetryBoundExponential | 1.14K | ± 43.89 | ops/s |
| openTelemetryExponential | 988.83 | ± 20.80 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 30.85K | ± 693.74 | ops/s |
| prometheusWriteToNull | 30.74K | ± 443.82 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 524.81K | ± 3.46K | ops/s |
| prometheusWriteToByteArray | 514.31K | ± 3.55K | ops/s |
| openMetricsWriteToNull | 505.73K | ± 6.05K | ops/s |
| openMetricsWriteToByteArray | 505.31K | ± 5.85K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.145 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.235 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.821 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.189 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.946 | — | — |
| HistogramBenchmark.prometheusClassic | 0.624 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.683 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 335793.469 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.114 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.115 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48911.023   ± 1662.195  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19381.376    ± 339.229  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31746.409    ± 790.863  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37660.392    ± 647.821  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22995.690    ± 398.690  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23078.015   ± 1153.197  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50828.891   ± 1088.330  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  556323550.406 ± 11460084.240  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  335026594.078 ± 289911.898  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65548.698    ± 937.543  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116699441.298 ± 1784487.836  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58725924.361 ± 309209.403  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56098.080   ± 1098.395  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6443.630     ± 87.911  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6481.516     ± 99.250  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6364.948    ± 101.803  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4000.334    ± 175.006  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1143.124     ± 43.891  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5155.409   ± 1000.752  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        988.826     ± 20.799  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6695.324   ± 2361.025  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11977.875    ± 191.651  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4524.439     ± 15.264  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2541.855    ± 119.119  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4431.194     ± 91.038  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      30850.707    ± 693.742  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30742.289    ± 443.820  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     505313.133   ± 5847.425  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     505732.303   ± 6051.032  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     514307.896   ± 3546.812  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     524810.291   ± 3464.324  ops/s
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

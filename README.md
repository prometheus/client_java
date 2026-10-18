# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-18T04:22:40Z
- **Commit:** [`eadfb6d`](https://github.com/prometheus/client_java/commit/eadfb6d550809340c24ed2a78b33ce30587a0364)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 258.58M | ± 8828.61K | ops/s |
| prometheusLabelValuesInc | 122.03M | ± 956.81K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 91.94M | ± 191.44K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.30M | ± 399.94K | ops/s |
| codahaleIncNoLabels | 26.67K | ± 104.13 | ops/s |
| prometheusInc | 25.66K | ± 50.45 | ops/s |
| prometheusNoLabelsInc | 25.54K | ± 545.99 | ops/s |
| openTelemetryBoundInc | 25.11K | ± 376.26 | ops/s |
| prometheusAdd | 25.05K | ± 89.88 | ops/s |
| openTelemetryBoundAdd | 23.16K | ± 131.58 | ops/s |
| openTelemetryIncNoLabels | 20.76K | ± 150.22 | ops/s |
| openTelemetryInc | 19.04K | ± 1.55K | ops/s |
| openTelemetryAdd | 16.55K | ± 787.62 | ops/s |
| simpleclientInc | 6.39K | ± 87.07 | ops/s |
| simpleclientNoLabelsInc | 6.33K | ± 50.34 | ops/s |
| simpleclientAdd | 6.18K | ± 165.40 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 6.48K | ± 17.62 | ops/s |
| simpleclient | 4.08K | ± 71.46 | ops/s |
| prometheusClassic | 3.64K | ± 355.38 | ops/s |
| openTelemetryClassic | 2.83K | ± 696.47 | ops/s |
| prometheusClassicSingleThread | 2.81K | ± 62.62 | ops/s |
| openTelemetryBoundClassic | 2.70K | ± 217.00 | ops/s |
| prometheusNative | 1.98K | ± 197.18 | ops/s |
| openTelemetryBoundExponential | 639.27 | ± 17.77 | ops/s |
| openTelemetryExponential | 537.21 | ± 27.34 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 18.68K | ± 44.15 | ops/s |
| openMetricsWriteToNull | 18.61K | ± 61.32 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 304.45K | ± 2.56K | ops/s |
| prometheusWriteToByteArray | 302.37K | ± 1.90K | ops/s |
| openMetricsWriteToNull | 281.65K | ± 1.27K | ops/s |
| openMetricsWriteToByteArray | 279.06K | ± 2.03K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.035 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.057 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.041 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.037 | — | — |
| CounterBenchmark.openTelemetryInc | 0.049 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.045 | — | — |
| CounterBenchmark.prometheusAdd | 0.147 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.144 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.145 | — | — |
| CounterBenchmark.simpleclientAdd | 0.152 | — | — |
| CounterBenchmark.simpleclientInc | 0.147 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.355 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.470 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.345 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.747 | — | — |
| HistogramBenchmark.prometheusClassic | 1.022 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.236 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.974 | — | — |
| HistogramBenchmark.prometheusNative | 417713.890 | — | — |
| HistogramBenchmark.simpleclient | 0.231 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.189 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.189 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.003 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.336 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.336 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      26671.047    ± 104.128  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      16549.706    ± 787.622  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      23161.372    ± 131.580  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      25113.609    ± 376.262  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      19039.453   ± 1547.029  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      20759.991    ± 150.224  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      25046.721     ± 89.878  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  258575388.802 ± 8828611.299  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   91944731.688 ± 191435.527  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      25660.658     ± 50.454  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  122031862.713 ± 956812.784  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58302914.206 ± 399942.115  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      25539.221    ± 545.992  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6183.791    ± 165.403  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6391.475     ± 87.065  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6332.598     ± 50.340  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2696.964    ± 216.999  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        639.274     ± 17.769  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2831.943    ± 696.465  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        537.209     ± 27.342  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3635.649    ± 355.378  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6478.186     ± 17.620  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2806.567     ± 62.622  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1983.178    ± 197.181  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4082.510     ± 71.459  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18609.476     ± 61.324  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18681.792     ± 44.150  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     279060.323   ± 2030.461  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     281648.518   ± 1273.991  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     302370.365   ± 1904.314  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     304447.029   ± 2563.813  ops/s
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

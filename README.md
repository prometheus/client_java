# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-02T04:16:06Z
- **Commit:** [`e574d32`](https://github.com/prometheus/client_java/commit/e574d3226951fbecda109554024dc1f99eb8d5d5)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 286.45M | ± 9481.73K | ops/s |
| prometheusLabelValuesInc | 140.25M | ± 440.69K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 106.52M | ± 559.51K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.57M | ± 255.86K | ops/s |
| codahaleIncNoLabels | 29.80K | ± 179.86 | ops/s |
| prometheusInc | 29.53K | ± 90.34 | ops/s |
| prometheusNoLabelsInc | 29.48K | ± 298.62 | ops/s |
| prometheusAdd | 28.14K | ± 416.22 | ops/s |
| openTelemetryBoundInc | 27.94K | ± 393.37 | ops/s |
| openTelemetryBoundAdd | 25.11K | ± 966.37 | ops/s |
| openTelemetryIncNoLabels | 22.05K | ± 319.74 | ops/s |
| openTelemetryInc | 21.79K | ± 1.81K | ops/s |
| openTelemetryAdd | 18.71K | ± 347.51 | ops/s |
| simpleclientInc | 7.28K | ± 66.46 | ops/s |
| simpleclientNoLabelsInc | 7.09K | ± 67.86 | ops/s |
| simpleclientAdd | 7.01K | ± 176.97 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.34K | ± 97.86 | ops/s |
| simpleclient | 4.65K | ± 62.11 | ops/s |
| prometheusClassic | 3.87K | ± 1.44K | ops/s |
| prometheusClassicSingleThread | 3.23K | ± 82.71 | ops/s |
| openTelemetryClassic | 2.93K | ± 268.99 | ops/s |
| openTelemetryBoundClassic | 2.53K | ± 669.40 | ops/s |
| prometheusNative | 1.93K | ± 417.27 | ops/s |
| openTelemetryBoundExponential | 651.46 | ± 45.76 | ops/s |
| openTelemetryExponential | 602.05 | ± 105.47 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 21.25K | ± 70.44 | ops/s |
| prometheusWriteToNull | 21.20K | ± 113.62 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 319.93K | ± 3.16K | ops/s |
| prometheusWriteToByteArray | 314.69K | ± 2.86K | ops/s |
| openMetricsWriteToByteArray | 295.25K | ± 1.29K | ops/s |
| openMetricsWriteToNull | 294.50K | ± 1.61K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.032 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.050 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.037 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.033 | — | — |
| CounterBenchmark.openTelemetryInc | 0.044 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.132 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.126 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.126 | — | — |
| CounterBenchmark.simpleclientAdd | 0.135 | — | — |
| CounterBenchmark.simpleclientInc | 0.129 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.132 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.389 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.445 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.329 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.608 | — | — |
| HistogramBenchmark.prometheusClassic | 1.039 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.105 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.841 | — | — |
| HistogramBenchmark.prometheusNative | 335793.985 | — | — |
| HistogramBenchmark.simpleclient | 0.204 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.166 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.166 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.336 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.669 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29795.791    ± 179.856  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      18705.004    ± 347.510  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      25109.695    ± 966.374  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      27939.462    ± 393.374  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21786.346   ± 1813.802  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22053.132    ± 319.735  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28143.501    ± 416.221  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  286445755.366 ± 9481733.004  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  106522661.405 ± 559512.817  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      29527.513     ± 90.339  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  140253486.179 ± 440693.972  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68574508.275 ± 255859.681  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      29484.405    ± 298.619  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7008.356    ± 176.970  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7279.553     ± 66.465  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7090.440     ± 67.857  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2534.864    ± 669.404  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        651.455     ± 45.761  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2931.987    ± 268.989  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        602.049    ± 105.473  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3870.526   ± 1440.953  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7335.955     ± 97.857  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3228.249     ± 82.707  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1932.677    ± 417.266  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4649.050     ± 62.106  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      21246.315     ± 70.441  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      21204.135    ± 113.616  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     295247.506   ± 1285.602  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     294504.888   ± 1609.738  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     314687.822   ± 2859.309  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     319927.165   ± 3157.897  ops/s
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

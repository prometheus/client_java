# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-12T04:42:30Z
- **Commit:** [`d8089b2`](https://github.com/prometheus/client_java/commit/d8089b219004512f136a5e3d6153df74ddfbe748)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 288.93M | ± 3282.95K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 93.30M | ± 102.98K | ops/s |
| prometheusLabelValuesInc | 92.78M | ± 973.03K | ops/s |
| prometheusLabelValuesIncSingleThread | 57.74M | ± 638.61K | ops/s |
| prometheusInc | 31.48K | ± 99.05 | ops/s |
| prometheusNoLabelsInc | 29.73K | ± 1.38K | ops/s |
| codahaleIncNoLabels | 29.26K | ± 840.79 | ops/s |
| prometheusAdd | 28.38K | ± 75.71 | ops/s |
| openTelemetryBoundInc | 28.38K | ± 1.26K | ops/s |
| openTelemetryBoundAdd | 27.35K | ± 471.85 | ops/s |
| openTelemetryIncNoLabels | 19.83K | ± 2.44K | ops/s |
| openTelemetryInc | 15.99K | ± 1.40K | ops/s |
| openTelemetryAdd | 14.52K | ± 376.68 | ops/s |
| simpleclientInc | 6.42K | ± 18.35 | ops/s |
| simpleclientAdd | 6.17K | ± 56.41 | ops/s |
| simpleclientNoLabelsInc | 5.97K | ± 119.22 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.73K | ± 232.98 | ops/s |
| prometheusClassic | 4.62K | ± 1.71K | ops/s |
| simpleclient | 4.11K | ± 77.68 | ops/s |
| openTelemetryBoundClassic | 3.94K | ± 1.81K | ops/s |
| prometheusClassicSingleThread | 3.23K | ± 81.06 | ops/s |
| openTelemetryClassic | 2.57K | ± 129.63 | ops/s |
| prometheusNative | 2.17K | ± 79.93 | ops/s |
| openTelemetryBoundExponential | 699.58 | ± 56.68 | ops/s |
| openTelemetryExponential | 553.40 | ± 46.24 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 18.18K | ± 78.89 | ops/s |
| prometheusWriteToNull | 18.16K | ± 83.46 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 334.80K | ± 1.95K | ops/s |
| prometheusWriteToByteArray | 331.88K | ± 3.30K | ops/s |
| openMetricsWriteToNull | 314.29K | ± 2.20K | ops/s |
| openMetricsWriteToByteArray | 313.22K | ± 2.01K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.032 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.064 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.034 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.033 | — | — |
| CounterBenchmark.openTelemetryInc | 0.058 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.048 | — | — |
| CounterBenchmark.prometheusAdd | 0.130 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.117 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.124 | — | — |
| CounterBenchmark.simpleclientAdd | 0.151 | — | — |
| CounterBenchmark.simpleclientInc | 0.145 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.156 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.269 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.333 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.367 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.692 | — | — |
| HistogramBenchmark.prometheusClassic | 0.882 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.052 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.849 | — | — |
| HistogramBenchmark.prometheusNative | 417713.739 | — | — |
| HistogramBenchmark.simpleclient | 0.227 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.192 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.193 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.669 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.002 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29263.707    ± 840.792  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      14519.745    ± 376.680  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      27354.076    ± 471.849  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      28379.279   ± 1258.796  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15989.278   ± 1400.684  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      19825.785   ± 2441.639  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28383.186     ± 75.712  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  288934485.194 ± 3282951.996  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   93299056.965 ± 102975.966  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31482.931     ± 99.051  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15   92779857.816 ± 973025.771  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   57739497.136 ± 638609.013  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      29725.399   ± 1379.639  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6173.967     ± 56.407  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6415.934     ± 18.347  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5973.696    ± 119.225  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3938.638   ± 1811.555  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        699.583     ± 56.677  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2567.218    ± 129.632  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        553.403     ± 46.236  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4618.473   ± 1706.993  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7731.658    ± 232.982  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3231.851     ± 81.063  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2166.292     ± 79.927  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4106.670     ± 77.676  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18182.415     ± 78.894  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18157.925     ± 83.460  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     313222.236   ± 2007.098  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     314292.839   ± 2203.011  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     331877.850   ± 3296.453  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     334801.272   ± 1946.240  ops/s
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

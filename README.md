# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-06T04:28:24Z
- **Commit:** [`affe1e4`](https://github.com/prometheus/client_java/commit/affe1e4fb99bd03d01e6b84bae67898f91999c92)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 528.41M | ± 36766.16K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.49M | ± 420.17K | ops/s |
| prometheusLabelValuesInc | 115.38M | ± 1200.51K | ops/s |
| prometheusLabelValuesIncSingleThread | 57.94M | ± 1409.29K | ops/s |
| prometheusInc | 65.56K | ± 1.19K | ops/s |
| prometheusNoLabelsInc | 56.63K | ± 910.84 | ops/s |
| prometheusAdd | 50.97K | ± 840.45 | ops/s |
| codahaleIncNoLabels | 48.92K | ± 1.49K | ops/s |
| openTelemetryBoundInc | 37.53K | ± 638.87 | ops/s |
| openTelemetryBoundAdd | 31.55K | ± 528.34 | ops/s |
| openTelemetryIncNoLabels | 21.59K | ± 354.04 | ops/s |
| openTelemetryInc | 18.11K | ± 316.30 | ops/s |
| openTelemetryAdd | 15.48K | ± 249.21 | ops/s |
| simpleclientInc | 6.59K | ± 108.86 | ops/s |
| simpleclientAdd | 6.42K | ± 110.90 | ops/s |
| simpleclientNoLabelsInc | 6.30K | ± 91.01 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.99K | ± 198.42 | ops/s |
| prometheusClassic | 4.79K | ± 1.42K | ops/s |
| openTelemetryBoundClassic | 4.67K | ± 501.31 | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 16.59 | ops/s |
| simpleclient | 4.40K | ± 75.79 | ops/s |
| openTelemetryClassic | 3.94K | ± 205.70 | ops/s |
| prometheusNative | 2.84K | ± 261.77 | ops/s |
| openTelemetryBoundExponential | 984.76 | ± 58.50 | ops/s |
| openTelemetryExponential | 867.29 | ± 89.56 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.95K | ± 216.60 | ops/s |
| openMetricsWriteToNull | 23.23K | ± 761.28 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 491.52K | ± 5.16K | ops/s |
| prometheusWriteToByteArray | 485.62K | ± 4.75K | ops/s |
| openMetricsWriteToByteArray | 479.64K | ± 4.02K | ops/s |
| openMetricsWriteToNull | 470.94K | ± 3.09K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.145 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.203 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.948 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.239 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.083 | — | — |
| HistogramBenchmark.prometheusClassic | 0.816 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.676 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.643 | — | — |
| HistogramBenchmark.prometheusNative | 417677.441 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.151 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.146 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48921.962   ± 1488.667  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15476.665    ± 249.209  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31547.788    ± 528.336  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37530.306    ± 638.874  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18113.673    ± 316.301  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21593.365    ± 354.044  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50966.563    ± 840.449  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  528411381.435 ± 36766158.083  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334493958.417 ± 420169.549  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65560.752   ± 1187.451  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115377441.394 ± 1200507.013  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   57936361.151 ± 1409286.695  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56628.874    ± 910.840  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6424.839    ± 110.895  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6587.410    ± 108.859  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6295.921     ± 91.008  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4668.085    ± 501.314  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        984.760     ± 58.498  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3939.968    ± 205.699  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        867.294     ± 89.562  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4793.290   ± 1418.306  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11991.881    ± 198.419  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4532.783     ± 16.595  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2839.701    ± 261.771  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4404.024     ± 75.791  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23230.336    ± 761.285  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23952.512    ± 216.600  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479635.828   ± 4022.059  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     470938.941   ± 3088.844  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485624.220   ± 4754.994  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491518.895   ± 5159.240  ops/s
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

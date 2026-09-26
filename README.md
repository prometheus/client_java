# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-26T04:22:56Z
- **Commit:** [`cce26b8`](https://github.com/prometheus/client_java/commit/cce26b87ccc0d8dbb83e8532ce7adba8f26c5372)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 548.95M | ± 2229.95K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.77M | ± 609.27K | ops/s |
| prometheusLabelValuesInc | 117.08M | ± 2047.30K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.45M | ± 286.00K | ops/s |
| prometheusInc | 66.28K | ± 367.79 | ops/s |
| prometheusNoLabelsInc | 55.78K | ± 834.80 | ops/s |
| prometheusAdd | 51.45K | ± 169.99 | ops/s |
| codahaleIncNoLabels | 49.30K | ± 1.73K | ops/s |
| openTelemetryBoundInc | 37.89K | ± 149.05 | ops/s |
| openTelemetryBoundAdd | 31.18K | ± 479.84 | ops/s |
| openTelemetryIncNoLabels | 22.00K | ± 113.47 | ops/s |
| openTelemetryInc | 17.90K | ± 356.66 | ops/s |
| openTelemetryAdd | 15.55K | ± 47.45 | ops/s |
| simpleclientInc | 6.56K | ± 42.67 | ops/s |
| simpleclientAdd | 6.49K | ± 54.51 | ops/s |
| simpleclientNoLabelsInc | 6.11K | ± 381.42 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.05K | ± 28.03 | ops/s |
| prometheusClassic | 6.12K | ± 1.92K | ops/s |
| openTelemetryBoundClassic | 5.24K | ± 1.37K | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 17.09 | ops/s |
| simpleclient | 4.43K | ± 23.56 | ops/s |
| openTelemetryClassic | 3.98K | ± 316.43 | ops/s |
| prometheusNative | 2.69K | ± 293.26 | ops/s |
| openTelemetryBoundExponential | 1.15K | ± 38.05 | ops/s |
| openTelemetryExponential | 862.83 | ± 7.95 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 24.23K | ± 321.31 | ops/s |
| prometheusWriteToNull | 23.19K | ± 308.82 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 542.91K | ± 2.76K | ops/s |
| prometheusWriteToNull | 537.93K | ± 11.67K | ops/s |
| openMetricsWriteToNull | 521.68K | ± 6.43K | ops/s |
| openMetricsWriteToByteArray | 512.29K | ± 5.22K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.052 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.055 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.144 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.152 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.189 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.807 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.233 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.079 | — | — |
| HistogramBenchmark.prometheusClassic | 0.664 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.658 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.642 | — | — |
| HistogramBenchmark.prometheusNative | 417713.384 | — | — |
| HistogramBenchmark.simpleclient | 0.212 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.144 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.151 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18429.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49299.159   ± 1733.847  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15553.330     ± 47.452  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31183.692    ± 479.841  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37893.582    ± 149.055  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17899.124    ± 356.662  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21995.351    ± 113.470  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51448.818    ± 169.988  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  548952924.251 ± 2229953.487  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334767844.363 ± 609274.875  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66280.871    ± 367.786  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  117076643.396 ± 2047301.538  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58447289.945 ± 286001.855  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55777.463    ± 834.800  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6492.059     ± 54.513  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6563.908     ± 42.673  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6109.063    ± 381.421  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5238.029   ± 1370.648  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1147.135     ± 38.047  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3983.499    ± 316.427  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        862.830      ± 7.951  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6119.811   ± 1923.022  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12045.223     ± 28.030  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4536.114     ± 17.085  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2687.386    ± 293.263  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4425.040     ± 23.560  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24228.849    ± 321.306  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23193.092    ± 308.824  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     512286.639   ± 5224.739  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     521682.388   ± 6427.711  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     542912.460   ± 2758.550  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     537926.863  ± 11666.468  ops/s
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

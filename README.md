# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-16T04:16:32Z
- **Commit:** [`743bbab`](https://github.com/prometheus/client_java/commit/743bbab03410082675b1203566ed65a84a5512d6)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 674.36M | ± 4439.39K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 370.44M | ± 440.11K | ops/s |
| prometheusLabelValuesInc | 140.28M | ± 4096.42K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.27M | ± 259.91K | ops/s |
| prometheusInc | 76.98K | ± 1.78K | ops/s |
| prometheusNoLabelsInc | 66.33K | ± 583.05 | ops/s |
| prometheusAdd | 62.96K | ± 975.70 | ops/s |
| codahaleIncNoLabels | 55.46K | ± 2.41K | ops/s |
| openTelemetryBoundInc | 44.80K | ± 78.69 | ops/s |
| openTelemetryBoundAdd | 38.74K | ± 623.62 | ops/s |
| openTelemetryIncNoLabels | 27.41K | ± 725.43 | ops/s |
| openTelemetryInc | 27.31K | ± 248.58 | ops/s |
| openTelemetryAdd | 24.09K | ± 53.88 | ops/s |
| simpleclientInc | 7.86K | ± 90.90 | ops/s |
| simpleclientNoLabelsInc | 7.58K | ± 27.41 | ops/s |
| simpleclientAdd | 7.43K | ± 416.69 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.51K | ± 49.28 | ops/s |
| prometheusClassic | 9.97K | ± 2.37K | ops/s |
| prometheusClassicSingleThread | 7.13K | ± 259.17 | ops/s |
| openTelemetryClassic | 6.92K | ± 1.61K | ops/s |
| simpleclient | 5.79K | ± 101.23 | ops/s |
| openTelemetryBoundClassic | 4.21K | ± 799.53 | ops/s |
| prometheusNative | 3.34K | ± 283.51 | ops/s |
| openTelemetryExponential | 927.87 | ± 39.39 | ops/s |
| openTelemetryBoundExponential | 916.98 | ± 26.49 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 40.15K | ± 237.23 | ops/s |
| prometheusWriteToNull | 39.79K | ± 1.17K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 760.86K | ± 24.58K | ops/s |
| prometheusWriteToByteArray | 758.70K | ± 11.28K | ops/s |
| openMetricsWriteToNull | 719.27K | ± 13.11K | ops/s |
| openMetricsWriteToByteArray | 698.49K | ± 20.17K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.017 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.039 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.034 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.034 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.048 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.127 | — | — |
| CounterBenchmark.simpleclientInc | 0.119 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.124 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.231 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.027 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.143 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.017 | — | — |
| HistogramBenchmark.prometheusClassic | 0.386 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.464 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.410 | — | — |
| HistogramBenchmark.prometheusNative | 417713.119 | — | — |
| HistogramBenchmark.simpleclient | 0.163 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.088 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.089 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      55458.670   ± 2411.325  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      24088.906     ± 53.883  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      38741.488    ± 623.615  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      44804.803     ± 78.693  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      27309.189    ± 248.583  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      27414.279    ± 725.425  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62958.334    ± 975.701  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  674358884.238 ± 4439389.657  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  370435473.073 ± 440111.112  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      76983.674   ± 1784.874  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  140275645.337 ± 4096416.752  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68272356.368 ± 259914.477  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66330.630    ± 583.046  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7433.997    ± 416.685  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7864.833     ± 90.903  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7584.241     ± 27.410  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4208.905    ± 799.529  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        916.975     ± 26.493  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6916.792   ± 1608.557  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        927.872     ± 39.392  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9969.123   ± 2371.560  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17511.103     ± 49.277  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7130.651    ± 259.169  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3340.286    ± 283.507  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5787.660    ± 101.230  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      40145.321    ± 237.228  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      39787.033   ± 1168.067  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     698488.982  ± 20165.005  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     719268.787  ± 13106.689  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     758702.240  ± 11278.564  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     760860.234  ± 24584.752  ops/s
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

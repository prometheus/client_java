# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-11T04:34:06Z
- **Commit:** [`d8089b2`](https://github.com/prometheus/client_java/commit/d8089b219004512f136a5e3d6153df74ddfbe748)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 254.24M | ± 438.02K | ops/s |
| prometheusLabelValuesInc | 126.88M | ± 479.31K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 95.39M | ± 34.41K | ops/s |
| prometheusLabelValuesIncSingleThread | 62.28M | ± 77.12K | ops/s |
| prometheusNoLabelsInc | 26.77K | ± 108.07 | ops/s |
| codahaleIncNoLabels | 26.68K | ± 1.37K | ops/s |
| prometheusInc | 26.54K | ± 267.02 | ops/s |
| openTelemetryBoundInc | 26.10K | ± 406.27 | ops/s |
| prometheusAdd | 25.60K | ± 389.43 | ops/s |
| openTelemetryBoundAdd | 24.09K | ± 161.94 | ops/s |
| openTelemetryIncNoLabels | 20.67K | ± 947.52 | ops/s |
| openTelemetryInc | 18.06K | ± 756.47 | ops/s |
| openTelemetryAdd | 16.16K | ± 205.31 | ops/s |
| simpleclientInc | 6.70K | ± 108.61 | ops/s |
| simpleclientNoLabelsInc | 6.54K | ± 60.49 | ops/s |
| simpleclientAdd | 6.47K | ± 220.34 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 6.74K | ± 13.14 | ops/s |
| simpleclient | 4.19K | ± 79.62 | ops/s |
| prometheusClassic | 3.95K | ± 1.20K | ops/s |
| prometheusClassicSingleThread | 2.92K | ± 65.62 | ops/s |
| openTelemetryBoundClassic | 2.85K | ± 116.11 | ops/s |
| openTelemetryClassic | 2.48K | ± 568.74 | ops/s |
| prometheusNative | 1.89K | ± 257.78 | ops/s |
| openTelemetryBoundExponential | 707.80 | ± 112.60 | ops/s |
| openTelemetryExponential | 514.23 | ± 35.26 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 17.99K | ± 19.39 | ops/s |
| prometheusWriteToNull | 17.93K | ± 114.27 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 324.36K | ± 2.08K | ops/s |
| prometheusWriteToByteArray | 320.62K | ± 1.76K | ops/s |
| openMetricsWriteToNull | 298.41K | ± 2.88K | ops/s |
| openMetricsWriteToByteArray | 297.76K | ± 1.09K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.035 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.058 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.039 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.036 | — | — |
| CounterBenchmark.openTelemetryInc | 0.052 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.045 | — | — |
| CounterBenchmark.prometheusAdd | 0.144 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.139 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.138 | — | — |
| CounterBenchmark.simpleclientAdd | 0.144 | — | — |
| CounterBenchmark.simpleclientInc | 0.139 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.142 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.334 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.349 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.393 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.822 | — | — |
| HistogramBenchmark.prometheusClassic | 0.990 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.198 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.968 | — | — |
| HistogramBenchmark.prometheusNative | 417713.995 | — | — |
| HistogramBenchmark.simpleclient | 0.222 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.194 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.195 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.669 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18485.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      26680.019   ± 1373.423  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      16160.253    ± 205.309  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      24093.966    ± 161.937  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      26098.937    ± 406.267  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18060.923    ± 756.468  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      20668.249    ± 947.518  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      25597.802    ± 389.429  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  254237215.964 ± 438019.798  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   95388290.089  ± 34411.517  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      26537.731    ± 267.022  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  126878568.753 ± 479309.038  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   62283598.626  ± 77116.791  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      26773.720    ± 108.072  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6467.623    ± 220.336  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6699.326    ± 108.612  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6543.982     ± 60.490  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       2847.348    ± 116.105  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        707.805    ± 112.596  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2483.700    ± 568.738  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        514.228     ± 35.256  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3954.745   ± 1202.552  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       6738.836     ± 13.140  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       2917.944     ± 65.618  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1888.570    ± 257.782  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4190.803     ± 79.624  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      17989.794     ± 19.392  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17933.794    ± 114.268  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     297758.944   ± 1089.123  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     298408.229   ± 2878.483  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     320618.640   ± 1761.346  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     324357.283   ± 2082.361  ops/s
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

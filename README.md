# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-18T04:15:02Z
- **Commit:** [`7cf92fe`](https://github.com/prometheus/client_java/commit/7cf92fe6d122d8ec5130a4fd930a822cb614376a)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 556.59M | ± 9507.61K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.42M | ± 431.81K | ops/s |
| prometheusLabelValuesInc | 115.12M | ± 1764.75K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.40M | ± 364.24K | ops/s |
| prometheusInc | 63.80K | ± 3.66K | ops/s |
| prometheusNoLabelsInc | 55.82K | ± 1.50K | ops/s |
| prometheusAdd | 51.14K | ± 898.61 | ops/s |
| codahaleIncNoLabels | 49.25K | ± 1.09K | ops/s |
| openTelemetryBoundInc | 37.86K | ± 737.20 | ops/s |
| openTelemetryBoundAdd | 32.15K | ± 441.03 | ops/s |
| openTelemetryIncNoLabels | 23.23K | ± 1.30K | ops/s |
| openTelemetryInc | 22.95K | ± 406.90 | ops/s |
| openTelemetryAdd | 19.36K | ± 358.31 | ops/s |
| simpleclientInc | 6.60K | ± 99.41 | ops/s |
| simpleclientAdd | 6.33K | ± 197.19 | ops/s |
| simpleclientNoLabelsInc | 6.31K | ± 100.34 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.01K | ± 120.49 | ops/s |
| prometheusClassic | 6.24K | ± 1.29K | ops/s |
| openTelemetryClassic | 5.35K | ± 1.40K | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 22.94 | ops/s |
| simpleclient | 4.47K | ± 74.31 | ops/s |
| openTelemetryBoundClassic | 3.99K | ± 113.51 | ops/s |
| prometheusNative | 2.72K | ± 316.29 | ops/s |
| openTelemetryBoundExponential | 1.08K | ± 36.42 | ops/s |
| openTelemetryExponential | 995.08 | ± 61.96 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 30.33K | ± 857.93 | ops/s |
| prometheusWriteToNull | 30.22K | ± 1.58K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 509.69K | ± 8.31K | ops/s |
| prometheusWriteToByteArray | 504.74K | ± 2.93K | ops/s |
| openMetricsWriteToNull | 497.75K | ± 6.56K | ops/s |
| openMetricsWriteToByteArray | 497.70K | ± 2.77K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.040 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.058 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.066 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.237 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.867 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.188 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.946 | — | — |
| HistogramBenchmark.prometheusClassic | 0.613 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.676 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.646 | — | — |
| HistogramBenchmark.prometheusNative | 417688.287 | — | — |
| HistogramBenchmark.simpleclient | 0.211 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.116 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.117 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49254.724   ± 1089.550  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19356.601    ± 358.311  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      32154.754    ± 441.034  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37860.304    ± 737.200  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22953.863    ± 406.898  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      23228.447   ± 1298.267  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51135.138    ± 898.613  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  556587813.550 ± 9507611.002  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334424450.915 ± 431806.323  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63804.158   ± 3662.391  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115120693.914 ± 1764754.188  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58401437.373 ± 364242.907  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55815.233   ± 1495.334  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6332.155    ± 197.188  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6598.377     ± 99.409  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6313.674    ± 100.344  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3992.782    ± 113.505  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1084.159     ± 36.419  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5350.807   ± 1400.566  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        995.084     ± 61.960  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6236.969   ± 1288.268  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12006.375    ± 120.489  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4512.684     ± 22.942  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2721.575    ± 316.287  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4468.565     ± 74.306  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      30332.603    ± 857.935  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30220.390   ± 1580.174  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     497697.986   ± 2772.055  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     497750.470   ± 6558.662  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     504741.383   ± 2926.948  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     509694.486   ± 8309.390  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-03T04:30:35Z
- **Commit:** [`9e9deb6`](https://github.com/prometheus/client_java/commit/9e9deb6b9e591c2a62b12882d8a987f120661b71)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 558.42M | ± 7181.26K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 335.05M | ± 315.08K | ops/s |
| prometheusLabelValuesInc | 116.15M | ± 2981.40K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.57M | ± 251.11K | ops/s |
| prometheusInc | 64.17K | ± 1.84K | ops/s |
| prometheusNoLabelsInc | 56.55K | ± 599.13 | ops/s |
| prometheusAdd | 50.46K | ± 1.37K | ops/s |
| codahaleIncNoLabels | 50.10K | ± 172.68 | ops/s |
| openTelemetryBoundInc | 37.95K | ± 168.14 | ops/s |
| openTelemetryBoundAdd | 31.69K | ± 691.34 | ops/s |
| openTelemetryIncNoLabels | 21.86K | ± 34.77 | ops/s |
| openTelemetryInc | 18.09K | ± 24.27 | ops/s |
| openTelemetryAdd | 15.57K | ± 53.70 | ops/s |
| simpleclientInc | 6.55K | ± 50.52 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 10.59 | ops/s |
| simpleclientAdd | 6.29K | ± 187.81 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.04K | ± 13.54 | ops/s |
| prometheusClassic | 7.16K | ± 147.66 | ops/s |
| openTelemetryBoundClassic | 4.62K | ± 1.73K | ops/s |
| prometheusClassicSingleThread | 4.55K | ± 20.29 | ops/s |
| simpleclient | 4.49K | ± 38.47 | ops/s |
| openTelemetryClassic | 4.35K | ± 868.36 | ops/s |
| prometheusNative | 2.85K | ± 285.59 | ops/s |
| openTelemetryBoundExponential | 956.84 | ± 61.67 | ops/s |
| openTelemetryExponential | 789.65 | ± 21.76 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.51K | ± 1.16K | ops/s |
| openMetricsWriteToNull | 23.37K | ± 887.33 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 548.51K | ± 8.85K | ops/s |
| prometheusWriteToByteArray | 539.71K | ± 7.36K | ops/s |
| openMetricsWriteToNull | 514.70K | ± 10.06K | ops/s |
| openMetricsWriteToByteArray | 514.28K | ± 6.83K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.024 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.146 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.231 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.972 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.221 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.176 | — | — |
| HistogramBenchmark.prometheusClassic | 0.514 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.663 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.640 | — | — |
| HistogramBenchmark.prometheusNative | 417713.314 | — | — |
| HistogramBenchmark.simpleclient | 0.208 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.150 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.149 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50102.242    ± 172.678  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15572.866     ± 53.699  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31686.658    ± 691.341  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37945.953    ± 168.140  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18091.361     ± 24.273  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21859.370     ± 34.769  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50458.645   ± 1369.630  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  558419219.898 ± 7181264.761  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  335046989.458 ± 315078.713  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64165.346   ± 1839.110  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116153063.441 ± 2981401.467  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58565873.977 ± 251105.712  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56549.016    ± 599.126  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6289.015    ± 187.808  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6550.646     ± 50.519  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6346.442     ± 10.589  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4616.627   ± 1733.020  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        956.836     ± 61.671  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4348.244    ± 868.363  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        789.654     ± 21.759  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7159.036    ± 147.661  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12043.974     ± 13.544  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4548.089     ± 20.292  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2845.378    ± 285.594  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4489.485     ± 38.466  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23368.059    ± 887.327  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23509.251   ± 1159.508  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     514283.265   ± 6830.263  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     514704.215  ± 10057.646  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     539705.113   ± 7362.332  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     548514.841   ± 8854.428  ops/s
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

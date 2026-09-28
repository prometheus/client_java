# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-28T04:33:16Z
- **Commit:** [`cce26b8`](https://github.com/prometheus/client_java/commit/cce26b87ccc0d8dbb83e8532ce7adba8f26c5372)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 527.88M | ± 4768.67K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 287.76M | ± 204.40K | ops/s |
| prometheusLabelValuesInc | 109.71M | ± 852.65K | ops/s |
| prometheusLabelValuesIncSingleThread | 53.10M | ± 135.79K | ops/s |
| prometheusInc | 60.16K | ± 793.15 | ops/s |
| prometheusNoLabelsInc | 51.49K | ± 972.87 | ops/s |
| prometheusAdd | 48.83K | ± 676.84 | ops/s |
| codahaleIncNoLabels | 44.02K | ± 219.22 | ops/s |
| openTelemetryBoundInc | 34.91K | ± 515.15 | ops/s |
| openTelemetryBoundAdd | 29.75K | ± 202.24 | ops/s |
| openTelemetryIncNoLabels | 21.15K | ± 649.02 | ops/s |
| openTelemetryInc | 16.68K | ± 36.43 | ops/s |
| openTelemetryAdd | 14.43K | ± 338.21 | ops/s |
| simpleclientInc | 6.12K | ± 58.48 | ops/s |
| simpleclientAdd | 5.93K | ± 267.78 | ops/s |
| simpleclientNoLabelsInc | 5.91K | ± 23.82 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.61K | ± 60.75 | ops/s |
| prometheusClassic | 8.18K | ± 1.62K | ops/s |
| openTelemetryBoundClassic | 5.76K | ± 1.22K | ops/s |
| prometheusClassicSingleThread | 5.59K | ± 27.97 | ops/s |
| openTelemetryClassic | 4.81K | ± 732.48 | ops/s |
| simpleclient | 4.49K | ± 53.18 | ops/s |
| prometheusNative | 2.95K | ± 225.23 | ops/s |
| openTelemetryBoundExponential | 735.68 | ± 44.94 | ops/s |
| openTelemetryExponential | 638.20 | ± 30.04 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.49K | ± 137.56 | ops/s |
| openMetricsWriteToNull | 27.21K | ± 238.04 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 638.66K | ± 2.84K | ops/s |
| prometheusWriteToByteArray | 625.21K | ± 5.05K | ops/s |
| openMetricsWriteToNull | 591.65K | ± 6.85K | ops/s |
| openMetricsWriteToByteArray | 580.59K | ± 7.94K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.021 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.064 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.031 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.027 | — | — |
| CounterBenchmark.openTelemetryInc | 0.056 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.044 | — | — |
| CounterBenchmark.prometheusAdd | 0.076 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.061 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.072 | — | — |
| CounterBenchmark.simpleclientAdd | 0.157 | — | — |
| CounterBenchmark.simpleclientInc | 0.153 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.158 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.168 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.274 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.200 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.465 | — | — |
| HistogramBenchmark.prometheusClassic | 0.465 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.591 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.522 | — | — |
| HistogramBenchmark.prometheusNative | 335793.267 | — | — |
| HistogramBenchmark.simpleclient | 0.208 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.129 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.127 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44023.931    ± 219.216  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      14432.897    ± 338.211  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      29752.910    ± 202.236  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      34908.889    ± 515.148  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      16679.914     ± 36.432  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21152.628    ± 649.017  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48827.572    ± 676.845  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  527876640.046 ± 4768672.427  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  287755911.852 ± 204397.649  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60163.911    ± 793.153  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  109708975.462 ± 852650.052  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   53100894.451 ± 135793.435  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51486.451    ± 972.869  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5932.216    ± 267.783  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6122.290     ± 58.485  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5905.019     ± 23.816  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5763.423   ± 1216.150  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        735.683     ± 44.944  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4806.369    ± 732.475  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        638.201     ± 30.042  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8176.733   ± 1619.329  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13608.828     ± 60.754  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5593.735     ± 27.972  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2945.689    ± 225.234  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4487.466     ± 53.176  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27208.459    ± 238.035  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27488.445    ± 137.563  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     580585.094   ± 7941.373  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     591651.616   ± 6849.575  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     625207.044   ± 5052.107  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     638657.850   ± 2839.906  ops/s
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

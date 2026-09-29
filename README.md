# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-29T04:25:40Z
- **Commit:** [`cce26b8`](https://github.com/prometheus/client_java/commit/cce26b87ccc0d8dbb83e8532ce7adba8f26c5372)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 555.19M | ± 7815.24K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.69M | ± 117.79K | ops/s |
| prometheusLabelValuesInc | 118.89M | ± 2076.78K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.47M | ± 486.73K | ops/s |
| prometheusInc | 66.81K | ± 714.26 | ops/s |
| prometheusNoLabelsInc | 56.18K | ± 913.15 | ops/s |
| prometheusAdd | 51.04K | ± 635.45 | ops/s |
| codahaleIncNoLabels | 47.91K | ± 1.97K | ops/s |
| openTelemetryBoundInc | 37.98K | ± 275.80 | ops/s |
| openTelemetryBoundAdd | 31.61K | ± 337.88 | ops/s |
| openTelemetryIncNoLabels | 21.86K | ± 138.91 | ops/s |
| openTelemetryInc | 18.15K | ± 120.32 | ops/s |
| openTelemetryAdd | 15.47K | ± 126.43 | ops/s |
| simpleclientInc | 6.57K | ± 87.85 | ops/s |
| simpleclientNoLabelsInc | 6.37K | ± 186.35 | ops/s |
| simpleclientAdd | 6.32K | ± 194.36 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.02K | ± 32.84 | ops/s |
| prometheusClassic | 6.04K | ± 1.55K | ops/s |
| openTelemetryClassic | 5.43K | ± 1.49K | ops/s |
| openTelemetryBoundClassic | 4.77K | ± 1.72K | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 16.95 | ops/s |
| simpleclient | 4.44K | ± 28.90 | ops/s |
| prometheusNative | 2.71K | ± 317.22 | ops/s |
| openTelemetryBoundExponential | 1.01K | ± 101.94 | ops/s |
| openTelemetryExponential | 888.12 | ± 36.68 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.66K | ± 1.11K | ops/s |
| openMetricsWriteToNull | 23.19K | ± 708.81 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 569.58K | ± 8.01K | ops/s |
| prometheusWriteToByteArray | 561.38K | ± 7.22K | ops/s |
| openMetricsWriteToNull | 533.34K | ± 2.45K | ops/s |
| openMetricsWriteToByteArray | 521.99K | ± 9.20K | ops/s |

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
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.055 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.147 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.146 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.225 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.928 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.184 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.042 | — | — |
| HistogramBenchmark.prometheusClassic | 0.654 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.663 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.641 | — | — |
| HistogramBenchmark.prometheusNative | 417713.378 | — | — |
| HistogramBenchmark.simpleclient | 0.209 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.151 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.148 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47913.549   ± 1974.655  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15469.796    ± 126.426  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31609.677    ± 337.878  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37981.214    ± 275.805  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18147.144    ± 120.321  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21860.431    ± 138.912  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51042.392    ± 635.447  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  555189241.863 ± 7815244.994  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334693168.310 ± 117792.091  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66812.938    ± 714.264  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  118885646.469 ± 2076780.830  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58468833.483 ± 486732.508  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56177.232    ± 913.145  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6321.412    ± 194.355  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6566.092     ± 87.853  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6365.420    ± 186.354  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4771.811   ± 1715.758  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1006.594    ± 101.944  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5432.192   ± 1490.085  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        888.122     ± 36.679  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6042.946   ± 1549.397  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12023.990     ± 32.844  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4534.076     ± 16.953  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2709.569    ± 317.219  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4442.871     ± 28.898  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23186.944    ± 708.814  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23663.725   ± 1108.783  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     521992.593   ± 9197.505  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     533338.118   ± 2447.981  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     561380.622   ± 7219.767  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     569580.382   ± 8010.431  ops/s
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

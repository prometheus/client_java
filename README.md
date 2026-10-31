# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-31T04:13:49Z
- **Commit:** [`e574d32`](https://github.com/prometheus/client_java/commit/e574d3226951fbecda109554024dc1f99eb8d5d5)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 681.53M | ± 2322.80K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 369.83M | ± 425.69K | ops/s |
| prometheusLabelValuesInc | 141.80M | ± 1254.85K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.15M | ± 256.15K | ops/s |
| prometheusInc | 77.16K | ± 169.90 | ops/s |
| prometheusNoLabelsInc | 64.79K | ± 2.97K | ops/s |
| codahaleIncNoLabels | 57.79K | ± 749.41 | ops/s |
| prometheusAdd | 53.85K | ± 12.95K | ops/s |
| openTelemetryBoundInc | 45.63K | ± 63.54 | ops/s |
| openTelemetryBoundAdd | 38.84K | ± 559.43 | ops/s |
| openTelemetryInc | 27.49K | ± 71.19 | ops/s |
| openTelemetryIncNoLabels | 27.19K | ± 815.72 | ops/s |
| openTelemetryAdd | 24.65K | ± 411.60 | ops/s |
| simpleclientInc | 7.85K | ± 131.95 | ops/s |
| simpleclientNoLabelsInc | 7.65K | ± 204.86 | ops/s |
| simpleclientAdd | 7.43K | ± 274.20 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.57K | ± 48.57 | ops/s |
| prometheusClassic | 10.76K | ± 2.14K | ops/s |
| prometheusClassicSingleThread | 7.22K | ± 20.76 | ops/s |
| openTelemetryBoundClassic | 5.95K | ± 2.52K | ops/s |
| simpleclient | 5.86K | ± 91.84 | ops/s |
| openTelemetryClassic | 5.32K | ± 319.33 | ops/s |
| prometheusNative | 3.42K | ± 202.58 | ops/s |
| openTelemetryBoundExponential | 897.09 | ± 58.58 | ops/s |
| openTelemetryExponential | 818.09 | ± 28.03 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 40.55K | ± 350.86 | ops/s |
| prometheusWriteToNull | 40.49K | ± 570.91 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 756.50K | ± 12.30K | ops/s |
| prometheusWriteToByteArray | 749.62K | ± 28.73K | ops/s |
| openMetricsWriteToByteArray | 706.93K | ± 4.53K | ops/s |
| openMetricsWriteToNull | 706.20K | ± 9.24K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.038 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.034 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.034 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.048 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.057 | — | — |
| CounterBenchmark.simpleclientAdd | 0.127 | — | — |
| CounterBenchmark.simpleclientInc | 0.120 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.123 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.179 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.056 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.178 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.153 | — | — |
| HistogramBenchmark.prometheusClassic | 0.356 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.459 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.405 | — | — |
| HistogramBenchmark.prometheusNative | 417713.089 | — | — |
| HistogramBenchmark.simpleclient | 0.161 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.087 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.087 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18477.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18504.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      57787.946    ± 749.413  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      24647.711    ± 411.603  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      38840.851    ± 559.434  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      45628.694     ± 63.537  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      27486.345     ± 71.192  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      27186.674    ± 815.722  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      53853.399  ± 12953.171  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  681529201.966 ± 2322803.378  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  369827878.136 ± 425693.906  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      77163.415    ± 169.899  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  141801836.430 ± 1254849.563  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68153139.699 ± 256152.601  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      64790.486   ± 2968.473  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7433.531    ± 274.195  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7853.615    ± 131.952  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7648.383    ± 204.857  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5950.234   ± 2515.095  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        897.092     ± 58.583  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5317.715    ± 319.326  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        818.093     ± 28.032  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15      10755.664   ± 2136.256  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17567.578     ± 48.569  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7219.373     ± 20.756  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3419.064    ± 202.585  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5864.752     ± 91.836  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      40553.952    ± 350.861  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      40493.553    ± 570.910  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     706925.913   ± 4529.727  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     706201.942   ± 9241.751  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     749621.589  ± 28727.709  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     756501.366  ± 12300.029  ops/s
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

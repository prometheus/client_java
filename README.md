# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-06T04:15:15Z
- **Commit:** [`456dda7`](https://github.com/prometheus/client_java/commit/456dda7309ad554cffea3c3a97451b460a6a3a75)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 561.34M | ± 9094.40K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.73M | ± 311.19K | ops/s |
| prometheusLabelValuesInc | 115.79M | ± 2755.41K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.45M | ± 212.15K | ops/s |
| prometheusInc | 66.26K | ± 1.01K | ops/s |
| prometheusNoLabelsInc | 55.24K | ± 975.01 | ops/s |
| prometheusAdd | 51.15K | ± 890.63 | ops/s |
| codahaleIncNoLabels | 47.23K | ± 844.93 | ops/s |
| openTelemetryBoundInc | 37.92K | ± 513.97 | ops/s |
| openTelemetryBoundAdd | 31.83K | ± 758.07 | ops/s |
| openTelemetryInc | 22.98K | ± 358.66 | ops/s |
| openTelemetryIncNoLabels | 22.73K | ± 1.09K | ops/s |
| openTelemetryAdd | 18.87K | ± 728.37 | ops/s |
| simpleclientInc | 6.61K | ± 121.82 | ops/s |
| simpleclientAdd | 6.44K | ± 80.70 | ops/s |
| simpleclientNoLabelsInc | 6.33K | ± 113.20 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.03K | ± 207.48 | ops/s |
| prometheusClassic | 5.35K | ± 2.65K | ops/s |
| openTelemetryClassic | 4.65K | ± 620.39 | ops/s |
| prometheusClassicSingleThread | 4.53K | ± 19.69 | ops/s |
| simpleclient | 4.42K | ± 92.83 | ops/s |
| openTelemetryBoundClassic | 4.05K | ± 71.15 | ops/s |
| prometheusNative | 2.94K | ± 229.32 | ops/s |
| openTelemetryBoundExponential | 1.04K | ± 29.07 | ops/s |
| openTelemetryExponential | 885.81 | ± 85.91 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 30.21K | ± 1.02K | ops/s |
| openMetricsWriteToNull | 30.19K | ± 1.54K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 519.99K | ± 11.64K | ops/s |
| prometheusWriteToByteArray | 515.76K | ± 7.17K | ops/s |
| openMetricsWriteToNull | 512.82K | ± 5.43K | ops/s |
| openMetricsWriteToByteArray | 494.61K | ± 1.23K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.020 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.050 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.041 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.067 | — | — |
| CounterBenchmark.simpleclientAdd | 0.145 | — | — |
| CounterBenchmark.simpleclientInc | 0.141 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.148 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.231 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.899 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.206 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.074 | — | — |
| HistogramBenchmark.prometheusClassic | 0.806 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.672 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 417713.271 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.117 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.117 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18496.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18496.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47234.773    ± 844.933  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      18872.190    ± 728.371  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31831.592    ± 758.072  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37917.986    ± 513.967  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      22984.902    ± 358.660  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22734.628   ± 1086.900  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51154.311    ± 890.627  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  561340554.446 ± 9094396.221  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334730777.307 ± 311185.692  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66263.952   ± 1012.690  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115788327.927 ± 2755409.072  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58452931.285 ± 212146.820  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55242.087    ± 975.007  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6435.146     ± 80.700  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6614.717    ± 121.816  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6327.630    ± 113.202  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4050.715     ± 71.154  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1043.348     ± 29.067  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4651.659    ± 620.387  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        885.807     ± 85.905  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5346.529   ± 2653.999  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12027.644    ± 207.478  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4532.048     ± 19.685  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2938.561    ± 229.316  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4424.156     ± 92.831  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      30185.204   ± 1540.996  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30207.980   ± 1015.156  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     494614.950   ± 1226.271  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     512819.050   ± 5431.562  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     515756.028   ± 7165.677  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     519992.505  ± 11644.338  ops/s
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

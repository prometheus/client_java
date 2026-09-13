# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-13T03:52:22Z
- **Commit:** [`fcc4466`](https://github.com/prometheus/client_java/commit/fcc4466e36262d155e3f7b0211fd8da6b013e3af)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 59.03K | ± 89.01 | ops/s |
| prometheusNoLabelsInc | 51.12K | ± 488.33 | ops/s |
| prometheusAdd | 48.23K | ± 429.47 | ops/s |
| codahaleIncNoLabels | 44.55K | ± 617.45 | ops/s |
| openTelemetryIncNoLabels | 17.14K | ± 33.42 | ops/s |
| openTelemetryInc | 13.96K | ± 190.09 | ops/s |
| openTelemetryAdd | 12.16K | ± 97.87 | ops/s |
| simpleclientInc | 6.21K | ± 113.10 | ops/s |
| simpleclientAdd | 6.02K | ± 223.46 | ops/s |
| simpleclientNoLabelsInc | 5.86K | ± 91.63 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.90K | ± 76.15 | ops/s |
| prometheusClassic | 6.87K | ± 2.80K | ops/s |
| prometheusClassicSingleThread | 5.85K | ± 8.61 | ops/s |
| simpleclient | 4.59K | ± 62.82 | ops/s |
| prometheusNative | 3.10K | ± 204.59 | ops/s |
| openTelemetryClassic | 774.63 | ± 6.52 | ops/s |
| openTelemetryExponential | 710.58 | ± 10.46 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 27.57K | ± 285.17 | ops/s |
| openMetricsWriteToNull | 27.33K | ± 157.59 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 563.17K | ± 2.63K | ops/s |
| prometheusWriteToByteArray | 554.62K | ± 4.47K | ops/s |
| openMetricsWriteToNull | 525.26K | ± 2.27K | ops/s |
| openMetricsWriteToByteArray | 523.43K | ± 2.91K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44546.407    ± 617.447  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12162.890     ± 97.873  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13962.647    ± 190.092  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17144.472     ± 33.420  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48227.139    ± 429.467  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59030.022     ± 89.005  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51124.734    ± 488.325  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6022.531    ± 223.463  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6205.787    ± 113.102  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5861.877     ± 91.631  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        774.633      ± 6.521  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        710.577     ± 10.463  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6874.158   ± 2802.869  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13897.159     ± 76.149  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5846.686      ± 8.611  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3102.497    ± 204.589  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4586.040     ± 62.823  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27332.112    ± 157.592  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27574.686    ± 285.171  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     523425.316   ± 2912.589  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     525264.650   ± 2273.942  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     554615.220   ± 4472.538  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     563168.056   ± 2628.420  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

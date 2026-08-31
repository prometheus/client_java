# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-31T03:54:15Z
- **Commit:** [`e43f451`](https://github.com/prometheus/client_java/commit/e43f4517810e3763fe863e2b84b55742b76df4c3)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 66.28K | ± 714.09 | ops/s |
| prometheusNoLabelsInc | 55.56K | ± 1.21K | ops/s |
| prometheusAdd | 50.23K | ± 152.95 | ops/s |
| codahaleIncNoLabels | 49.15K | ± 1.44K | ops/s |
| openTelemetryIncNoLabels | 18.49K | ± 168.16 | ops/s |
| openTelemetryInc | 15.50K | ± 70.21 | ops/s |
| openTelemetryAdd | 12.99K | ± 14.72 | ops/s |
| simpleclientInc | 6.63K | ± 60.61 | ops/s |
| simpleclientAdd | 6.47K | ± 20.73 | ops/s |
| simpleclientNoLabelsInc | 6.43K | ± 145.60 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.27K | ± 27.31 | ops/s |
| prometheusClassic | 5.92K | ± 1.49K | ops/s |
| prometheusClassicSingleThread | 4.64K | ± 60.50 | ops/s |
| simpleclient | 4.40K | ± 113.60 | ops/s |
| prometheusNative | 3.02K | ± 303.68 | ops/s |
| openTelemetryClassic | 856.69 | ± 74.32 | ops/s |
| openTelemetryExponential | 854.76 | ± 124.49 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 24.12K | ± 362.15 | ops/s |
| openMetricsWriteToNull | 23.49K | ± 815.81 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 513.18K | ± 5.73K | ops/s |
| prometheusWriteToByteArray | 506.56K | ± 2.76K | ops/s |
| openMetricsWriteToNull | 486.86K | ± 2.50K | ops/s |
| openMetricsWriteToByteArray | 480.55K | ± 3.98K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49150.019   ± 1438.008  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12987.629     ± 14.720  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15496.740     ± 70.207  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18487.647    ± 168.163  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50225.734    ± 152.951  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66275.272    ± 714.088  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55555.611   ± 1212.143  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6466.658     ± 20.729  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6625.461     ± 60.611  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6427.431    ± 145.595  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        856.687     ± 74.317  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        854.763    ± 124.487  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5920.894   ± 1485.258  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12270.728     ± 27.312  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4636.430     ± 60.497  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3018.924    ± 303.675  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4402.931    ± 113.602  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23491.032    ± 815.812  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24124.220    ± 362.148  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     480554.562   ± 3980.748  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486856.879   ± 2501.296  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     506555.146   ± 2763.260  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     513179.389   ± 5733.819  ops/s
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

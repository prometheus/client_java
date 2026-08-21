# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-21T04:00:01Z
- **Commit:** [`dfeba03`](https://github.com/prometheus/client_java/commit/dfeba0312720214e99c42a890fa3e2c0f7c6039d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 67.27K | ± 16.41K | ops/s |
| prometheusNoLabelsInc | 66.09K | ± 527.84 | ops/s |
| prometheusAdd | 62.74K | ± 980.64 | ops/s |
| codahaleIncNoLabels | 56.19K | ± 1.96K | ops/s |
| openTelemetryIncNoLabels | 22.13K | ± 172.68 | ops/s |
| openTelemetryInc | 17.72K | ± 241.92 | ops/s |
| openTelemetryAdd | 15.18K | ± 872.06 | ops/s |
| simpleclientNoLabelsInc | 7.93K | ± 256.97 | ops/s |
| simpleclientInc | 7.86K | ± 8.21 | ops/s |
| simpleclientAdd | 7.81K | ± 68.34 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 18.13K | ± 36.42 | ops/s |
| prometheusClassicSingleThread | 7.49K | ± 28.44 | ops/s |
| prometheusClassic | 6.63K | ± 1.11K | ops/s |
| simpleclient | 5.89K | ± 79.82 | ops/s |
| prometheusNative | 4.09K | ± 73.15 | ops/s |
| openTelemetryClassic | 958.99 | ± 3.03 | ops/s |
| openTelemetryExponential | 882.17 | ± 113.00 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 35.59K | ± 56.56 | ops/s |
| openMetricsWriteToNull | 35.19K | ± 468.39 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 701.11K | ± 4.18K | ops/s |
| prometheusWriteToByteArray | 686.34K | ± 11.12K | ops/s |
| openMetricsWriteToNull | 659.56K | ± 4.14K | ops/s |
| openMetricsWriteToByteArray | 642.92K | ± 8.62K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      56185.398   ± 1957.530  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15176.254    ± 872.057  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17718.647    ± 241.923  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22128.227    ± 172.685  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62744.572    ± 980.639  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67266.027  ± 16406.330  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66086.523    ± 527.841  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7813.370     ± 68.343  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7855.986      ± 8.214  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7932.026    ± 256.967  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        958.990      ± 3.032  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        882.174    ± 112.999  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6629.191   ± 1106.500  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      18130.294     ± 36.422  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7494.714     ± 28.440  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4087.688     ± 73.148  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5893.013     ± 79.821  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35185.334    ± 468.389  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35590.353     ± 56.561  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     642924.139   ± 8615.056  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     659558.492   ± 4143.540  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     686340.097  ± 11123.325  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     701109.349   ± 4181.905  ops/s
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

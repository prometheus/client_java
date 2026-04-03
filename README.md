# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-03T04:30:21Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.54K | ± 131.84 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.24K | ± 1.10K | ops/s | 1.2x slower |
| prometheusAdd | 51.24K | ± 527.45 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 43.65K | ± 7.85K | ops/s | 1.5x slower |
| simpleclientInc | 6.62K | ± 64.47 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.41K | ± 172.30 | ops/s | 10x slower |
| simpleclientAdd | 6.20K | ± 238.77 | ops/s | 11x slower |
| openTelemetryInc | 1.38K | ± 240.40 | ops/s | 48x slower |
| openTelemetryAdd | 1.25K | ± 112.10 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.19K | ± 47.99 | ops/s | 56x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.49K | ± 19.17 | ops/s | **fastest** |
| prometheusClassic | 4.36K | ± 573.29 | ops/s | 1.0x slower |
| prometheusNative | 3.18K | ± 46.14 | ops/s | 1.4x slower |
| openTelemetryClassic | 688.53 | ± 10.58 | ops/s | 6.5x slower |
| openTelemetryExponential | 547.17 | ± 20.19 | ops/s | 8.2x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 498.33K | ± 2.57K | ops/s | **fastest** |
| prometheusWriteToByteArray | 485.89K | ± 3.81K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 484.52K | ± 1.70K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 480.76K | ± 2.66K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43654.653   ± 7846.155  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1251.967    ± 112.103  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1378.951    ± 240.404  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1186.078     ± 47.986  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51244.322    ± 527.454  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66535.094    ± 131.837  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56242.178   ± 1098.199  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6195.776    ± 238.771  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6624.993     ± 64.471  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6414.038    ± 172.299  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        688.525     ± 10.579  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        547.166     ± 20.193  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4357.593    ± 573.290  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3178.112     ± 46.137  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4490.762     ± 19.169  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     480763.984   ± 2662.004  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     484523.784   ± 1695.754  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485885.543   ± 3812.405  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     498334.625   ± 2572.031  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-17T04:25:45Z
- **Commit:** [`b81332e`](https://github.com/prometheus/client_java/commit/b81332e3a09e465f956f118a2403e64b83771ae5)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.83K | ± 326.02 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.19K | ± 214.55 | ops/s | 1.2x slower |
| prometheusAdd | 50.93K | ± 398.44 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 46.99K | ± 154.29 | ops/s | 1.4x slower |
| simpleclientNoLabelsInc | 6.70K | ± 16.54 | ops/s | 10.0x slower |
| simpleclientInc | 6.66K | ± 148.32 | ops/s | 10x slower |
| simpleclientAdd | 6.17K | ± 48.24 | ops/s | 11x slower |
| openTelemetryAdd | 1.30K | ± 78.38 | ops/s | 52x slower |
| openTelemetryInc | 1.22K | ± 17.28 | ops/s | 55x slower |
| openTelemetryIncNoLabels | 1.18K | ± 10.93 | ops/s | 57x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.56K | ± 35.64 | ops/s | **fastest** |
| prometheusClassic | 4.10K | ± 252.80 | ops/s | 1.1x slower |
| prometheusNative | 2.62K | ± 134.65 | ops/s | 1.7x slower |
| openTelemetryClassic | 677.36 | ± 34.59 | ops/s | 6.7x slower |
| openTelemetryExponential | 576.83 | ± 30.21 | ops/s | 7.9x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 497.08K | ± 3.08K | ops/s | **fastest** |
| prometheusWriteToByteArray | 493.80K | ± 3.82K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 490.54K | ± 2.20K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 485.01K | ± 4.12K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      46988.830    ± 154.292  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1296.228     ± 78.378  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1218.243     ± 17.281  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1176.153     ± 10.933  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50930.467    ± 398.443  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66834.840    ± 326.017  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57191.822    ± 214.550  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6166.778     ± 48.237  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6664.658    ± 148.316  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6700.249     ± 16.541  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        677.361     ± 34.588  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        576.830     ± 30.206  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4101.439    ± 252.804  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2617.469    ± 134.648  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4557.729     ± 35.644  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     485005.411   ± 4120.643  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490539.151   ± 2201.120  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     493803.642   ± 3818.143  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     497080.790   ± 3076.963  ops/s
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

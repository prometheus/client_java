# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-09T04:30:23Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.77K | ± 734.85 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.43K | ± 919.56 | ops/s | 1.2x slower |
| prometheusAdd | 48.27K | ± 758.91 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.04K | ± 184.64 | ops/s | 1.4x slower |
| simpleclientInc | 6.35K | ± 26.48 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.09K | ± 260.51 | ops/s | 10.0x slower |
| simpleclientAdd | 6.00K | ± 87.04 | ops/s | 10x slower |
| openTelemetryAdd | 1.49K | ± 49.46 | ops/s | 41x slower |
| openTelemetryInc | 1.40K | ± 44.14 | ops/s | 43x slower |
| openTelemetryIncNoLabels | 1.38K | ± 73.71 | ops/s | 44x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.46K | ± 2.76K | ops/s | **fastest** |
| simpleclient | 4.42K | ± 70.99 | ops/s | 1.7x slower |
| prometheusNative | 2.73K | ± 55.64 | ops/s | 2.7x slower |
| openTelemetryClassic | 623.64 | ± 28.04 | ops/s | 12x slower |
| openTelemetryExponential | 510.79 | ± 13.27 | ops/s | 15x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 551.49K | ± 6.78K | ops/s | **fastest** |
| prometheusWriteToByteArray | 543.62K | ± 4.16K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 532.89K | ± 9.23K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 526.90K | ± 7.34K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44041.446    ± 184.640  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1491.622     ± 49.462  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1399.063     ± 44.142  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1377.473     ± 73.708  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48272.277    ± 758.907  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60770.320    ± 734.854  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51431.818    ± 919.563  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5996.483     ± 87.041  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6346.877     ± 26.484  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6087.762    ± 260.507  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        623.645     ± 28.040  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        510.794     ± 13.265  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7464.291   ± 2759.370  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2733.944     ± 55.640  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4416.756     ± 70.992  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     526902.982   ± 7340.923  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     532888.356   ± 9231.494  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     543623.431   ± 4157.205  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     551491.501   ± 6781.655  ops/s
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

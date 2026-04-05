# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-05T04:30:30Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.24K | ± 3.57K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.98K | ± 335.23 | ops/s | 1.1x slower |
| prometheusAdd | 50.83K | ± 1.26K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.26K | ± 1.61K | ops/s | 1.3x slower |
| simpleclientInc | 6.59K | ± 165.01 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.35K | ± 194.86 | ops/s | 10x slower |
| simpleclientAdd | 6.32K | ± 211.54 | ops/s | 10x slower |
| openTelemetryInc | 1.30K | ± 40.73 | ops/s | 49x slower |
| openTelemetryAdd | 1.26K | ± 52.53 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.22K | ± 34.00 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.59K | ± 792.42 | ops/s | **fastest** |
| simpleclient | 4.48K | ± 68.27 | ops/s | 1.5x slower |
| prometheusNative | 2.83K | ± 305.91 | ops/s | 2.3x slower |
| openTelemetryClassic | 695.03 | ± 28.27 | ops/s | 9.5x slower |
| openTelemetryExponential | 583.26 | ± 42.71 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.28K | ± 2.25K | ops/s | **fastest** |
| openMetricsWriteToNull | 486.07K | ± 3.36K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 486.06K | ± 5.31K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.98K | ± 4.49K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48264.967   ± 1608.083  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1261.903     ± 52.530  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1302.039     ± 40.729  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1218.400     ± 33.999  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50834.280   ± 1262.350  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64239.317   ± 3571.144  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56982.515    ± 335.228  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6321.343    ± 211.542  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6591.826    ± 165.008  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6348.005    ± 194.857  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        695.032     ± 28.269  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        583.261     ± 42.715  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6589.304    ± 792.423  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2832.661    ± 305.910  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4479.126     ± 68.270  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475976.497   ± 4485.723  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486072.530   ± 3363.532  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     486063.711   ± 5307.946  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491280.401   ± 2251.017  ops/s
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

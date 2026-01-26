# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-01-26T23:15:55Z
- **Commit:** [`d32fd12`](https://github.com/prometheus/client_java/commit/d32fd1260440996d672c2650d43af3b535a28c32)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 67.53K | ± 956.34 | ops/s | **fastest** |
| prometheusNoLabelsInc | 60.16K | ± 733.68 | ops/s | 1.1x slower |
| prometheusAdd | 51.71K | ± 190.19 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.19K | ± 1.24K | ops/s | 1.4x slower |
| simpleclientInc | 7.09K | ± 107.68 | ops/s | 9.5x slower |
| simpleclientAdd | 6.76K | ± 52.80 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.75K | ± 155.58 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.42K | ± 201.96 | ops/s | 47x slower |
| openTelemetryInc | 1.29K | ± 38.60 | ops/s | 52x slower |
| openTelemetryAdd | 1.26K | ± 96.12 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.32K | ± 107.56 | ops/s | **fastest** |
| simpleclient | 4.51K | ± 50.20 | ops/s | 1.2x slower |
| prometheusNative | 3.13K | ± 34.57 | ops/s | 1.7x slower |
| openTelemetryClassic | 685.63 | ± 21.53 | ops/s | 7.8x slower |
| openTelemetryExponential | 570.06 | ± 15.75 | ops/s | 9.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 573.91K | ± 5.56K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.97K | ± 13.14K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 521.52K | ± 9.75K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 515.43K | ± 9.06K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48187.829   ± 1240.298  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1263.879     ± 96.125  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1292.436     ± 38.599  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1421.689    ± 201.955  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51709.772    ± 190.188  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      67527.313    ± 956.339  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      60162.035    ± 733.681  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6760.809     ± 52.800  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7088.266    ± 107.676  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6751.425    ± 155.583  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        685.635     ± 21.534  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        570.058     ± 15.752  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5316.776    ± 107.563  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3126.789     ± 34.565  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4512.752     ± 50.196  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     515433.242   ± 9063.055  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     521517.753   ± 9751.094  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547970.768  ± 13138.875  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     573907.220   ± 5558.404  ops/s
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

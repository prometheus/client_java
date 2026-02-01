# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-01T04:28:44Z
- **Commit:** [`421033a`](https://github.com/prometheus/client_java/commit/421033a2f72ef0c52d77fae6eb1b346c81b1fdb3)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.39K | ± 1.20K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.52K | ± 87.10 | ops/s | 1.1x slower |
| prometheusAdd | 51.70K | ± 311.59 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.71K | ± 1.85K | ops/s | 1.3x slower |
| simpleclientInc | 6.70K | ± 122.28 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.56K | ± 163.50 | ops/s | 9.8x slower |
| simpleclientAdd | 6.47K | ± 165.59 | ops/s | 10.0x slower |
| openTelemetryAdd | 1.54K | ± 258.13 | ops/s | 42x slower |
| openTelemetryInc | 1.37K | ± 203.44 | ops/s | 47x slower |
| openTelemetryIncNoLabels | 1.20K | ± 52.93 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.34K | ± 83.04 | ops/s | **fastest** |
| simpleclient | 4.57K | ± 28.05 | ops/s | 1.2x slower |
| prometheusNative | 2.95K | ± 143.03 | ops/s | 1.8x slower |
| openTelemetryClassic | 685.09 | ± 32.67 | ops/s | 7.8x slower |
| openTelemetryExponential | 562.51 | ± 17.74 | ops/s | 9.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 535.96K | ± 9.49K | ops/s | **fastest** |
| prometheusWriteToNull | 533.70K | ± 6.04K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 505.25K | ± 4.33K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 500.14K | ± 4.98K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49711.834   ± 1854.368  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1539.427    ± 258.133  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1373.961    ± 203.441  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1201.930     ± 52.929  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51702.895    ± 311.595  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64388.389   ± 1198.359  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56515.639     ± 87.098  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6465.692    ± 165.587  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6696.656    ± 122.281  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6557.477    ± 163.499  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        685.091     ± 32.667  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        562.515     ± 17.737  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5340.879     ± 83.038  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2947.180    ± 143.027  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4574.515     ± 28.055  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     500139.821   ± 4984.768  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     505250.601   ± 4331.714  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     535959.613   ± 9490.235  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     533697.475   ± 6035.606  ops/s
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

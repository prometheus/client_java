# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-07T04:39:14Z
- **Commit:** [`de73848`](https://github.com/prometheus/client_java/commit/de738487b85e8f85d8d3d79c54b8d05b739a7e42)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.61K | ± 474.88 | ops/s | **fastest** |
| prometheusNoLabelsInc | 50.52K | ± 280.51 | ops/s | 1.2x slower |
| prometheusAdd | 49.34K | ± 697.06 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.18K | ± 218.46 | ops/s | 1.3x slower |
| simpleclientInc | 6.14K | ± 57.28 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 5.91K | ± 20.21 | ops/s | 10x slower |
| simpleclientAdd | 5.87K | ± 193.29 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 4.91K | ± 714.64 | ops/s | 12x slower |
| openTelemetryInc | 4.71K | ± 942.09 | ops/s | 13x slower |
| openTelemetryAdd | 3.88K | ± 867.52 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.68K | ± 1.08K | ops/s | **fastest** |
| simpleclient | 4.06K | ± 131.21 | ops/s | 1.4x slower |
| prometheusNative | 3.15K | ± 35.58 | ops/s | 1.8x slower |
| openTelemetryClassic | 701.51 | ± 15.21 | ops/s | 8.1x slower |
| openTelemetryExponential | 534.95 | ± 21.02 | ops/s | 11x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.49K | ± 326.16 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.19K | ± 203.64 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 561.42K | ± 3.42K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.73K | ± 14.62K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 527.39K | ± 11.40K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 518.11K | ± 2.25K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44181.394    ± 218.456  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3875.520    ± 867.523  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4711.003    ± 942.094  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4912.621    ± 714.642  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      49337.791    ± 697.062  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59605.657    ± 474.885  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50524.061    ± 280.514  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5870.708    ± 193.291  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6138.901     ± 57.280  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5908.551     ± 20.208  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        701.506     ± 15.208  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        534.951     ± 21.022  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5679.263   ± 1076.823  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3147.898     ± 35.583  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4063.894    ± 131.214  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27191.757    ± 203.642  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27490.615    ± 326.160  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     518111.110   ± 2251.589  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     527389.473  ± 11398.184  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547728.551  ± 14622.368  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     561417.357   ± 3419.952  ops/s
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

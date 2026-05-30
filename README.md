# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-30T04:36:09Z
- **Commit:** [`23f36f5`](https://github.com/prometheus/client_java/commit/23f36f52b6f3792fcd5fd0c8ae2e7c306f17ef31)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.15K | ± 1.63K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.22K | ± 1.04K | ops/s | 1.2x slower |
| prometheusAdd | 51.43K | ± 187.28 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.77K | ± 742.30 | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 37.25 | ops/s | 9.9x slower |
| simpleclientAdd | 6.36K | ± 206.71 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.35K | ± 15.36 | ops/s | 10x slower |
| openTelemetryAdd | 3.76K | ± 371.48 | ops/s | 17x slower |
| openTelemetryInc | 3.38K | ± 270.16 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.30K | ± 191.78 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.72K | ± 1.66K | ops/s | **fastest** |
| simpleclient | 4.41K | ± 22.25 | ops/s | 1.3x slower |
| prometheusNative | 2.65K | ± 163.70 | ops/s | 2.2x slower |
| openTelemetryClassic | 730.64 | ± 27.00 | ops/s | 7.8x slower |
| openTelemetryExponential | 659.01 | ± 83.53 | ops/s | 8.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.80K | ± 686.80 | ops/s | **fastest** |
| prometheusWriteToNull | 23.23K | ± 406.66 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 513.98K | ± 4.56K | ops/s | **fastest** |
| prometheusWriteToByteArray | 501.95K | ± 11.03K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 488.01K | ± 4.85K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 481.32K | ± 7.04K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48772.285    ± 742.304  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3756.624    ± 371.480  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3376.921    ± 270.163  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3299.266    ± 191.776  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51430.146    ± 187.284  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65146.022   ± 1634.597  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56215.127   ± 1043.097  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6362.972    ± 206.710  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6557.758     ± 37.251  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6348.190     ± 15.360  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        730.638     ± 26.998  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        659.012     ± 83.534  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5721.700   ± 1659.644  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2647.765    ± 163.697  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4413.682     ± 22.250  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23795.964    ± 686.798  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23234.118    ± 406.665  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481321.824   ± 7038.669  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     488009.179   ± 4847.592  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     501945.444  ± 11031.946  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     513980.590   ± 4563.072  ops/s
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

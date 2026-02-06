# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-06T04:23:26Z
- **Commit:** [`0c9d5bf`](https://github.com/prometheus/client_java/commit/0c9d5bf33d6debfab8965146db3997df91516193)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.60K | ± 715.63 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.21K | ± 146.45 | ops/s | 1.2x slower |
| prometheusAdd | 51.62K | ± 328.73 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 45.53K | ± 7.44K | ops/s | 1.5x slower |
| simpleclientNoLabelsInc | 6.70K | ± 26.73 | ops/s | 9.9x slower |
| simpleclientAdd | 6.54K | ± 21.12 | ops/s | 10x slower |
| simpleclientInc | 6.49K | ± 102.66 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.31K | ± 195.04 | ops/s | 51x slower |
| openTelemetryAdd | 1.29K | ± 51.78 | ops/s | 52x slower |
| openTelemetryInc | 1.28K | ± 40.75 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.03K | ± 191.82 | ops/s | **fastest** |
| simpleclient | 4.53K | ± 25.12 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 106.19 | ops/s | 1.6x slower |
| openTelemetryClassic | 675.57 | ± 17.27 | ops/s | 7.4x slower |
| openTelemetryExponential | 524.14 | ± 42.28 | ops/s | 9.6x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 493.40K | ± 3.68K | ops/s | **fastest** |
| prometheusWriteToByteArray | 488.35K | ± 2.88K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 477.56K | ± 5.09K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.97K | ± 3.75K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      45527.102   ± 7435.600  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1291.537     ± 51.778  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1277.790     ± 40.747  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1313.369    ± 195.038  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51623.645    ± 328.732  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66603.485    ± 715.629  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57213.503    ± 146.447  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6535.514     ± 21.123  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6492.676    ± 102.661  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6696.718     ± 26.729  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        675.573     ± 17.267  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        524.137     ± 42.277  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5030.684    ± 191.823  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3063.738    ± 106.193  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4532.659     ± 25.120  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473974.888   ± 3751.353  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     477559.382   ± 5090.178  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     488348.098   ± 2876.028  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     493395.485   ± 3679.522  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-14T04:28:34Z
- **Commit:** [`5966d1d`](https://github.com/prometheus/client_java/commit/5966d1d4fdfc30e3a7eb09b0a88da5b2e9dc07c5)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.22K | ± 267.92 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.66K | ± 287.20 | ops/s | 1.2x slower |
| prometheusAdd | 50.52K | ± 439.81 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.63K | ± 2.03K | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 28.30 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 10.14 | ops/s | 10x slower |
| simpleclientAdd | 6.29K | ± 215.36 | ops/s | 11x slower |
| openTelemetryAdd | 3.60K | ± 442.37 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.23K | ± 281.40 | ops/s | 21x slower |
| openTelemetryInc | 3.04K | ± 239.69 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassic | 4.41K | ± 245.60 | ops/s | **fastest** |
| simpleclient | 4.41K | ± 33.14 | ops/s | 1.0x slower |
| prometheusNative | 2.66K | ± 86.21 | ops/s | 1.7x slower |
| openTelemetryClassic | 752.91 | ± 23.89 | ops/s | 5.9x slower |
| openTelemetryExponential | 649.17 | ± 66.44 | ops/s | 6.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.30K | ± 1.55K | ops/s | **fastest** |
| prometheusWriteToNull | 22.65K | ± 1.25K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToByteArray | 503.17K | ± 3.95K | ops/s | **fastest** |
| prometheusWriteToNull | 501.80K | ± 4.10K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.31K | ± 5.05K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 474.63K | ± 5.73K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49627.614   ± 2029.752  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3596.160    ± 442.373  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3037.074    ± 239.686  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3228.350    ± 281.401  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50522.174    ± 439.805  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66221.761    ± 267.916  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56659.833    ± 287.202  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6292.213    ± 215.361  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6555.756     ± 28.300  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6341.269     ± 10.141  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        752.909     ± 23.894  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        649.170     ± 66.444  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4411.876    ± 245.598  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2662.041     ± 86.211  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4406.870     ± 33.136  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23302.332   ± 1550.510  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22646.392   ± 1250.218  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     474625.537   ± 5726.777  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482308.912   ± 5050.092  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     503173.524   ± 3946.191  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     501799.190   ± 4101.628  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

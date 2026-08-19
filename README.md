# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-19T03:56:52Z
- **Commit:** [`c8e2c03`](https://github.com/prometheus/client_java/commit/c8e2c03788424cac089ff2772463f00fa6f33afa)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.45K | ± 725.92 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.39K | ± 1.10K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.25K | ± 1.52K | ops/s | 1.4x slower |
| prometheusAdd | 48.21K | ± 4.95K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 18.54K | ± 76.89 | ops/s | 3.5x slower |
| openTelemetryInc | 14.21K | ± 729.46 | ops/s | 4.6x slower |
| openTelemetryAdd | 12.75K | ± 360.03 | ops/s | 5.1x slower |
| simpleclientInc | 6.60K | ± 85.40 | ops/s | 9.9x slower |
| simpleclientAdd | 6.44K | ± 27.73 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.35K | ± 36.66 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.29K | ± 41.05 | ops/s | **fastest** |
| prometheusClassic | 6.05K | ± 1.79K | ops/s | 2.0x slower |
| prometheusClassicSingleThread | 4.57K | ± 45.13 | ops/s | 2.7x slower |
| simpleclient | 4.44K | ± 42.33 | ops/s | 2.8x slower |
| prometheusNative | 2.98K | ± 269.22 | ops/s | 4.1x slower |
| openTelemetryClassic | 796.21 | ± 23.00 | ops/s | 15x slower |
| openTelemetryExponential | 767.93 | ± 174.03 | ops/s | 16x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 24.17K | ± 384.06 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.59K | ± 1.13K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 502.61K | ± 2.20K | ops/s | **fastest** |
| prometheusWriteToByteArray | 495.77K | ± 4.81K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 484.94K | ± 2.56K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 477.89K | ± 3.24K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48246.185   ± 1521.542  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12753.069    ± 360.034  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14214.523    ± 729.464  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18537.061     ± 76.892  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48208.219   ± 4954.709  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65446.224    ± 725.924  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56386.494   ± 1101.961  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6439.590     ± 27.733  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6596.331     ± 85.400  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6353.114     ± 36.661  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        796.214     ± 22.997  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        767.926    ± 174.029  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6047.072   ± 1790.090  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12292.857     ± 41.048  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4567.046     ± 45.126  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2980.421    ± 269.217  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4441.234     ± 42.326  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23586.182   ± 1134.361  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24171.609    ± 384.058  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477893.205   ± 3235.450  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     484935.789   ± 2559.521  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     495766.560   ± 4809.775  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502610.295   ± 2200.113  ops/s
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

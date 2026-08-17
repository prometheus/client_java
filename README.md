# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-17T03:58:35Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.21K | ± 1.52K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.21K | ± 976.97 | ops/s | 1.2x slower |
| prometheusAdd | 51.03K | ± 760.03 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.25K | ± 95.54 | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.35K | ± 137.77 | ops/s | 3.6x slower |
| openTelemetryInc | 14.85K | ± 280.41 | ops/s | 4.4x slower |
| openTelemetryAdd | 12.82K | ± 141.07 | ops/s | 5.1x slower |
| simpleclientInc | 6.56K | ± 33.81 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.35K | ± 11.52 | ops/s | 10x slower |
| simpleclientAdd | 6.22K | ± 352.81 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.29K | ± 37.77 | ops/s | **fastest** |
| prometheusClassic | 6.41K | ± 1.42K | ops/s | 1.9x slower |
| prometheusClassicSingleThread | 4.52K | ± 59.69 | ops/s | 2.7x slower |
| simpleclient | 4.37K | ± 49.13 | ops/s | 2.8x slower |
| prometheusNative | 3.05K | ± 375.07 | ops/s | 4.0x slower |
| openTelemetryClassic | 873.92 | ± 58.15 | ops/s | 14x slower |
| openTelemetryExponential | 869.28 | ± 142.25 | ops/s | 14x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 24.08K | ± 527.29 | ops/s | **fastest** |
| prometheusWriteToNull | 24.08K | ± 126.73 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToByteArray | 477.94K | ± 2.39K | ops/s | **fastest** |
| prometheusWriteToNull | 476.76K | ± 3.72K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 453.39K | ± 3.72K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 443.92K | ± 4.59K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50249.155     ± 95.540  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12818.221    ± 141.071  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14850.403    ± 280.411  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18354.161    ± 137.765  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51030.869    ± 760.029  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65210.318   ± 1515.475  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56210.591    ± 976.971  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6221.488    ± 352.806  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6558.370     ± 33.806  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6352.599     ± 11.520  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        873.922     ± 58.150  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        869.277    ± 142.247  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6407.952   ± 1422.959  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12289.839     ± 37.766  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4516.352     ± 59.692  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3049.730    ± 375.065  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4373.067     ± 49.133  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24082.766    ± 527.293  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24078.335    ± 126.734  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     443917.108   ± 4589.526  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     453390.224   ± 3724.456  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     477937.097   ± 2393.348  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     476761.219   ± 3716.675  ops/s
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

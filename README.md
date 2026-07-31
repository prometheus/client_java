# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-31T04:38:30Z
- **Commit:** [`3404554`](https://github.com/prometheus/client_java/commit/34045542970750463b2956e426388fdaca0d3b07)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.13K | ± 334.31 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.77K | ± 373.27 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.12K | ± 1.81K | ops/s | 1.3x slower |
| prometheusAdd | 48.72K | ± 3.68K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 18.44K | ± 143.80 | ops/s | 3.6x slower |
| openTelemetryInc | 15.42K | ± 230.82 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.99K | ± 103.16 | ops/s | 5.1x slower |
| simpleclientInc | 6.56K | ± 39.00 | ops/s | 10x slower |
| simpleclientAdd | 6.43K | ± 26.20 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 6.01 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.38K | ± 163.09 | ops/s | **fastest** |
| prometheusClassic | 7.44K | ± 2.13K | ops/s | 1.7x slower |
| simpleclient | 4.43K | ± 70.72 | ops/s | 2.8x slower |
| prometheusClassicSingleThread | 4.24K | ± 642.52 | ops/s | 2.9x slower |
| prometheusNative | 2.93K | ± 370.72 | ops/s | 4.2x slower |
| openTelemetryClassic | 770.13 | ± 39.74 | ops/s | 16x slower |
| openTelemetryExponential | 737.38 | ± 144.26 | ops/s | 17x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.78K | ± 309.72 | ops/s | **fastest** |
| prometheusWriteToNull | 22.82K | ± 1.10K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 513.10K | ± 4.96K | ops/s | **fastest** |
| prometheusWriteToByteArray | 502.29K | ± 3.86K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.17K | ± 6.40K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 479.16K | ± 3.55K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49124.363   ± 1810.868  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12991.201    ± 103.158  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15423.752    ± 230.820  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18437.346    ± 143.795  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48718.384   ± 3684.770  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66129.502    ± 334.313  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56774.456    ± 373.272  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6432.843     ± 26.203  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6561.213     ± 39.003  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6340.663      ± 6.012  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        770.133     ± 39.739  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        737.379    ± 144.263  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7441.517   ± 2127.912  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12375.560    ± 163.089  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4238.419    ± 642.525  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2926.422    ± 370.716  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4430.921     ± 70.722  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23780.635    ± 309.717  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22816.773   ± 1104.324  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479160.973   ± 3548.681  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487169.197   ± 6395.558  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     502291.548   ± 3863.557  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     513100.119   ± 4958.929  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-17T04:29:22Z
- **Commit:** [`8d91443`](https://github.com/prometheus/client_java/commit/8d91443665952d8a2585a9e2f220a5811ef2a051)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusNoLabelsInc | 28.40K | ± 933.40 | ops/s | **fastest** |
| codahaleIncNoLabels | 28.14K | ± 1.67K | ops/s | 1.0x slower |
| prometheusInc | 27.36K | ± 631.31 | ops/s | 1.0x slower |
| prometheusAdd | 26.19K | ± 687.28 | ops/s | 1.1x slower |
| simpleclientInc | 7.15K | ± 181.86 | ops/s | 4.0x slower |
| simpleclientAdd | 7.11K | ± 278.05 | ops/s | 4.0x slower |
| simpleclientNoLabelsInc | 6.67K | ± 114.35 | ops/s | 4.3x slower |
| openTelemetryIncNoLabels | 2.47K | ± 480.26 | ops/s | 11x slower |
| openTelemetryAdd | 2.13K | ± 163.85 | ops/s | 13x slower |
| openTelemetryInc | 2.07K | ± 171.29 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| simpleclient | 4.49K | ± 97.52 | ops/s | **fastest** |
| prometheusClassic | 3.36K | ± 361.72 | ops/s | 1.3x slower |
| prometheusNative | 1.93K | ± 344.93 | ops/s | 2.3x slower |
| openTelemetryClassic | 442.45 | ± 25.59 | ops/s | 10x slower |
| openTelemetryExponential | 344.24 | ± 24.75 | ops/s | 13x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 17.94K | ± 236.08 | ops/s | **fastest** |
| openMetricsWriteToNull | 17.69K | ± 224.51 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 316.80K | ± 5.60K | ops/s | **fastest** |
| prometheusWriteToByteArray | 306.76K | ± 5.39K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 288.53K | ± 5.75K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 279.47K | ± 5.30K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28143.526   ± 1665.245  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2125.755    ± 163.853  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2070.604    ± 171.288  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2470.367    ± 480.258  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      26191.127    ± 687.277  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      27360.580    ± 631.305  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      28401.927    ± 933.398  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7105.005    ± 278.051  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7145.816    ± 181.857  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6670.386    ± 114.351  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        442.449     ± 25.592  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        344.237     ± 24.748  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3355.862    ± 361.719  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1929.781    ± 344.929  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4487.263     ± 97.517  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      17690.531    ± 224.512  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17941.580    ± 236.083  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     279468.028   ± 5301.972  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     288530.195   ± 5746.784  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     306763.472   ± 5386.970  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     316800.901   ± 5604.775  ops/s
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

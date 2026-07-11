# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-11T04:29:06Z
- **Commit:** [`1bb6c6d`](https://github.com/prometheus/client_java/commit/1bb6c6dfc3a31b410b135baa12ee8ee7671897bc)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| codahaleIncNoLabels | 28.92K | ± 291.98 | ops/s | **fastest** |
| prometheusInc | 28.13K | ± 1.14K | ops/s | 1.0x slower |
| prometheusNoLabelsInc | 28.05K | ± 536.94 | ops/s | 1.0x slower |
| prometheusAdd | 26.28K | ± 704.37 | ops/s | 1.1x slower |
| simpleclientInc | 6.81K | ± 105.61 | ops/s | 4.2x slower |
| simpleclientAdd | 6.77K | ± 67.66 | ops/s | 4.3x slower |
| simpleclientNoLabelsInc | 6.77K | ± 20.60 | ops/s | 4.3x slower |
| openTelemetryIncNoLabels | 2.55K | ± 323.03 | ops/s | 11x slower |
| openTelemetryInc | 2.35K | ± 193.51 | ops/s | 12x slower |
| openTelemetryAdd | 2.21K | ± 233.25 | ops/s | 13x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| simpleclient | 4.44K | ± 47.19 | ops/s | **fastest** |
| prometheusClassic | 2.86K | ± 273.53 | ops/s | 1.6x slower |
| prometheusNative | 1.78K | ± 123.56 | ops/s | 2.5x slower |
| openTelemetryClassic | 440.44 | ± 40.18 | ops/s | 10x slower |
| openTelemetryExponential | 325.14 | ± 31.54 | ops/s | 14x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 18.39K | ± 159.92 | ops/s | **fastest** |
| prometheusWriteToNull | 18.32K | ± 82.09 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 301.69K | ± 2.15K | ops/s | **fastest** |
| prometheusWriteToByteArray | 296.50K | ± 1.39K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 278.55K | ± 1.94K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 275.53K | ± 1.47K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28917.707    ± 291.982  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2206.461    ± 233.246  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2347.583    ± 193.515  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2553.796    ± 323.034  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      26278.863    ± 704.373  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      28130.811   ± 1137.831  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      28048.180    ± 536.939  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6774.636     ± 67.657  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6814.403    ± 105.611  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6766.803     ± 20.596  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        440.439     ± 40.180  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        325.142     ± 31.538  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2856.379    ± 273.532  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1777.294    ± 123.562  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4442.504     ± 47.190  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18393.598    ± 159.917  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18315.271     ± 82.089  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     275531.396   ± 1466.121  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     278547.723   ± 1942.190  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     296501.185   ± 1394.913  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     301685.868   ± 2154.117  ops/s
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

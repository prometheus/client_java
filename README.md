# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-18T04:29:58Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusNoLabelsInc | 31.38K | ± 271.00 | ops/s | **fastest** |
| prometheusInc | 30.57K | ± 1.60K | ops/s | 1.0x slower |
| codahaleIncNoLabels | 30.51K | ± 701.52 | ops/s | 1.0x slower |
| prometheusAdd | 28.60K | ± 129.16 | ops/s | 1.1x slower |
| simpleclientInc | 6.86K | ± 31.89 | ops/s | 4.6x slower |
| simpleclientNoLabelsInc | 6.76K | ± 237.00 | ops/s | 4.6x slower |
| simpleclientAdd | 6.37K | ± 268.47 | ops/s | 4.9x slower |
| openTelemetryInc | 1.36K | ± 114.75 | ops/s | 23x slower |
| openTelemetryIncNoLabels | 1.31K | ± 98.14 | ops/s | 24x slower |
| openTelemetryAdd | 1.25K | ± 30.94 | ops/s | 25x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.46K | ± 74.48 | ops/s | **fastest** |
| prometheusClassic | 3.92K | ± 1.84K | ops/s | 1.1x slower |
| prometheusNative | 2.04K | ± 127.67 | ops/s | 2.2x slower |
| openTelemetryClassic | 498.31 | ± 34.60 | ops/s | 8.9x slower |
| openTelemetryExponential | 377.62 | ± 10.53 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 313.31K | ± 1.49K | ops/s | **fastest** |
| prometheusWriteToByteArray | 310.65K | ± 1.67K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 294.07K | ± 1.51K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 291.20K | ± 1.55K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      30509.330    ± 701.521  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1251.467     ± 30.940  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1358.228    ± 114.753  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1307.426     ± 98.144  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28597.074    ± 129.157  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30570.876   ± 1603.660  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31384.839    ± 270.999  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6370.043    ± 268.468  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6857.902     ± 31.892  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6763.164    ± 237.003  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        498.308     ± 34.595  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        377.620     ± 10.526  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3920.355   ± 1842.076  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2041.987    ± 127.669  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4455.561     ± 74.483  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     291196.614   ± 1551.240  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     294071.507   ± 1514.475  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     310650.663   ± 1669.030  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     313310.878   ± 1490.552  ops/s
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

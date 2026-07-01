# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-01T04:38:42Z
- **Commit:** [`2a2c73d`](https://github.com/prometheus/client_java/commit/2a2c73d7d23bfa291b10df85056027398e8a868d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.92K | ± 452.45 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.64K | ± 307.85 | ops/s | 1.2x slower |
| prometheusAdd | 51.04K | ± 275.95 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.10K | ± 1.46K | ops/s | 1.3x slower |
| simpleclientInc | 6.60K | ± 87.04 | ops/s | 10.0x slower |
| simpleclientAdd | 6.50K | ± 49.93 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.35K | ± 12.84 | ops/s | 10x slower |
| openTelemetryAdd | 3.59K | ± 491.13 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.34K | ± 480.56 | ops/s | 20x slower |
| openTelemetryInc | 3.21K | ± 181.92 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.03K | ± 1.66K | ops/s | **fastest** |
| simpleclient | 4.39K | ± 14.46 | ops/s | 1.4x slower |
| prometheusNative | 3.16K | ± 150.55 | ops/s | 1.9x slower |
| openTelemetryClassic | 771.41 | ± 29.66 | ops/s | 7.8x slower |
| openTelemetryExponential | 614.82 | ± 78.38 | ops/s | 9.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 23.53K | ± 340.50 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.36K | ± 766.32 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 523.71K | ± 7.28K | ops/s | **fastest** |
| prometheusWriteToByteArray | 515.35K | ± 2.31K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 497.42K | ± 1.92K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 493.27K | ± 3.84K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49100.745   ± 1464.802  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3590.911    ± 491.135  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3205.427    ± 181.923  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3340.716    ± 480.558  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51036.397    ± 275.951  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65923.332    ± 452.449  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56641.979    ± 307.849  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6497.846     ± 49.929  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6597.193     ± 87.040  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6348.832     ± 12.844  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        771.414     ± 29.661  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        614.815     ± 78.376  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6029.135   ± 1657.827  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3162.805    ± 150.547  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4392.562     ± 14.457  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23362.854    ± 766.318  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23527.534    ± 340.504  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     493274.715   ± 3842.993  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     497423.170   ± 1918.017  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     515354.597   ± 2307.130  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     523707.032   ± 7282.061  ops/s
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

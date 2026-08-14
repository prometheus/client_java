# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-14T04:26:18Z
- **Commit:** [`90f99d6`](https://github.com/prometheus/client_java/commit/90f99d635109472d8ccca304f044f93a1b0f1436)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 64.93K | ± 1.20K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.65K | ± 270.42 | ops/s | 1.1x slower |
| prometheusAdd | 50.69K | ± 80.16 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.19K | ± 96.11 | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.30K | ± 407.39 | ops/s | 3.5x slower |
| openTelemetryInc | 15.10K | ± 457.00 | ops/s | 4.3x slower |
| openTelemetryAdd | 13.00K | ± 31.22 | ops/s | 5.0x slower |
| simpleclientInc | 6.61K | ± 84.29 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.35K | ± 44.11 | ops/s | 10x slower |
| simpleclientAdd | 6.14K | ± 364.42 | ops/s | 11x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.30K | ± 34.14 | ops/s | **fastest** |
| prometheusClassic | 6.31K | ± 1.44K | ops/s | 1.9x slower |
| prometheusClassicSingleThread | 4.58K | ± 37.68 | ops/s | 2.7x slower |
| simpleclient | 4.41K | ± 63.16 | ops/s | 2.8x slower |
| prometheusNative | 2.96K | ± 277.55 | ops/s | 4.2x slower |
| openTelemetryClassic | 811.84 | ± 86.75 | ops/s | 15x slower |
| openTelemetryExponential | 655.44 | ± 5.95 | ops/s | 19x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.31K | ± 248.99 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.29K | ± 621.55 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 490.81K | ± 3.08K | ops/s | **fastest** |
| prometheusWriteToByteArray | 483.92K | ± 2.48K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 461.49K | ± 3.37K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 459.39K | ± 7.16K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50186.878     ± 96.113  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12998.446     ± 31.225  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15098.525    ± 457.003  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18301.478    ± 407.392  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50689.884     ± 80.160  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64931.159   ± 1200.872  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56652.286    ± 270.419  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6142.725    ± 364.420  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6605.185     ± 84.291  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6352.994     ± 44.108  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        811.837     ± 86.748  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        655.439      ± 5.949  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6308.228   ± 1440.959  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12295.894     ± 34.143  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4580.704     ± 37.679  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2959.645    ± 277.555  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4413.918     ± 63.156  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23289.297    ± 621.551  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23305.713    ± 248.991  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     459392.596   ± 7155.118  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     461490.870   ± 3371.352  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     483923.554   ± 2475.182  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     490813.613   ± 3081.417  ops/s
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

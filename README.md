# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-24T04:37:34Z
- **Commit:** [`07623c1`](https://github.com/prometheus/client_java/commit/07623c14dedfeffed3eace5b8718127add250668)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.39K | ± 93.68 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.12K | ± 990.17 | ops/s | 1.2x slower |
| prometheusAdd | 51.35K | ± 212.10 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.02K | ± 7.79K | ops/s | 1.5x slower |
| openTelemetryIncNoLabels | 18.70K | ± 174.84 | ops/s | 3.6x slower |
| openTelemetryInc | 14.82K | ± 309.29 | ops/s | 4.5x slower |
| openTelemetryAdd | 12.00K | ± 1.09K | ops/s | 5.5x slower |
| simpleclientInc | 6.53K | ± 41.81 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 12.75 | ops/s | 10x slower |
| simpleclientAdd | 6.29K | ± 174.81 | ops/s | 11x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.67K | ± 39.89 | ops/s | **fastest** |
| prometheusClassic | 6.72K | ± 562.37 | ops/s | 1.9x slower |
| prometheusClassicSingleThread | 4.58K | ± 26.72 | ops/s | 2.8x slower |
| simpleclient | 4.37K | ± 22.58 | ops/s | 2.9x slower |
| prometheusNative | 2.78K | ± 257.15 | ops/s | 4.6x slower |
| openTelemetryClassic | 794.26 | ± 9.13 | ops/s | 16x slower |
| openTelemetryExponential | 693.13 | ± 78.61 | ops/s | 18x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 24.36K | ± 583.84 | ops/s | **fastest** |
| openMetricsWriteToNull | 24.28K | ± 502.04 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 499.71K | ± 4.20K | ops/s | **fastest** |
| prometheusWriteToByteArray | 494.70K | ± 4.53K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.68K | ± 5.11K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.35K | ± 5.96K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44015.424   ± 7787.198  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      11998.291   ± 1087.327  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14824.840    ± 309.295  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18697.626    ± 174.836  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51350.247    ± 212.098  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66390.521     ± 93.681  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56123.559    ± 990.165  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6286.038    ± 174.813  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6534.566     ± 41.813  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6341.706     ± 12.753  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        794.261      ± 9.128  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        693.130     ± 78.608  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6724.971    ± 562.369  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12669.518     ± 39.890  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4584.332     ± 26.718  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2781.162    ± 257.146  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4372.179     ± 22.584  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24282.295    ± 502.035  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24358.847    ± 583.838  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475348.692   ± 5955.302  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487677.102   ± 5114.085  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     494696.132   ± 4528.657  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     499714.619   ± 4195.681  ops/s
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

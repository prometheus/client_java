# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-30T04:37:05Z
- **Commit:** [`ed46db8`](https://github.com/prometheus/client_java/commit/ed46db842f136bac47e29bd23fbc335e81eadd53)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| codahaleIncNoLabels | 31.47K | ± 313.22 | ops/s | **fastest** |
| prometheusNoLabelsInc | 31.00K | ± 109.46 | ops/s | 1.0x slower |
| prometheusInc | 30.90K | ± 142.87 | ops/s | 1.0x slower |
| prometheusAdd | 29.97K | ± 150.25 | ops/s | 1.1x slower |
| openTelemetryIncNoLabels | 19.80K | ± 172.08 | ops/s | 1.6x slower |
| openTelemetryInc | 17.59K | ± 103.89 | ops/s | 1.8x slower |
| openTelemetryAdd | 15.42K | ± 50.36 | ops/s | 2.0x slower |
| simpleclientInc | 7.82K | ± 53.75 | ops/s | 4.0x slower |
| simpleclientNoLabelsInc | 7.70K | ± 67.12 | ops/s | 4.1x slower |
| simpleclientAdd | 7.68K | ± 99.32 | ops/s | 4.1x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 7.68K | ± 218.08 | ops/s | **fastest** |
| simpleclient | 5.04K | ± 14.90 | ops/s | 1.5x slower |
| prometheusClassicSingleThread | 3.42K | ± 58.72 | ops/s | 2.2x slower |
| prometheusClassic | 3.07K | ± 496.09 | ops/s | 2.5x slower |
| prometheusNative | 2.24K | ± 488.61 | ops/s | 3.4x slower |
| openTelemetryExponential | 516.73 | ± 52.51 | ops/s | 15x slower |
| openTelemetryClassic | 513.08 | ± 20.83 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 20.70K | ± 62.18 | ops/s | **fastest** |
| prometheusWriteToNull | 20.60K | ± 168.48 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 349.04K | ± 6.40K | ops/s | **fastest** |
| prometheusWriteToByteArray | 344.79K | ± 8.34K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 323.64K | ± 10.53K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 316.64K | ± 5.31K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      31466.158    ± 313.221  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15419.354     ± 50.356  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17589.505    ± 103.894  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      19795.937    ± 172.084  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      29965.332    ± 150.254  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30900.536    ± 142.870  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30997.864    ± 109.456  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7677.687     ± 99.325  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7816.181     ± 53.747  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7700.985     ± 67.118  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        513.085     ± 20.834  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        516.733     ± 52.513  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3070.975    ± 496.088  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7681.931    ± 218.081  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3416.624     ± 58.724  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2237.437    ± 488.613  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5039.183     ± 14.905  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      20699.181     ± 62.175  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      20604.933    ± 168.476  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     316642.249   ± 5313.319  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     323642.041  ± 10529.896  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     344793.654   ± 8338.919  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     349039.337   ± 6404.626  ops/s
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

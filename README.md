# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-25T04:36:19Z
- **Commit:** [`7c081da`](https://github.com/prometheus/client_java/commit/7c081da30c522abb0b931d3800bbc5f3b2904ad4)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.33K | ± 1.40K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.65K | ± 667.40 | ops/s | 1.2x slower |
| prometheusAdd | 51.61K | ± 271.11 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.06K | ± 1.69K | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.59K | ± 51.03 | ops/s | 3.5x slower |
| openTelemetryInc | 14.38K | ± 659.46 | ops/s | 4.5x slower |
| openTelemetryAdd | 13.00K | ± 40.23 | ops/s | 5.0x slower |
| simpleclientInc | 6.57K | ± 41.58 | ops/s | 10.0x slower |
| simpleclientAdd | 6.46K | ± 42.72 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.38K | ± 36.09 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.46K | ± 234.64 | ops/s | **fastest** |
| prometheusClassic | 6.09K | ± 1.48K | ops/s | 2.0x slower |
| prometheusClassicSingleThread | 4.55K | ± 71.05 | ops/s | 2.7x slower |
| simpleclient | 4.49K | ± 42.25 | ops/s | 2.8x slower |
| prometheusNative | 2.91K | ± 289.85 | ops/s | 4.3x slower |
| openTelemetryClassic | 821.58 | ± 27.07 | ops/s | 15x slower |
| openTelemetryExponential | 741.82 | ± 145.37 | ops/s | 17x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 24.01K | ± 1.10K | ops/s | **fastest** |
| openMetricsWriteToNull | 23.81K | ± 141.45 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 505.28K | ± 3.72K | ops/s | **fastest** |
| prometheusWriteToByteArray | 490.59K | ± 6.95K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.31K | ± 5.56K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.61K | ± 4.74K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49055.970   ± 1690.812  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12998.838     ± 40.234  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14377.500    ± 659.460  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18592.930     ± 51.029  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51613.048    ± 271.105  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65334.391   ± 1404.160  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56645.334    ± 667.396  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6457.450     ± 42.716  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6566.243     ± 41.579  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6384.532     ± 36.088  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        821.585     ± 27.073  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        741.816    ± 145.368  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6087.118   ± 1476.853  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12459.982    ± 234.642  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4553.337     ± 71.046  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2909.502    ± 289.846  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4487.578     ± 42.249  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23805.289    ± 141.448  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24009.063   ± 1096.945  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473614.055   ± 4741.680  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485313.376   ± 5561.760  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     490591.221   ± 6952.492  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     505283.515   ± 3724.052  ops/s
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

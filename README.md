# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-01T04:36:51Z
- **Commit:** [`922943c`](https://github.com/prometheus/client_java/commit/922943cfe12acb5e373a0a6152384673c3c7b6dc)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 76.48K | ± 583.78 | ops/s | **fastest** |
| prometheusNoLabelsInc | 66.48K | ± 695.05 | ops/s | 1.2x slower |
| prometheusAdd | 61.70K | ± 109.19 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 56.04K | ± 2.68K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 22.14K | ± 158.10 | ops/s | 3.5x slower |
| openTelemetryInc | 17.45K | ± 278.63 | ops/s | 4.4x slower |
| openTelemetryAdd | 15.75K | ± 23.46 | ops/s | 4.9x slower |
| simpleclientInc | 7.94K | ± 138.54 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 7.62K | ± 40.02 | ops/s | 10x slower |
| simpleclientAdd | 7.61K | ± 447.75 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 18.13K | ± 37.58 | ops/s | **fastest** |
| prometheusClassic | 7.96K | ± 2.28K | ops/s | 2.3x slower |
| prometheusClassicSingleThread | 7.49K | ± 28.78 | ops/s | 2.4x slower |
| simpleclient | 5.79K | ± 142.98 | ops/s | 3.1x slower |
| prometheusNative | 3.90K | ± 316.02 | ops/s | 4.7x slower |
| openTelemetryClassic | 1.13K | ± 38.59 | ops/s | 16x slower |
| openTelemetryExponential | 797.12 | ± 42.78 | ops/s | 23x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 35.19K | ± 312.04 | ops/s | **fastest** |
| openMetricsWriteToNull | 35.18K | ± 379.50 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 703.12K | ± 2.14K | ops/s | **fastest** |
| prometheusWriteToByteArray | 689.22K | ± 7.40K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 660.18K | ± 4.54K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 649.34K | ± 3.10K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      56036.710   ± 2681.841  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15749.869     ± 23.463  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17452.958    ± 278.633  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22136.335    ± 158.096  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      61700.718    ± 109.186  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      76480.543    ± 583.779  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66475.831    ± 695.045  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7614.154    ± 447.753  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7940.598    ± 138.542  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7621.497     ± 40.019  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       1127.857     ± 38.586  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        797.122     ± 42.785  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7957.921   ± 2276.350  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      18132.201     ± 37.582  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7493.828     ± 28.784  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3897.026    ± 316.019  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5791.187    ± 142.983  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35177.707    ± 379.496  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35191.992    ± 312.036  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     649335.829   ± 3099.579  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     660178.945   ± 4544.401  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     689222.300   ± 7403.960  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     703124.788   ± 2136.358  ops/s
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

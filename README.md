# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-18T04:27:43Z
- **Commit:** [`8d91443`](https://github.com/prometheus/client_java/commit/8d91443665952d8a2585a9e2f220a5811ef2a051)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 76.95K | ± 484.83 | ops/s | **fastest** |
| prometheusNoLabelsInc | 65.37K | ± 534.11 | ops/s | 1.2x slower |
| prometheusAdd | 61.83K | ± 457.23 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 57.94K | ± 1.28K | ops/s | 1.3x slower |
| simpleclientInc | 7.91K | ± 77.35 | ops/s | 9.7x slower |
| simpleclientAdd | 7.84K | ± 45.44 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 7.76K | ± 231.68 | ops/s | 9.9x slower |
| openTelemetryIncNoLabels | 6.97K | ± 1.76K | ops/s | 11x slower |
| openTelemetryInc | 5.92K | ± 1.46K | ops/s | 13x slower |
| openTelemetryAdd | 4.98K | ± 1.03K | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassic | 6.13K | ± 25.99 | ops/s | **fastest** |
| simpleclient | 5.61K | ± 106.66 | ops/s | 1.1x slower |
| prometheusNative | 3.94K | ± 179.14 | ops/s | 1.6x slower |
| openTelemetryClassic | 905.46 | ± 17.46 | ops/s | 6.8x slower |
| openTelemetryExponential | 683.80 | ± 31.96 | ops/s | 9.0x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 35.43K | ± 185.46 | ops/s | **fastest** |
| prometheusWriteToNull | 34.95K | ± 840.36 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 703.30K | ± 7.85K | ops/s | **fastest** |
| prometheusWriteToByteArray | 682.33K | ± 4.27K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 653.64K | ± 4.64K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 645.21K | ± 4.68K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      57935.663   ± 1278.125  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4975.588   ± 1025.129  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5918.646   ± 1458.875  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       6969.486   ± 1756.364  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      61825.986    ± 457.232  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      76951.687    ± 484.834  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      65370.064    ± 534.107  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7840.556     ± 45.439  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7910.299     ± 77.349  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7759.778    ± 231.685  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        905.465     ± 17.456  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        683.798     ± 31.956  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6129.302     ± 25.992  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3941.276    ± 179.138  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5614.970    ± 106.665  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      35434.144    ± 185.455  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      34945.895    ± 840.365  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     645213.471   ± 4684.208  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     653644.496   ± 4638.291  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     682331.457   ± 4269.091  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     703296.323   ± 7846.506  ops/s
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

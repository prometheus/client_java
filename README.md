# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-26T04:38:06Z
- **Commit:** [`9432fdc`](https://github.com/prometheus/client_java/commit/9432fdc1933611d34d944d4640ebca3bf91f8ee4)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 59.81K | ± 146.39 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.13K | ± 1.14K | ops/s | 1.2x slower |
| prometheusAdd | 48.82K | ± 764.04 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.17K | ± 1.14K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 17.02K | ± 222.25 | ops/s | 3.5x slower |
| openTelemetryInc | 13.86K | ± 538.10 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.16K | ± 22.44 | ops/s | 4.9x slower |
| simpleclientInc | 6.14K | ± 52.49 | ops/s | 9.7x slower |
| simpleclientAdd | 6.02K | ± 158.49 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 5.85K | ± 75.03 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 14.10K | ± 413.33 | ops/s | **fastest** |
| prometheusClassic | 6.02K | ± 1.04K | ops/s | 2.3x slower |
| prometheusClassicSingleThread | 5.93K | ± 15.88 | ops/s | 2.4x slower |
| simpleclient | 4.38K | ± 49.66 | ops/s | 3.2x slower |
| prometheusNative | 2.74K | ± 113.80 | ops/s | 5.1x slower |
| openTelemetryClassic | 827.00 | ± 32.89 | ops/s | 17x slower |
| openTelemetryExponential | 671.51 | ± 18.14 | ops/s | 21x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 27.21K | ± 354.38 | ops/s | **fastest** |
| openMetricsWriteToNull | 26.92K | ± 142.67 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 562.30K | ± 3.14K | ops/s | **fastest** |
| prometheusWriteToByteArray | 551.38K | ± 5.00K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 528.35K | ± 8.55K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 508.74K | ± 4.89K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44170.129   ± 1137.772  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12162.687     ± 22.436  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      13857.571    ± 538.097  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      17018.458    ± 222.251  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48820.593    ± 764.039  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59812.433    ± 146.391  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51129.231   ± 1140.226  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6021.316    ± 158.490  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6135.764     ± 52.487  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5845.118     ± 75.026  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        827.000     ± 32.894  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        671.515     ± 18.138  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6019.307   ± 1041.367  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      14098.534    ± 413.335  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5931.335     ± 15.885  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2739.956    ± 113.801  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4380.899     ± 49.662  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      26920.731    ± 142.673  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27211.168    ± 354.383  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     508741.047   ± 4886.348  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     528346.929   ± 8548.129  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     551380.895   ± 5002.415  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     562297.475   ± 3141.234  ops/s
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

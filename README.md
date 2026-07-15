# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-15T04:29:07Z
- **Commit:** [`be2bc20`](https://github.com/prometheus/client_java/commit/be2bc20fdf941be85a0ad020f5f405af623a7883)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 63.31K | ± 3.82K | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.76K | ± 956.38 | ops/s | 1.1x slower |
| prometheusAdd | 51.66K | ± 97.57 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 47.05K | ± 225.10 | ops/s | 1.3x slower |
| simpleclientInc | 6.59K | ± 87.69 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.44K | ± 129.16 | ops/s | 9.8x slower |
| simpleclientAdd | 6.26K | ± 246.17 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.40K | ± 248.80 | ops/s | 19x slower |
| openTelemetryAdd | 3.37K | ± 373.55 | ops/s | 19x slower |
| openTelemetryInc | 3.17K | ± 185.95 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassic | 5.98K | ± 1.09K | ops/s | **fastest** |
| simpleclient | 4.45K | ± 70.98 | ops/s | 1.3x slower |
| prometheusNative | 3.14K | ± 71.34 | ops/s | 1.9x slower |
| openTelemetryClassic | 777.35 | ± 18.25 | ops/s | 7.7x slower |
| openTelemetryExponential | 638.27 | ± 99.42 | ops/s | 9.4x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.90K | ± 270.19 | ops/s | **fastest** |
| prometheusWriteToNull | 23.87K | ± 1.15K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 508.76K | ± 2.58K | ops/s | **fastest** |
| prometheusWriteToByteArray | 487.88K | ± 5.18K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 476.78K | ± 6.68K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 466.65K | ± 6.84K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47046.515    ± 225.100  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3366.253    ± 373.555  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3173.130    ± 185.954  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3402.730    ± 248.800  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51657.160     ± 97.573  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63311.698   ± 3817.445  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55761.335    ± 956.377  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6264.528    ± 246.171  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6587.923     ± 87.688  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6444.233    ± 129.159  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        777.354     ± 18.252  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        638.266     ± 99.417  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5984.142   ± 1093.386  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3135.729     ± 71.344  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4447.577     ± 70.978  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23895.253    ± 270.189  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23871.111   ± 1153.598  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     466646.331   ± 6840.661  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     476783.539   ± 6680.293  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     487878.804   ± 5181.797  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     508758.233   ± 2576.682  ops/s
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

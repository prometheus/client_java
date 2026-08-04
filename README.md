# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-04T04:37:27Z
- **Commit:** [`922943c`](https://github.com/prometheus/client_java/commit/922943cfe12acb5e373a0a6152384673c3c7b6dc)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.55K | ± 282.25 | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.92K | ± 654.25 | ops/s | 1.2x slower |
| prometheusAdd | 50.92K | ± 742.92 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.95K | ± 1.24K | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 18.54K | ± 62.90 | ops/s | 3.6x slower |
| openTelemetryInc | 14.79K | ± 558.02 | ops/s | 4.5x slower |
| openTelemetryAdd | 13.05K | ± 27.14 | ops/s | 5.1x slower |
| simpleclientInc | 6.55K | ± 154.67 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.52K | ± 132.24 | ops/s | 10x slower |
| simpleclientAdd | 6.45K | ± 9.72 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.28K | ± 19.58 | ops/s | **fastest** |
| prometheusClassic | 4.65K | ± 655.96 | ops/s | 2.6x slower |
| prometheusClassicSingleThread | 4.58K | ± 26.75 | ops/s | 2.7x slower |
| simpleclient | 4.39K | ± 87.20 | ops/s | 2.8x slower |
| prometheusNative | 2.71K | ± 187.52 | ops/s | 4.5x slower |
| openTelemetryExponential | 836.20 | ± 111.83 | ops/s | 15x slower |
| openTelemetryClassic | 783.76 | ± 26.52 | ops/s | 16x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.98K | ± 433.71 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.56K | ± 774.09 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 507.75K | ± 3.14K | ops/s | **fastest** |
| prometheusWriteToByteArray | 501.07K | ± 5.99K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.99K | ± 1.62K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.84K | ± 2.21K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48953.097   ± 1236.993  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      13049.173     ± 27.142  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14789.316    ± 558.022  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18536.654     ± 62.901  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50915.388    ± 742.916  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66548.316    ± 282.251  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55923.302    ± 654.251  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6445.474      ± 9.722  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6545.898    ± 154.674  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6517.258    ± 132.242  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        783.756     ± 26.522  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        836.197    ± 111.835  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4654.185    ± 655.956  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12278.863     ± 19.579  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4581.097     ± 26.754  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2705.645    ± 187.522  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4388.188     ± 87.197  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23556.708    ± 774.094  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23981.432    ± 433.713  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479837.545   ± 2211.708  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487987.330   ± 1617.768  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     501074.913   ± 5993.278  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     507751.823   ± 3137.936  ops/s
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

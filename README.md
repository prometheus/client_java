# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-01T04:41:32Z
- **Commit:** [`8add981`](https://github.com/prometheus/client_java/commit/8add981e2c57d68aa9a8b497b2496f3ef2904d38)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.19K | ± 1.12K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.95K | ± 439.76 | ops/s | 1.1x slower |
| prometheusAdd | 51.39K | ± 306.27 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.99K | ± 1.44K | ops/s | 1.3x slower |
| simpleclientInc | 6.50K | ± 10.78 | ops/s | 10x slower |
| simpleclientAdd | 6.29K | ± 234.28 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.29K | ± 69.78 | ops/s | 10x slower |
| openTelemetryInc | 3.66K | ± 558.47 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.25K | ± 467.90 | ops/s | 20x slower |
| openTelemetryAdd | 3.03K | ± 174.30 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.34K | ± 1.29K | ops/s | **fastest** |
| simpleclient | 4.38K | ± 20.86 | ops/s | 1.2x slower |
| prometheusNative | 3.18K | ± 17.60 | ops/s | 1.7x slower |
| openTelemetryClassic | 749.85 | ± 17.93 | ops/s | 7.1x slower |
| openTelemetryExponential | 665.06 | ± 59.32 | ops/s | 8.0x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.17K | ± 545.11 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.64K | ± 591.86 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 495.64K | ± 2.59K | ops/s | **fastest** |
| prometheusWriteToByteArray | 493.23K | ± 5.65K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 476.69K | ± 7.66K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.74K | ± 7.00K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48987.076   ± 1439.812  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3028.668    ± 174.302  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3657.076    ± 558.467  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3252.763    ± 467.897  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51392.570    ± 306.268  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65190.975   ± 1124.745  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56950.992    ± 439.763  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6294.847    ± 234.277  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6504.837     ± 10.781  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6288.451     ± 69.781  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        749.853     ± 17.928  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        665.064     ± 59.322  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5343.077   ± 1288.996  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3183.111     ± 17.598  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4378.859     ± 20.863  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23642.805    ± 591.857  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24172.653    ± 545.108  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473736.809   ± 6995.594  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     476693.796   ± 7664.144  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     493233.888   ± 5650.864  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     495640.114   ± 2587.014  ops/s
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

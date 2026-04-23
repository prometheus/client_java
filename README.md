# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-23T04:32:03Z
- **Commit:** [`d6be77d`](https://github.com/prometheus/client_java/commit/d6be77d0d1e166debae46fa59c624b80b26a5dca)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1011-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.80K | ± 685.85 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.01K | ± 680.74 | ops/s | 1.2x slower |
| prometheusAdd | 48.70K | ± 965.23 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.06K | ± 121.80 | ops/s | 1.4x slower |
| simpleclientInc | 6.36K | ± 17.95 | ops/s | 9.6x slower |
| simpleclientAdd | 6.07K | ± 191.75 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 5.73K | ± 115.70 | ops/s | 11x slower |
| openTelemetryInc | 5.27K | ± 1.02K | ops/s | 12x slower |
| openTelemetryIncNoLabels | 5.15K | ± 729.54 | ops/s | 12x slower |
| openTelemetryAdd | 4.02K | ± 956.27 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.68K | ± 587.16 | ops/s | **fastest** |
| simpleclient | 4.34K | ± 53.20 | ops/s | 1.1x slower |
| prometheusNative | 3.08K | ± 265.03 | ops/s | 1.5x slower |
| openTelemetryClassic | 742.93 | ± 13.60 | ops/s | 6.3x slower |
| openTelemetryExponential | 575.77 | ± 22.28 | ops/s | 8.1x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 558.53K | ± 3.77K | ops/s | **fastest** |
| prometheusWriteToByteArray | 549.38K | ± 2.11K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 538.96K | ± 2.06K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 530.90K | ± 4.73K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44060.998    ± 121.798  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4019.123    ± 956.269  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5270.457   ± 1020.439  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5149.203    ± 729.542  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48699.467    ± 965.231  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60798.489    ± 685.853  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51012.140    ± 680.741  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6068.956    ± 191.748  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6356.017     ± 17.950  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5726.759    ± 115.700  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        742.926     ± 13.598  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        575.774     ± 22.282  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4676.010    ± 587.161  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3084.061    ± 265.033  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4337.795     ± 53.195  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     530900.401   ± 4730.974  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     538963.464   ± 2058.846  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     549381.849   ± 2108.897  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     558529.963   ± 3769.721  ops/s
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

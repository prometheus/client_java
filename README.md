# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-07T04:15:52Z
- **Commit:** [`e6eb2f9`](https://github.com/prometheus/client_java/commit/e6eb2f91d6da13485a83c4eab5171f510382f800)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.61K | ± 618.12 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.08K | ± 1.05K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.69K | ± 1.34K | ops/s | 1.4x slower |
| prometheusAdd | 46.42K | ± 8.16K | ops/s | 1.4x slower |
| simpleclientInc | 6.69K | ± 127.03 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.57K | ± 181.48 | ops/s | 10x slower |
| simpleclientAdd | 6.27K | ± 212.49 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 1.34K | ± 141.48 | ops/s | 50x slower |
| openTelemetryAdd | 1.32K | ± 63.06 | ops/s | 51x slower |
| openTelemetryInc | 1.24K | ± 4.38 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.51K | ± 1.49K | ops/s | **fastest** |
| simpleclient | 4.53K | ± 59.37 | ops/s | 1.2x slower |
| prometheusNative | 2.83K | ± 314.04 | ops/s | 1.9x slower |
| openTelemetryClassic | 678.67 | ± 23.85 | ops/s | 8.1x slower |
| openTelemetryExponential | 547.92 | ± 44.33 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 489.67K | ± 3.17K | ops/s | **fastest** |
| prometheusWriteToByteArray | 478.16K | ± 7.06K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 475.33K | ± 4.59K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 470.28K | ± 8.19K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48685.839   ± 1338.718  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1318.020     ± 63.063  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1237.700      ± 4.382  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1344.016    ± 141.484  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      46421.619   ± 8157.557  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66611.305    ± 618.125  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56080.392   ± 1049.815  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6272.948    ± 212.494  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6693.859    ± 127.028  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6571.055    ± 181.482  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        678.670     ± 23.850  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        547.915     ± 44.333  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5505.811   ± 1490.930  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2829.026    ± 314.043  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4534.567     ± 59.365  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     470281.488   ± 8186.732  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     475328.477   ± 4585.248  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     478160.575   ± 7062.614  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     489667.164   ± 3165.842  ops/s
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

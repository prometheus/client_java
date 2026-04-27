# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-27T04:34:40Z
- **Commit:** [`dec8e5b`](https://github.com/prometheus/client_java/commit/dec8e5b15a1c48c54be6b81517f2cb334bc0ee60)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.32K | ± 1.51K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.07K | ± 123.64 | ops/s | 1.1x slower |
| prometheusAdd | 50.98K | ± 852.04 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.68K | ± 1.54K | ops/s | 1.3x slower |
| simpleclientInc | 6.69K | ± 17.66 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.60K | ± 14.05 | ops/s | 9.9x slower |
| simpleclientAdd | 6.03K | ± 71.63 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 3.53K | ± 588.53 | ops/s | 19x slower |
| openTelemetryInc | 3.37K | ± 458.06 | ops/s | 19x slower |
| openTelemetryAdd | 2.85K | ± 42.03 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.52K | ± 1.57K | ops/s | **fastest** |
| simpleclient | 4.45K | ± 26.18 | ops/s | 1.2x slower |
| prometheusNative | 3.20K | ± 85.02 | ops/s | 1.7x slower |
| openTelemetryClassic | 778.04 | ± 36.53 | ops/s | 7.1x slower |
| openTelemetryExponential | 589.84 | ± 17.58 | ops/s | 9.4x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 485.18K | ± 3.31K | ops/s | **fastest** |
| prometheusWriteToByteArray | 482.80K | ± 2.58K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 479.71K | ± 1.91K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 465.58K | ± 6.28K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49678.816   ± 1543.640  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2847.345     ± 42.028  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3368.706    ± 458.056  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3527.640    ± 588.532  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50984.257    ± 852.038  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65322.849   ± 1508.347  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57068.934    ± 123.636  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6034.284     ± 71.633  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6688.771     ± 17.655  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6604.475     ± 14.053  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        778.042     ± 36.530  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        589.839     ± 17.584  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5518.872   ± 1574.886  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3201.261     ± 85.015  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4451.229     ± 26.181  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     465575.282   ± 6280.439  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     479705.683   ± 1909.148  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     482803.256   ± 2584.021  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     485182.783   ± 3307.975  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-15T04:41:59Z
- **Commit:** [`9672749`](https://github.com/prometheus/client_java/commit/9672749085f9029ccb7328b3e88e8e78fa29e402)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.12K | ± 1.14K | ops/s | **fastest** |
| prometheusNoLabelsInc | 50.83K | ± 138.83 | ops/s | 1.2x slower |
| prometheusAdd | 48.69K | ± 908.09 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.00K | ± 162.34 | ops/s | 1.4x slower |
| simpleclientInc | 6.20K | ± 93.09 | ops/s | 9.7x slower |
| simpleclientAdd | 6.12K | ± 15.00 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 5.89K | ± 11.04 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 4.62K | ± 1.26K | ops/s | 13x slower |
| openTelemetryInc | 4.50K | ± 897.26 | ops/s | 13x slower |
| openTelemetryAdd | 4.36K | ± 854.76 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.64K | ± 555.90 | ops/s | **fastest** |
| simpleclient | 4.59K | ± 81.35 | ops/s | 1.0x slower |
| prometheusNative | 3.07K | ± 310.26 | ops/s | 1.5x slower |
| openTelemetryClassic | 693.21 | ± 25.48 | ops/s | 6.7x slower |
| openTelemetryExponential | 565.41 | ± 37.72 | ops/s | 8.2x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 27.40K | ± 148.00 | ops/s | **fastest** |
| prometheusWriteToNull | 27.13K | ± 439.87 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 555.38K | ± 10.93K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.33K | ± 6.32K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 523.94K | ± 3.38K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 514.05K | ± 6.29K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43996.960    ± 162.342  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4358.648    ± 854.755  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4499.714    ± 897.263  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4624.073   ± 1256.080  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48691.008    ± 908.090  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60117.044   ± 1144.768  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50827.035    ± 138.826  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6124.792     ± 15.004  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6202.290     ± 93.088  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5891.871     ± 11.042  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        693.213     ± 25.475  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        565.413     ± 37.719  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4642.882    ± 555.903  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3073.699    ± 310.260  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4585.180     ± 81.355  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27404.903    ± 148.001  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27133.362    ± 439.871  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     514052.325   ± 6291.153  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     523936.251   ± 3376.885  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547328.445   ± 6317.663  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     555379.205  ± 10932.197  ops/s
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

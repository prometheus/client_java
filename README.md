# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-03T04:32:23Z
- **Commit:** [`7f899e7`](https://github.com/prometheus/client_java/commit/7f899e79ded325256bd0e444e33696b5f194700d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.29K | ± 174.82 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.23K | ± 992.82 | ops/s | 1.2x slower |
| prometheusAdd | 51.34K | ± 202.80 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.54K | ± 2.06K | ops/s | 1.3x slower |
| simpleclientInc | 6.52K | ± 40.60 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 30.61 | ops/s | 10x slower |
| simpleclientAdd | 6.34K | ± 179.34 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.61K | ± 778.33 | ops/s | 18x slower |
| openTelemetryInc | 3.40K | ± 420.95 | ops/s | 19x slower |
| openTelemetryAdd | 2.97K | ± 105.45 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.54K | ± 582.78 | ops/s | **fastest** |
| simpleclient | 4.42K | ± 77.25 | ops/s | 1.0x slower |
| prometheusNative | 2.85K | ± 261.05 | ops/s | 1.6x slower |
| openTelemetryClassic | 766.29 | ± 10.43 | ops/s | 5.9x slower |
| openTelemetryExponential | 675.65 | ± 74.34 | ops/s | 6.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.81K | ± 984.85 | ops/s | **fastest** |
| prometheusWriteToNull | 23.76K | ± 946.58 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 510.58K | ± 7.81K | ops/s | **fastest** |
| prometheusWriteToByteArray | 498.45K | ± 5.70K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 488.90K | ± 3.73K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 487.59K | ± 2.46K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49537.929   ± 2064.079  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2974.764    ± 105.453  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3403.389    ± 420.949  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3613.718    ± 778.326  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51341.965    ± 202.804  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66289.709    ± 174.821  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56226.550    ± 992.825  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6342.785    ± 179.339  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6524.382     ± 40.605  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6364.308     ± 30.610  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        766.287     ± 10.428  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        675.652     ± 74.343  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4537.508    ± 582.777  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2853.538    ± 261.051  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4424.323     ± 77.251  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23807.502    ± 984.847  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23755.299    ± 946.582  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     487586.226   ± 2457.080  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     488896.836   ± 3728.860  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     498450.094   ± 5704.634  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     510577.601   ± 7807.074  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-31T04:39:21Z
- **Commit:** [`8add981`](https://github.com/prometheus/client_java/commit/8add981e2c57d68aa9a8b497b2496f3ef2904d38)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.73K | ± 477.77 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.48K | ± 449.01 | ops/s | 1.2x slower |
| prometheusAdd | 51.17K | ± 309.69 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 43.93K | ± 7.75K | ops/s | 1.5x slower |
| simpleclientInc | 6.58K | ± 63.23 | ops/s | 10x slower |
| simpleclientAdd | 6.45K | ± 20.15 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 30.96 | ops/s | 10x slower |
| openTelemetryInc | 3.28K | ± 364.47 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.23K | ± 387.46 | ops/s | 21x slower |
| openTelemetryAdd | 3.22K | ± 90.16 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.41K | ± 1.33K | ops/s | **fastest** |
| simpleclient | 4.38K | ± 53.00 | ops/s | 1.2x slower |
| prometheusNative | 2.95K | ± 320.13 | ops/s | 1.8x slower |
| openTelemetryClassic | 766.82 | ± 11.62 | ops/s | 7.1x slower |
| openTelemetryExponential | 700.43 | ± 52.68 | ops/s | 7.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 22.88K | ± 458.77 | ops/s | **fastest** |
| openMetricsWriteToNull | 22.84K | ± 489.14 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 518.15K | ± 3.83K | ops/s | **fastest** |
| prometheusWriteToByteArray | 502.85K | ± 6.75K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 490.68K | ± 2.31K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 489.90K | ± 4.31K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43932.275   ± 7754.876  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3222.785     ± 90.161  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3280.803    ± 364.467  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3225.189    ± 387.455  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51170.455    ± 309.690  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66728.292    ± 477.766  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56476.753    ± 449.006  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6448.603     ± 20.152  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6579.529     ± 63.232  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6363.708     ± 30.959  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        766.818     ± 11.617  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        700.432     ± 52.680  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5410.520   ± 1333.489  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2946.048    ± 320.135  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4375.384     ± 52.999  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      22844.030    ± 489.144  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22880.258    ± 458.767  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     489904.708   ± 4314.871  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490679.001   ± 2309.942  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     502854.574   ± 6747.411  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     518154.941   ± 3834.022  ops/s
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

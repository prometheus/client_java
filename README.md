# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-06T04:32:31Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.12K | ± 3.44K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.30K | ± 1.46K | ops/s | 1.1x slower |
| prometheusAdd | 50.85K | ± 259.68 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.57K | ± 5.47K | ops/s | 1.4x slower |
| simpleclientNoLabelsInc | 6.52K | ± 139.36 | ops/s | 9.8x slower |
| simpleclientInc | 6.46K | ± 151.76 | ops/s | 9.9x slower |
| simpleclientAdd | 6.06K | ± 79.25 | ops/s | 11x slower |
| openTelemetryAdd | 1.31K | ± 82.83 | ops/s | 49x slower |
| openTelemetryInc | 1.24K | ± 12.81 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.22K | ± 8.81 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 8.23K | ± 1.51K | ops/s | **fastest** |
| simpleclient | 4.51K | ± 9.79 | ops/s | 1.8x slower |
| prometheusNative | 2.89K | ± 357.61 | ops/s | 2.8x slower |
| openTelemetryClassic | 704.21 | ± 25.13 | ops/s | 12x slower |
| openTelemetryExponential | 582.98 | ± 9.10 | ops/s | 14x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.02K | ± 2.84K | ops/s | **fastest** |
| prometheusWriteToByteArray | 489.03K | ± 2.35K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.59K | ± 6.34K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 480.37K | ± 6.62K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44569.918   ± 5465.678  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1308.696     ± 82.831  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1235.474     ± 12.814  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1220.429      ± 8.806  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50850.347    ± 259.683  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64122.795   ± 3441.528  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56304.140   ± 1455.697  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6056.297     ± 79.252  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6460.524    ± 151.759  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6524.671    ± 139.359  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        704.215     ± 25.126  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        582.981      ± 9.097  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8228.234   ± 1505.126  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2893.669    ± 357.609  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4514.095      ± 9.788  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     480374.212   ± 6621.999  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482585.553   ± 6341.425  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489034.034   ± 2348.264  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491016.634   ± 2835.865  ops/s
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

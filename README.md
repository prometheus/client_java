# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-13T04:37:27Z
- **Commit:** [`63f82ad`](https://github.com/prometheus/client_java/commit/63f82addfc2f5fc81bdabfbc49ddbc0ecb2874b8)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.07K | ± 49.41 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.62K | ± 904.66 | ops/s | 1.1x slower |
| prometheusAdd | 48.66K | ± 917.78 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.96K | ± 132.99 | ops/s | 1.3x slower |
| simpleclientInc | 6.23K | ± 57.60 | ops/s | 9.5x slower |
| simpleclientNoLabelsInc | 5.89K | ± 14.56 | ops/s | 10x slower |
| simpleclientAdd | 5.89K | ± 196.60 | ops/s | 10x slower |
| openTelemetryInc | 3.87K | ± 298.35 | ops/s | 15x slower |
| openTelemetryIncNoLabels | 3.83K | ± 583.26 | ops/s | 15x slower |
| openTelemetryAdd | 3.22K | ± 285.89 | ops/s | 18x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.86K | ± 1.33K | ops/s | **fastest** |
| simpleclient | 4.52K | ± 37.14 | ops/s | 1.3x slower |
| prometheusNative | 3.01K | ± 265.96 | ops/s | 1.9x slower |
| openTelemetryClassic | 687.98 | ± 19.70 | ops/s | 8.5x slower |
| openTelemetryExponential | 546.50 | ± 11.68 | ops/s | 11x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.57K | ± 99.79 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.36K | ± 221.86 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 559.44K | ± 14.82K | ops/s | **fastest** |
| prometheusWriteToByteArray | 556.15K | ± 5.83K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 536.87K | ± 2.29K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 524.02K | ± 1.90K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43956.429    ± 132.992  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3219.308    ± 285.888  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3870.955    ± 298.346  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3832.477    ± 583.257  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48655.080    ± 917.777  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59070.073     ± 49.412  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51616.275    ± 904.662  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5886.212    ± 196.601  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6225.566     ± 57.599  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5894.948     ± 14.562  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        687.980     ± 19.700  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        546.495     ± 11.682  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5858.395   ± 1330.296  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3013.024    ± 265.956  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4518.086     ± 37.141  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27360.580    ± 221.861  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27566.061     ± 99.794  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     524019.167   ± 1896.664  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     536868.568   ± 2289.102  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     556149.703   ± 5834.833  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     559440.826  ± 14816.487  ops/s
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

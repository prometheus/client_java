# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-13T04:33:21Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.58K | ± 145.30 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.68K | ± 439.72 | ops/s | 1.1x slower |
| prometheusAdd | 49.36K | ± 2.87K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.12K | ± 1.62K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.63K | ± 13.85 | ops/s | 9.6x slower |
| simpleclientInc | 6.46K | ± 201.32 | ops/s | 9.8x slower |
| simpleclientAdd | 6.22K | ± 210.76 | ops/s | 10x slower |
| openTelemetryInc | 1.48K | ± 163.87 | ops/s | 43x slower |
| openTelemetryAdd | 1.28K | ± 58.68 | ops/s | 50x slower |
| openTelemetryIncNoLabels | 1.18K | ± 30.80 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.28K | ± 2.09K | ops/s | **fastest** |
| simpleclient | 4.40K | ± 20.79 | ops/s | 1.2x slower |
| prometheusNative | 2.83K | ± 316.80 | ops/s | 1.9x slower |
| openTelemetryClassic | 669.12 | ± 16.62 | ops/s | 7.9x slower |
| openTelemetryExponential | 557.20 | ± 64.89 | ops/s | 9.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.60K | ± 2.62K | ops/s | **fastest** |
| prometheusWriteToByteArray | 484.44K | ± 3.63K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 478.99K | ± 9.07K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 471.55K | ± 6.96K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48121.353   ± 1622.052  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1283.828     ± 58.677  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1476.873    ± 163.871  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1181.899     ± 30.795  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      49360.814   ± 2869.557  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63578.410    ± 145.297  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56680.288    ± 439.718  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6217.561    ± 210.756  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6455.311    ± 201.323  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6632.150     ± 13.849  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        669.118     ± 16.617  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        557.199     ± 64.890  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5283.313   ± 2089.106  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2829.218    ± 316.802  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4399.290     ± 20.790  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     471547.526   ± 6963.245  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     478989.183   ± 9072.909  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     484441.096   ± 3629.855  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491601.326   ± 2616.365  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-18T04:27:42Z
- **Commit:** [`5093750`](https://github.com/prometheus/client_java/commit/50937500b4cfa35825a4441860b256df819c918c)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.43K | ± 1.39K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.06K | ± 350.48 | ops/s | 1.1x slower |
| prometheusAdd | 51.39K | ± 609.52 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.39K | ± 228.83 | ops/s | 1.4x slower |
| simpleclientInc | 6.79K | ± 19.01 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.60K | ± 128.35 | ops/s | 9.9x slower |
| simpleclientAdd | 6.25K | ± 273.16 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.45K | ± 206.16 | ops/s | 45x slower |
| openTelemetryAdd | 1.27K | ± 24.51 | ops/s | 51x slower |
| openTelemetryInc | 1.25K | ± 12.42 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.90K | ± 1.05K | ops/s | **fastest** |
| simpleclient | 4.53K | ± 50.88 | ops/s | 1.3x slower |
| prometheusNative | 2.61K | ± 77.88 | ops/s | 2.3x slower |
| openTelemetryClassic | 682.03 | ± 24.32 | ops/s | 8.7x slower |
| openTelemetryExponential | 562.33 | ± 36.14 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 492.83K | ± 1.09K | ops/s | **fastest** |
| prometheusWriteToByteArray | 491.41K | ± 2.51K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 485.09K | ± 3.42K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.19K | ± 6.19K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47388.774    ± 228.835  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1270.656     ± 24.512  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1253.403     ± 12.415  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1453.479    ± 206.161  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51394.577    ± 609.521  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65429.792   ± 1388.194  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57055.131    ± 350.485  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6247.065    ± 273.156  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6790.908     ± 19.010  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6604.552    ± 128.345  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        682.034     ± 24.316  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        562.333     ± 36.142  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5900.338   ± 1049.729  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2609.969     ± 77.877  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4532.656     ± 50.881  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483187.800   ± 6193.170  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     485089.925   ± 3421.412  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491407.170   ± 2509.797  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492830.118   ± 1093.001  ops/s
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

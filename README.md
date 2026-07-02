# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-02T04:35:39Z
- **Commit:** [`0a88856`](https://github.com/prometheus/client_java/commit/0a888563c2b1d57fccee3a2537fa6348b7003724)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.79K | ± 914.63 | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.54K | ± 1.44K | ops/s | 1.2x slower |
| prometheusAdd | 51.23K | ± 683.10 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.12K | ± 2.56K | ops/s | 1.3x slower |
| simpleclientInc | 6.58K | ± 7.74 | ops/s | 10.0x slower |
| simpleclientAdd | 6.37K | ± 217.21 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.34K | ± 17.02 | ops/s | 10x slower |
| openTelemetryAdd | 3.06K | ± 213.27 | ops/s | 21x slower |
| openTelemetryIncNoLabels | 2.94K | ± 116.11 | ops/s | 22x slower |
| openTelemetryInc | 2.92K | ± 328.30 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 7.35K | ± 1.51K | ops/s | **fastest** |
| simpleclient | 4.43K | ± 54.14 | ops/s | 1.7x slower |
| prometheusNative | 2.57K | ± 52.91 | ops/s | 2.9x slower |
| openTelemetryClassic | 755.24 | ± 27.12 | ops/s | 9.7x slower |
| openTelemetryExponential | 605.54 | ± 54.33 | ops/s | 12x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.03K | ± 558.47 | ops/s | **fastest** |
| prometheusWriteToNull | 23.08K | ± 779.89 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 503.59K | ± 3.98K | ops/s | **fastest** |
| prometheusWriteToByteArray | 502.80K | ± 2.11K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 483.93K | ± 4.86K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.03K | ± 6.54K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49124.827   ± 2560.008  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3063.294    ± 213.271  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2919.125    ± 328.303  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2936.618    ± 116.106  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51229.537    ± 683.096  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65785.812    ± 914.627  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55537.849   ± 1444.706  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6368.553    ± 217.210  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6581.185      ± 7.744  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6336.477     ± 17.024  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        755.243     ± 27.117  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        605.538     ± 54.334  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7346.859   ± 1511.183  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2568.402     ± 52.906  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4427.148     ± 54.139  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24029.973    ± 558.470  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23084.226    ± 779.891  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483025.457   ± 6544.069  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     483929.112   ± 4863.123  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     502797.669   ± 2112.128  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     503588.639   ± 3979.840  ops/s
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

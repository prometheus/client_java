# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-28T04:38:35Z
- **Commit:** [`2a2c73d`](https://github.com/prometheus/client_java/commit/2a2c73d7d23bfa291b10df85056027398e8a868d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.76K | ± 112.46 | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.65K | ± 1.91K | ops/s | 1.2x slower |
| prometheusAdd | 51.38K | ± 223.17 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.80K | ± 1.27K | ops/s | 1.3x slower |
| simpleclientInc | 6.52K | ± 11.42 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.44K | ± 129.59 | ops/s | 10x slower |
| simpleclientAdd | 6.23K | ± 200.88 | ops/s | 11x slower |
| openTelemetryAdd | 3.38K | ± 357.00 | ops/s | 19x slower |
| openTelemetryIncNoLabels | 3.17K | ± 210.47 | ops/s | 21x slower |
| openTelemetryInc | 3.14K | ± 315.07 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.59K | ± 1.30K | ops/s | **fastest** |
| simpleclient | 4.34K | ± 20.64 | ops/s | 1.3x slower |
| prometheusNative | 2.98K | ± 343.14 | ops/s | 1.9x slower |
| openTelemetryClassic | 744.80 | ± 10.06 | ops/s | 7.5x slower |
| openTelemetryExponential | 667.12 | ± 69.22 | ops/s | 8.4x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.84K | ± 202.90 | ops/s | **fastest** |
| prometheusWriteToNull | 23.03K | ± 528.33 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 500.71K | ± 2.67K | ops/s | **fastest** |
| prometheusWriteToByteArray | 496.26K | ± 2.00K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.54K | ± 2.53K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.62K | ± 2.00K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48798.506   ± 1265.447  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3379.859    ± 357.001  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3140.334    ± 315.067  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3167.654    ± 210.468  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51384.923    ± 223.171  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65757.381    ± 112.459  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55645.867   ± 1914.771  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6228.892    ± 200.877  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6517.387     ± 11.423  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6442.913    ± 129.593  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        744.801     ± 10.057  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        667.118     ± 69.223  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5593.759   ± 1300.994  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2981.163    ± 343.138  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4338.648     ± 20.642  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23841.176    ± 202.898  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23026.452    ± 528.326  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479617.575   ± 1996.318  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482541.918   ± 2528.169  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496259.795   ± 1998.889  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     500705.498   ± 2673.061  ops/s
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

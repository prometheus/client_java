# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-02T04:41:15Z
- **Commit:** [`9c3b097`](https://github.com/prometheus/client_java/commit/9c3b097f6842ffc08fb3a2ed00217c73a6c2b191)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.32K | ± 428.43 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.52K | ± 403.32 | ops/s | 1.2x slower |
| prometheusAdd | 48.92K | ± 676.84 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.65K | ± 564.47 | ops/s | 1.3x slower |
| simpleclientInc | 6.10K | ± 93.63 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 5.98K | ± 98.65 | ops/s | 9.9x slower |
| simpleclientAdd | 5.89K | ± 156.26 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.55K | ± 698.77 | ops/s | 11x slower |
| openTelemetryAdd | 4.49K | ± 819.98 | ops/s | 13x slower |
| openTelemetryInc | 3.98K | ± 280.42 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.78K | ± 1.30K | ops/s | **fastest** |
| simpleclient | 4.23K | ± 150.50 | ops/s | 1.4x slower |
| prometheusNative | 3.02K | ± 300.29 | ops/s | 1.9x slower |
| openTelemetryClassic | 721.62 | ± 17.73 | ops/s | 8.0x slower |
| openTelemetryExponential | 552.87 | ± 10.39 | ops/s | 10x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.38K | ± 351.18 | ops/s | **fastest** |
| openMetricsWriteToNull | 26.95K | ± 609.41 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 550.55K | ± 6.01K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.84K | ± 4.61K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 518.11K | ± 8.46K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 509.14K | ± 7.38K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44651.974    ± 564.469  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4488.141    ± 819.978  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3975.645    ± 280.416  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5549.448    ± 698.770  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48920.248    ± 676.842  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59320.626    ± 428.433  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51516.588    ± 403.322  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5886.191    ± 156.259  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6101.314     ± 93.629  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5983.313     ± 98.654  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        721.616     ± 17.725  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        552.867     ± 10.387  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5780.717   ± 1300.403  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3015.481    ± 300.289  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4227.330    ± 150.497  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      26952.999    ± 609.412  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27381.386    ± 351.175  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     509138.468   ± 7377.470  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     518111.969   ± 8456.858  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547840.528   ± 4608.119  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     550554.790   ± 6008.213  ops/s
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

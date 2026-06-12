# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-12T04:39:50Z
- **Commit:** [`9672749`](https://github.com/prometheus/client_java/commit/9672749085f9029ccb7328b3e88e8e78fa29e402)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.93K | ± 127.35 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.19K | ± 390.81 | ops/s | 1.2x slower |
| prometheusAdd | 48.27K | ± 675.80 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 39.60K | ± 5.09K | ops/s | 1.5x slower |
| simpleclientInc | 6.14K | ± 127.26 | ops/s | 9.8x slower |
| simpleclientAdd | 5.99K | ± 138.75 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 5.91K | ± 24.30 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.75K | ± 953.99 | ops/s | 10x slower |
| openTelemetryInc | 4.97K | ± 805.21 | ops/s | 12x slower |
| openTelemetryAdd | 3.43K | ± 187.85 | ops/s | 17x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.62K | ± 1.68K | ops/s | **fastest** |
| simpleclient | 4.34K | ± 73.80 | ops/s | 1.3x slower |
| prometheusNative | 3.03K | ± 97.64 | ops/s | 1.9x slower |
| openTelemetryClassic | 717.90 | ± 10.01 | ops/s | 7.8x slower |
| openTelemetryExponential | 579.55 | ± 20.16 | ops/s | 9.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.43K | ± 283.11 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.29K | ± 284.72 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 559.59K | ± 7.02K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.29K | ± 9.96K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 532.74K | ± 2.95K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 519.43K | ± 2.17K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      39603.028   ± 5086.991  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3434.307    ± 187.846  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4965.891    ± 805.215  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5747.783    ± 953.991  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48269.288    ± 675.803  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59931.802    ± 127.349  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51186.797    ± 390.807  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5987.546    ± 138.751  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6142.438    ± 127.262  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5913.715     ± 24.296  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        717.896     ± 10.012  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        579.552     ± 20.160  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5620.076   ± 1675.394  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3029.918     ± 97.642  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4341.062     ± 73.798  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27285.837    ± 284.725  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27430.736    ± 283.112  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     519431.185   ± 2172.069  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     532742.719   ± 2951.591  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547294.491   ± 9955.473  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     559587.669   ± 7017.006  ops/s
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

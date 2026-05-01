# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-01T04:34:27Z
- **Commit:** [`188e434`](https://github.com/prometheus/client_java/commit/188e434f25be73f75a463239b5cb4d54a8f72cca)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.34K | ± 1.51K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.27K | ± 1.20K | ops/s | 1.2x slower |
| prometheusAdd | 48.73K | ± 843.91 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 44.75K | ± 571.46 | ops/s | 1.3x slower |
| simpleclientInc | 6.16K | ± 148.06 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 5.96K | ± 231.42 | ops/s | 10x slower |
| simpleclientAdd | 5.94K | ± 180.94 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.07K | ± 807.02 | ops/s | 12x slower |
| openTelemetryInc | 4.88K | ± 765.41 | ops/s | 12x slower |
| openTelemetryAdd | 4.03K | ± 741.74 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.89K | ± 471.95 | ops/s | **fastest** |
| simpleclient | 4.22K | ± 52.49 | ops/s | 1.2x slower |
| prometheusNative | 2.85K | ± 207.94 | ops/s | 1.7x slower |
| openTelemetryClassic | 734.12 | ± 14.63 | ops/s | 6.7x slower |
| openTelemetryExponential | 546.40 | ± 28.30 | ops/s | 8.9x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 535.54K | ± 6.72K | ops/s | **fastest** |
| prometheusWriteToByteArray | 527.95K | ± 3.50K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 522.35K | ± 3.16K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 509.09K | ± 6.80K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44746.574    ± 571.457  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4030.536    ± 741.744  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4875.896    ± 765.413  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5067.698    ± 807.016  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48733.719    ± 843.913  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60341.601   ± 1508.987  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51272.928   ± 1204.629  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5938.652    ± 180.938  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6156.516    ± 148.062  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5957.156    ± 231.423  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        734.122     ± 14.630  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        546.404     ± 28.302  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4888.968    ± 471.950  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2846.103    ± 207.944  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4218.533     ± 52.494  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     509092.764   ± 6797.891  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     522347.219   ± 3157.201  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     527954.754   ± 3501.816  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     535539.226   ± 6723.387  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-05T04:24:38Z
- **Commit:** [`586eaf5`](https://github.com/prometheus/client_java/commit/586eaf5298dded8a1eb8add4490736c8a149fcd7)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.69K | ± 686.14 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.01K | ± 1.37K | ops/s | 1.2x slower |
| prometheusAdd | 51.34K | ± 392.89 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 45.74K | ± 7.88K | ops/s | 1.5x slower |
| simpleclientInc | 6.78K | ± 32.55 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.58K | ± 179.02 | ops/s | 10x slower |
| simpleclientAdd | 6.19K | ± 34.90 | ops/s | 11x slower |
| openTelemetryAdd | 1.35K | ± 49.09 | ops/s | 49x slower |
| openTelemetryInc | 1.30K | ± 67.92 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.23K | ± 96.37 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.02K | ± 6.12 | ops/s | **fastest** |
| simpleclient | 4.57K | ± 21.23 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 173.91 | ops/s | 1.6x slower |
| openTelemetryClassic | 698.91 | ± 58.20 | ops/s | 7.2x slower |
| openTelemetryExponential | 530.49 | ± 30.40 | ops/s | 9.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 529.67K | ± 3.90K | ops/s | **fastest** |
| prometheusWriteToByteArray | 528.61K | ± 7.21K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 511.11K | ± 1.22K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 505.21K | ± 4.48K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      45737.046   ± 7877.027  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1348.117     ± 49.092  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1300.114     ± 67.922  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1232.138     ± 96.369  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51344.478    ± 392.891  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66694.021    ± 686.137  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56011.999   ± 1369.857  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6186.351     ± 34.896  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6776.839     ± 32.549  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6583.748    ± 179.019  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        698.910     ± 58.196  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        530.488     ± 30.395  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5020.903      ± 6.121  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3060.227    ± 173.911  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4571.518     ± 21.229  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     505214.545   ± 4477.286  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     511105.541   ± 1219.846  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     528609.092   ± 7214.604  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     529665.284   ± 3901.519  ops/s
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

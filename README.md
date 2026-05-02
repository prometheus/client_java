# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-02T04:32:36Z
- **Commit:** [`188e434`](https://github.com/prometheus/client_java/commit/188e434f25be73f75a463239b5cb4d54a8f72cca)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.32K | ± 1.32K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.34K | ± 730.70 | ops/s | 1.2x slower |
| prometheusAdd | 48.54K | ± 20.24 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 42.57K | ± 1.46K | ops/s | 1.4x slower |
| simpleclientInc | 6.11K | ± 152.15 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 5.98K | ± 232.59 | ops/s | 10x slower |
| simpleclientAdd | 5.96K | ± 225.72 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 4.55K | ± 106.24 | ops/s | 13x slower |
| openTelemetryInc | 3.98K | ± 117.98 | ops/s | 15x slower |
| openTelemetryAdd | 3.22K | ± 206.90 | ops/s | 19x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.20K | ± 32.39 | ops/s | **fastest** |
| prometheusClassic | 3.84K | ± 72.03 | ops/s | 1.1x slower |
| prometheusNative | 3.11K | ± 165.61 | ops/s | 1.3x slower |
| openTelemetryClassic | 684.23 | ± 7.33 | ops/s | 6.1x slower |
| openTelemetryExponential | 539.33 | ± 20.93 | ops/s | 7.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 545.50K | ± 3.83K | ops/s | **fastest** |
| prometheusWriteToByteArray | 533.74K | ± 1.19K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 527.90K | ± 2.68K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 514.92K | ± 6.92K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42567.839   ± 1462.274  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3223.000    ± 206.898  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3982.460    ± 117.980  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4547.205    ± 106.239  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48538.158     ± 20.237  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60317.059   ± 1320.836  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51337.867    ± 730.695  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5958.493    ± 225.716  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6105.749    ± 152.148  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5978.515    ± 232.586  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        684.233      ± 7.331  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        539.326     ± 20.932  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3836.486     ± 72.034  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3108.202    ± 165.612  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4195.924     ± 32.389  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     514916.549   ± 6916.689  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     527898.184   ± 2682.869  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     533737.653   ± 1189.700  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     545503.497   ± 3829.578  ops/s
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

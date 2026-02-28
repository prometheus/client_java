# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-28T04:13:03Z
- **Commit:** [`6938479`](https://github.com/prometheus/client_java/commit/69384791685f0e86a28f04191434ecab310365ba)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.03K | ± 1.78K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.89K | ± 261.53 | ops/s | 1.1x slower |
| prometheusAdd | 51.60K | ± 213.98 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.25K | ± 849.48 | ops/s | 1.3x slower |
| simpleclientInc | 6.56K | ± 186.47 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.43K | ± 221.01 | ops/s | 10x slower |
| simpleclientAdd | 6.33K | ± 165.49 | ops/s | 10x slower |
| openTelemetryAdd | 1.59K | ± 234.52 | ops/s | 41x slower |
| openTelemetryIncNoLabels | 1.30K | ± 143.80 | ops/s | 50x slower |
| openTelemetryInc | 1.19K | ± 38.10 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.54K | ± 50.51 | ops/s | **fastest** |
| prometheusClassic | 4.47K | ± 628.26 | ops/s | 1.0x slower |
| prometheusNative | 2.81K | ± 270.89 | ops/s | 1.6x slower |
| openTelemetryClassic | 686.19 | ± 19.17 | ops/s | 6.6x slower |
| openTelemetryExponential | 566.90 | ± 27.31 | ops/s | 8.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 491.80K | ± 1.27K | ops/s | **fastest** |
| prometheusWriteToByteArray | 488.13K | ± 3.36K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 486.09K | ± 7.05K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 481.87K | ± 5.01K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48247.859    ± 849.478  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1587.373    ± 234.518  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1194.162     ± 38.098  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1302.714    ± 143.802  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51598.282    ± 213.976  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65033.579   ± 1780.416  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56893.342    ± 261.532  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6332.040    ± 165.489  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6556.612    ± 186.471  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6430.120    ± 221.006  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        686.186     ± 19.170  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        566.897     ± 27.313  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4473.416    ± 628.256  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2813.200    ± 270.892  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4538.190     ± 50.514  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     481874.911   ± 5011.547  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486089.779   ± 7047.657  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     488127.628   ± 3358.570  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     491799.460   ± 1267.447  ops/s
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

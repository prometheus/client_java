# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-04T04:35:56Z
- **Commit:** [`188e434`](https://github.com/prometheus/client_java/commit/188e434f25be73f75a463239b5cb4d54a8f72cca)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.43K | ± 1.76K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.90K | ± 185.96 | ops/s | 1.1x slower |
| prometheusAdd | 51.19K | ± 512.09 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.74K | ± 2.04K | ops/s | 1.3x slower |
| simpleclientInc | 6.57K | ± 203.55 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.35K | ± 225.37 | ops/s | 10x slower |
| simpleclientAdd | 6.13K | ± 231.83 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 3.40K | ± 199.51 | ops/s | 19x slower |
| openTelemetryAdd | 3.25K | ± 404.94 | ops/s | 20x slower |
| openTelemetryInc | 3.23K | ± 251.04 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.42K | ± 1.31K | ops/s | **fastest** |
| simpleclient | 4.51K | ± 27.23 | ops/s | 1.2x slower |
| prometheusNative | 3.14K | ± 81.13 | ops/s | 1.7x slower |
| openTelemetryClassic | 764.27 | ± 50.81 | ops/s | 7.1x slower |
| openTelemetryExponential | 675.47 | ± 83.26 | ops/s | 8.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 490.72K | ± 2.09K | ops/s | **fastest** |
| prometheusWriteToByteArray | 484.62K | ± 7.74K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 481.24K | ± 1.79K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 468.34K | ± 5.02K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49744.720   ± 2036.603  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3249.209    ± 404.941  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3230.014    ± 251.036  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3403.312    ± 199.510  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51190.173    ± 512.089  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65427.991   ± 1763.560  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56903.653    ± 185.959  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6130.212    ± 231.831  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6569.831    ± 203.553  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6350.060    ± 225.374  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        764.266     ± 50.810  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        675.470     ± 83.264  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5418.658   ± 1309.657  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3144.708     ± 81.126  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4510.231     ± 27.234  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     468340.689   ± 5015.343  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481236.349   ± 1785.718  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     484615.376   ± 7735.581  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     490717.143   ± 2094.100  ops/s
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

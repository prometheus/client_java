# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-23T04:28:51Z
- **Commit:** [`7cc774c`](https://github.com/prometheus/client_java/commit/7cc774c336efb38e38a1bedc834a9b03afad5259)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.15K | ± 4.00K | ops/s | **fastest** |
| prometheusNoLabelsInc | 54.86K | ± 1.26K | ops/s | 1.2x slower |
| prometheusAdd | 51.39K | ± 180.29 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 48.56K | ± 1.39K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.56K | ± 225.85 | ops/s | 9.6x slower |
| simpleclientInc | 6.55K | ± 195.63 | ops/s | 9.6x slower |
| simpleclientAdd | 6.15K | ± 56.53 | ops/s | 10x slower |
| openTelemetryAdd | 1.43K | ± 207.08 | ops/s | 44x slower |
| openTelemetryInc | 1.40K | ± 206.54 | ops/s | 45x slower |
| openTelemetryIncNoLabels | 1.33K | ± 160.27 | ops/s | 48x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.59K | ± 26.81 | ops/s | **fastest** |
| prometheusClassic | 4.20K | ± 63.11 | ops/s | 1.1x slower |
| prometheusNative | 2.94K | ± 212.13 | ops/s | 1.6x slower |
| openTelemetryClassic | 664.17 | ± 21.17 | ops/s | 6.9x slower |
| openTelemetryExponential | 551.30 | ± 49.51 | ops/s | 8.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 491.04K | ± 1.05K | ops/s | **fastest** |
| prometheusWriteToNull | 490.99K | ± 4.23K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 484.55K | ± 2.97K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 474.91K | ± 4.92K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48563.498   ± 1390.405  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1429.845    ± 207.080  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1397.865    ± 206.543  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1326.523    ± 160.270  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51385.748    ± 180.290  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63145.332   ± 3995.819  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54856.893   ± 1264.004  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6152.620     ± 56.530  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6554.093    ± 195.630  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6561.142    ± 225.847  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        664.165     ± 21.171  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        551.298     ± 49.507  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4198.058     ± 63.111  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2943.957    ± 212.131  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4590.018     ± 26.812  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     474913.996   ± 4917.690  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     484549.520   ± 2969.704  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     491041.875   ± 1050.843  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     490991.851   ± 4227.255  ops/s
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

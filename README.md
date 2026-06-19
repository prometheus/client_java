# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-19T04:40:56Z
- **Commit:** [`ce14377`](https://github.com/prometheus/client_java/commit/ce1437725c9745d247308c5db63f92469c37125d)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.62K | ± 1.67K | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.71K | ± 995.13 | ops/s | 1.2x slower |
| prometheusAdd | 51.57K | ± 131.28 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.71K | ± 1.18K | ops/s | 1.4x slower |
| simpleclientInc | 6.58K | ± 7.63 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.33K | ± 23.98 | ops/s | 10x slower |
| simpleclientAdd | 6.30K | ± 415.51 | ops/s | 10x slower |
| openTelemetryInc | 3.37K | ± 476.13 | ops/s | 19x slower |
| openTelemetryAdd | 3.22K | ± 308.57 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.01K | ± 28.77 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.02K | ± 1.03K | ops/s | **fastest** |
| simpleclient | 4.37K | ± 41.19 | ops/s | 1.1x slower |
| prometheusNative | 2.85K | ± 293.50 | ops/s | 1.8x slower |
| openTelemetryClassic | 778.01 | ± 17.81 | ops/s | 6.4x slower |
| openTelemetryExponential | 571.09 | ± 8.57 | ops/s | 8.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 24.43K | ± 666.02 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.91K | ± 460.33 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 511.43K | ± 2.62K | ops/s | **fastest** |
| prometheusWriteToByteArray | 501.17K | ± 6.40K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 489.47K | ± 3.68K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 484.24K | ± 3.38K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47708.666   ± 1178.820  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3224.479    ± 308.570  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3372.913    ± 476.125  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3012.469     ± 28.769  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51574.822    ± 131.281  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65616.037   ± 1673.513  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55711.164    ± 995.134  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6298.093    ± 415.505  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6579.224      ± 7.629  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6331.561     ± 23.979  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        778.013     ± 17.808  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        571.085      ± 8.569  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5016.242   ± 1034.746  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2852.961    ± 293.504  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4372.428     ± 41.187  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23907.454    ± 460.327  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24430.573    ± 666.025  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     484237.379   ± 3378.023  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     489472.356   ± 3682.727  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     501171.288   ± 6397.154  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     511432.936   ± 2622.360  ops/s
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

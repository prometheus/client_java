# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-05T04:33:31Z
- **Commit:** [`b5137b2`](https://github.com/prometheus/client_java/commit/b5137b283a03b11f05a6979f4480593bda44b1b4)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.97K | ± 1.14K | ops/s | **fastest** |
| prometheusNoLabelsInc | 50.12K | ± 3.22K | ops/s | 1.2x slower |
| prometheusAdd | 48.94K | ± 591.39 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.22K | ± 1.09K | ops/s | 1.4x slower |
| simpleclientInc | 6.10K | ± 82.51 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.01K | ± 221.99 | ops/s | 10.0x slower |
| simpleclientAdd | 5.85K | ± 190.31 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.27K | ± 980.10 | ops/s | 11x slower |
| openTelemetryInc | 5.20K | ± 1.18K | ops/s | 12x slower |
| openTelemetryAdd | 4.39K | ± 834.59 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.28K | ± 1.84K | ops/s | **fastest** |
| simpleclient | 4.61K | ± 59.24 | ops/s | 1.1x slower |
| prometheusNative | 2.98K | ± 280.25 | ops/s | 1.8x slower |
| openTelemetryClassic | 706.17 | ± 14.70 | ops/s | 7.5x slower |
| openTelemetryExponential | 529.14 | ± 24.73 | ops/s | 10.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 563.99K | ± 1.63K | ops/s | **fastest** |
| prometheusWriteToByteArray | 553.26K | ± 3.16K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 546.39K | ± 1.91K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 531.99K | ± 1.67K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43223.876   ± 1089.173  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4391.127    ± 834.590  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5200.062   ± 1181.311  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5268.982    ± 980.096  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48941.363    ± 591.390  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59965.992   ± 1140.486  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      50124.720   ± 3217.519  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5852.539    ± 190.305  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6100.818     ± 82.514  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6005.775    ± 221.992  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        706.165     ± 14.697  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        529.140     ± 24.725  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5284.685   ± 1842.373  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2975.066    ± 280.247  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4605.428     ± 59.242  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     531985.435   ± 1669.897  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     546387.263   ± 1914.883  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     553256.381   ± 3163.111  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     563988.834   ± 1626.021  ops/s
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

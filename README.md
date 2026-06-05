# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-05T04:39:31Z
- **Commit:** [`6e528fb`](https://github.com/prometheus/client_java/commit/6e528fb36fbbf61339521d58b4021eaa0c5baab2)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.14K | ± 751.49 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.20K | ± 132.33 | ops/s | 1.2x slower |
| prometheusAdd | 50.86K | ± 492.00 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.73K | ± 770.68 | ops/s | 1.4x slower |
| simpleclientInc | 6.56K | ± 41.81 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.38K | ± 181.46 | ops/s | 10x slower |
| simpleclientAdd | 6.10K | ± 283.70 | ops/s | 11x slower |
| openTelemetryAdd | 3.53K | ± 322.28 | ops/s | 19x slower |
| openTelemetryInc | 3.25K | ± 761.82 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.01K | ± 232.29 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.38K | ± 38.23 | ops/s | **fastest** |
| prometheusClassic | 4.38K | ± 584.58 | ops/s | 1.0x slower |
| prometheusNative | 2.79K | ± 279.97 | ops/s | 1.6x slower |
| openTelemetryClassic | 767.45 | ± 38.63 | ops/s | 5.7x slower |
| openTelemetryExponential | 723.66 | ± 13.32 | ops/s | 6.1x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.53K | ± 847.97 | ops/s | **fastest** |
| prometheusWriteToNull | 23.15K | ± 602.76 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 509.50K | ± 4.54K | ops/s | **fastest** |
| prometheusWriteToByteArray | 507.19K | ± 9.64K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 488.31K | ± 2.63K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 483.93K | ± 4.26K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48733.551    ± 770.682  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3526.466    ± 322.277  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3254.756    ± 761.821  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3008.736    ± 232.294  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50861.557    ± 492.000  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66143.223    ± 751.486  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57200.271    ± 132.327  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6096.224    ± 283.700  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6561.891     ± 41.809  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6378.389    ± 181.455  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        767.453     ± 38.629  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        723.660     ± 13.325  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4378.237    ± 584.581  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2792.252    ± 279.965  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4384.515     ± 38.235  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23528.282    ± 847.966  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23145.838    ± 602.758  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     483927.057   ± 4256.000  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     488313.308   ± 2629.022  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     507185.742   ± 9641.608  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     509500.009   ± 4538.721  ops/s
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

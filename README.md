# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-02T04:37:57Z
- **Commit:** [`922943c`](https://github.com/prometheus/client_java/commit/922943cfe12acb5e373a0a6152384673c3c7b6dc)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.75K | ± 708.74 | ops/s | **fastest** |
| prometheusNoLabelsInc | 54.95K | ± 133.37 | ops/s | 1.2x slower |
| prometheusAdd | 51.26K | ± 500.73 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.83K | ± 1.48K | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.54K | ± 61.89 | ops/s | 3.5x slower |
| openTelemetryInc | 15.28K | ± 214.04 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.52K | ± 558.26 | ops/s | 5.3x slower |
| simpleclientInc | 6.59K | ± 9.89 | ops/s | 10.0x slower |
| simpleclientNoLabelsInc | 6.33K | ± 66.84 | ops/s | 10x slower |
| simpleclientAdd | 6.22K | ± 190.81 | ops/s | 11x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.29K | ± 16.66 | ops/s | **fastest** |
| prometheusClassicSingleThread | 4.55K | ± 20.55 | ops/s | 2.7x slower |
| simpleclient | 4.40K | ± 38.03 | ops/s | 2.8x slower |
| prometheusClassic | 4.06K | ± 138.97 | ops/s | 3.0x slower |
| prometheusNative | 3.17K | ± 122.02 | ops/s | 3.9x slower |
| openTelemetryExponential | 959.37 | ± 17.67 | ops/s | 13x slower |
| openTelemetryClassic | 739.07 | ± 37.14 | ops/s | 17x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.80K | ± 572.09 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.21K | ± 598.13 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 498.54K | ± 2.90K | ops/s | **fastest** |
| prometheusWriteToByteArray | 494.07K | ± 2.28K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 488.92K | ± 3.75K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 476.17K | ± 4.54K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48830.212   ± 1481.715  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12519.329    ± 558.261  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15283.763    ± 214.041  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18541.871     ± 61.889  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51260.465    ± 500.729  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65745.072    ± 708.735  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      54946.625    ± 133.370  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6224.860    ± 190.810  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6591.117      ± 9.895  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6328.209     ± 66.843  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        739.075     ± 37.136  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        959.370     ± 17.666  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4055.690    ± 138.967  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12293.775     ± 16.661  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4548.640     ± 20.552  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3165.142    ± 122.016  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4397.621     ± 38.031  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23213.932    ± 598.128  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23796.997    ± 572.090  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     476173.542   ± 4543.231  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     488923.078   ± 3745.790  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     494072.871   ± 2277.615  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     498540.924   ± 2901.412  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

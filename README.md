# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-06T04:36:32Z
- **Commit:** [`565a583`](https://github.com/prometheus/client_java/commit/565a58396c92ddfbe1b64de37c40a0a8c165a612)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.13K | ± 1.15K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.45K | ± 204.56 | ops/s | 1.2x slower |
| prometheusAdd | 50.78K | ± 608.35 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.13K | ± 1.63K | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.46K | ± 181.85 | ops/s | 3.5x slower |
| openTelemetryInc | 15.19K | ± 31.38 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.18K | ± 1.17K | ops/s | 5.3x slower |
| simpleclientInc | 6.57K | ± 80.27 | ops/s | 9.9x slower |
| simpleclientAdd | 6.49K | ± 60.93 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.33K | ± 12.84 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.29K | ± 23.22 | ops/s | **fastest** |
| prometheusClassic | 6.31K | ± 663.81 | ops/s | 1.9x slower |
| prometheusClassicSingleThread | 4.56K | ± 47.74 | ops/s | 2.7x slower |
| simpleclient | 4.43K | ± 14.82 | ops/s | 2.8x slower |
| prometheusNative | 2.96K | ± 307.46 | ops/s | 4.2x slower |
| openTelemetryExponential | 888.54 | ± 189.37 | ops/s | 14x slower |
| openTelemetryClassic | 811.58 | ± 48.63 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.93K | ± 516.36 | ops/s | **fastest** |
| prometheusWriteToNull | 23.89K | ± 353.68 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 503.06K | ± 3.88K | ops/s | **fastest** |
| prometheusWriteToByteArray | 486.54K | ± 9.40K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 482.20K | ± 2.34K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 474.28K | ± 5.04K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49127.888   ± 1630.055  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12184.913   ± 1168.898  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      15188.177     ± 31.375  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18460.734    ± 181.851  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50782.479    ± 608.347  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65129.358   ± 1147.830  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56452.054    ± 204.555  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6485.071     ± 60.926  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6565.080     ± 80.275  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6332.210     ± 12.838  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        811.582     ± 48.626  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        888.537    ± 189.371  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6307.466    ± 663.808  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12288.598     ± 23.223  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4557.555     ± 47.736  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2961.066    ± 307.456  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4425.701     ± 14.820  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23930.911    ± 516.358  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23894.443    ± 353.676  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     474281.236   ± 5041.899  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482199.945   ± 2338.935  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     486536.936   ± 9396.331  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     503055.378   ± 3884.819  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-21T04:37:40Z
- **Commit:** [`0a91771`](https://github.com/prometheus/client_java/commit/0a917717bbd9ec2112f3e85b4d8d03777a39b511)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.15K | ± 1.40K | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.44K | ± 2.51K | ops/s | 1.2x slower |
| prometheusAdd | 51.52K | ± 126.82 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.47K | ± 1.29K | ops/s | 1.3x slower |
| simpleclientInc | 6.51K | ± 55.73 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.37K | ± 29.83 | ops/s | 10x slower |
| simpleclientAdd | 6.34K | ± 196.65 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.46K | ± 178.03 | ops/s | 19x slower |
| openTelemetryAdd | 3.12K | ± 368.46 | ops/s | 21x slower |
| openTelemetryInc | 3.04K | ± 235.05 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.59K | ± 136.49 | ops/s | **fastest** |
| prometheusClassic | 5.69K | ± 1.23K | ops/s | 2.2x slower |
| prometheusClassicSingleThread | 4.59K | ± 23.02 | ops/s | 2.7x slower |
| simpleclient | 4.43K | ± 55.43 | ops/s | 2.8x slower |
| prometheusNative | 2.83K | ± 268.57 | ops/s | 4.5x slower |
| openTelemetryClassic | 770.81 | ± 13.42 | ops/s | 16x slower |
| openTelemetryExponential | 606.64 | ± 11.71 | ops/s | 21x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 24.21K | ± 961.00 | ops/s | **fastest** |
| openMetricsWriteToNull | 24.05K | ± 1.26K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 509.33K | ± 2.93K | ops/s | **fastest** |
| prometheusWriteToByteArray | 503.11K | ± 6.49K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 486.53K | ± 6.31K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.15K | ± 6.08K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48470.890   ± 1294.869  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3119.278    ± 368.457  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3037.221    ± 235.047  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3464.901    ± 178.028  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51523.638    ± 126.823  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65154.835   ± 1398.043  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55435.166   ± 2510.537  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6336.782    ± 196.645  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6506.379     ± 55.731  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6369.902     ± 29.833  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        770.806     ± 13.421  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        606.639     ± 11.713  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5689.421   ± 1229.805  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12588.469    ± 136.487  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4588.670     ± 23.020  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2825.288    ± 268.565  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4429.409     ± 55.430  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24045.958   ± 1259.164  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24208.042    ± 961.001  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475153.304   ± 6080.972  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486532.182   ± 6308.921  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     503105.762   ± 6487.317  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     509327.972   ± 2925.696  ops/s
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

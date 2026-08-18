# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-18T03:55:16Z
- **Commit:** [`3ad84d4`](https://github.com/prometheus/client_java/commit/3ad84d4a9b517364790ee00dbd505e3e4397fc77)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 64.81K | ± 1.01K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.87K | ± 365.62 | ops/s | 1.1x slower |
| prometheusAdd | 50.91K | ± 549.94 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.37K | ± 865.29 | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.39K | ± 213.58 | ops/s | 3.5x slower |
| openTelemetryInc | 14.95K | ± 214.20 | ops/s | 4.3x slower |
| openTelemetryAdd | 12.79K | ± 187.24 | ops/s | 5.1x slower |
| simpleclientInc | 6.61K | ± 49.26 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.36K | ± 39.46 | ops/s | 10x slower |
| simpleclientAdd | 6.30K | ± 233.69 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.31K | ± 23.61 | ops/s | **fastest** |
| prometheusClassic | 4.74K | ± 652.49 | ops/s | 2.6x slower |
| prometheusClassicSingleThread | 4.54K | ± 30.18 | ops/s | 2.7x slower |
| simpleclient | 4.42K | ± 70.06 | ops/s | 2.8x slower |
| prometheusNative | 2.69K | ± 47.95 | ops/s | 4.6x slower |
| openTelemetryExponential | 938.80 | ± 60.30 | ops/s | 13x slower |
| openTelemetryClassic | 826.41 | ± 55.04 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 24.08K | ± 850.88 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.63K | ± 1.20K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 493.52K | ± 5.34K | ops/s | **fastest** |
| prometheusWriteToByteArray | 488.22K | ± 1.81K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 468.57K | ± 5.38K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 463.92K | ± 7.28K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48369.878    ± 865.289  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12792.179    ± 187.238  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14954.623    ± 214.197  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18388.034    ± 213.584  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50907.740    ± 549.942  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64806.901   ± 1009.229  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56872.420    ± 365.618  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6298.685    ± 233.687  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6609.137     ± 49.260  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6362.423     ± 39.456  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        826.407     ± 55.044  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        938.796     ± 60.300  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4741.909    ± 652.487  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12310.332     ± 23.607  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4539.020     ± 30.178  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2691.802     ± 47.954  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4421.527     ± 70.057  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23631.987   ± 1197.452  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24077.500    ± 850.878  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     463923.499   ± 7281.395  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     468572.579   ± 5380.568  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     488223.590   ± 1805.765  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     493519.946   ± 5336.115  ops/s
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

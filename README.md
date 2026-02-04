# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-04T04:23:03Z
- **Commit:** [`ba308cf`](https://github.com/prometheus/client_java/commit/ba308cf393c89af8b2cd6773570ec7ba72acc42a)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.66K | ± 528.59 | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.45K | ± 871.63 | ops/s | 1.2x slower |
| prometheusAdd | 51.64K | ± 216.32 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.78K | ± 2.06K | ops/s | 1.4x slower |
| simpleclientInc | 6.79K | ± 24.15 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.45K | ± 184.97 | ops/s | 10x slower |
| simpleclientAdd | 6.07K | ± 147.12 | ops/s | 11x slower |
| openTelemetryAdd | 1.32K | ± 50.02 | ops/s | 50x slower |
| openTelemetryInc | 1.31K | ± 172.71 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.23K | ± 30.55 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.11K | ± 121.08 | ops/s | **fastest** |
| simpleclient | 4.49K | ± 51.46 | ops/s | 1.1x slower |
| prometheusNative | 3.07K | ± 191.88 | ops/s | 1.7x slower |
| openTelemetryClassic | 665.79 | ± 29.79 | ops/s | 7.7x slower |
| openTelemetryExponential | 551.39 | ± 31.03 | ops/s | 9.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 547.30K | ± 9.92K | ops/s | **fastest** |
| prometheusWriteToByteArray | 518.51K | ± 8.79K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 490.02K | ± 7.29K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 478.57K | ± 6.88K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47778.697   ± 2061.616  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1324.925     ± 50.023  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1306.398    ± 172.706  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1232.574     ± 30.555  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51636.538    ± 216.320  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66659.858    ± 528.586  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55449.633    ± 871.625  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6071.593    ± 147.123  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6790.302     ± 24.148  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6447.433    ± 184.974  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        665.794     ± 29.795  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        551.391     ± 31.033  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5112.176    ± 121.083  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3066.824    ± 191.880  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4492.527     ± 51.459  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478567.214   ± 6877.946  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490022.612   ± 7286.405  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     518509.582   ± 8790.704  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     547304.381   ± 9918.583  ops/s
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

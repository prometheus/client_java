# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-07T04:30:42Z
- **Commit:** [`0fa1ad7`](https://github.com/prometheus/client_java/commit/0fa1ad7dcb71f7f02e19ee9604c07d9c48802f04)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1008-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.87K | ± 1.68K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.18K | ± 133.77 | ops/s | 1.2x slower |
| prometheusAdd | 51.55K | ± 276.70 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.06K | ± 2.31K | ops/s | 1.3x slower |
| simpleclientInc | 6.71K | ± 12.05 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.49K | ± 189.07 | ops/s | 10x slower |
| simpleclientAdd | 6.07K | ± 261.23 | ops/s | 11x slower |
| openTelemetryInc | 1.47K | ± 199.25 | ops/s | 45x slower |
| openTelemetryAdd | 1.42K | ± 117.99 | ops/s | 46x slower |
| openTelemetryIncNoLabels | 1.38K | ± 254.41 | ops/s | 48x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.91K | ± 1.34K | ops/s | **fastest** |
| simpleclient | 4.45K | ± 59.08 | ops/s | 1.3x slower |
| prometheusNative | 2.83K | ± 128.72 | ops/s | 2.1x slower |
| openTelemetryClassic | 667.93 | ± 11.25 | ops/s | 8.9x slower |
| openTelemetryExponential | 542.15 | ± 11.23 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 487.53K | ± 2.09K | ops/s | **fastest** |
| prometheusWriteToByteArray | 481.15K | ± 2.46K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 478.94K | ± 4.05K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.46K | ± 5.33K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49064.044   ± 2311.052  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1424.342    ± 117.995  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1466.115    ± 199.254  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1376.275    ± 254.413  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51547.856    ± 276.699  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65865.435   ± 1680.160  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57178.430    ± 133.766  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6072.631    ± 261.231  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6712.722     ± 12.053  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6493.527    ± 189.072  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        667.932     ± 11.254  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        542.150     ± 11.227  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5912.958   ± 1344.714  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2834.389    ± 128.723  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4447.808     ± 59.083  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475459.313   ± 5329.041  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     478941.266   ± 4053.787  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     481149.579   ± 2461.991  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     487532.652   ± 2091.746  ops/s
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

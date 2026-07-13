# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-13T04:31:56Z
- **Commit:** [`63967bd`](https://github.com/prometheus/client_java/commit/63967bd36ebc638234742ec58ad28f6098a92b3a)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 64.95K | ± 1.44K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.35K | ± 1.04K | ops/s | 1.2x slower |
| prometheusAdd | 51.49K | ± 164.92 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.37K | ± 242.03 | ops/s | 1.4x slower |
| simpleclientInc | 6.63K | ± 61.34 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.42K | ± 155.89 | ops/s | 10x slower |
| simpleclientAdd | 6.22K | ± 207.57 | ops/s | 10x slower |
| openTelemetryAdd | 3.54K | ± 291.22 | ops/s | 18x slower |
| openTelemetryIncNoLabels | 3.42K | ± 192.90 | ops/s | 19x slower |
| openTelemetryInc | 3.22K | ± 411.83 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassic | 5.82K | ± 2.75K | ops/s | **fastest** |
| simpleclient | 4.45K | ± 24.48 | ops/s | 1.3x slower |
| prometheusNative | 2.81K | ± 345.95 | ops/s | 2.1x slower |
| openTelemetryClassic | 767.10 | ± 30.88 | ops/s | 7.6x slower |
| openTelemetryExponential | 547.95 | ± 18.27 | ops/s | 11x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 23.77K | ± 359.36 | ops/s | **fastest** |
| prometheusWriteToNull | 23.25K | ± 945.22 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 498.53K | ± 4.60K | ops/s | **fastest** |
| prometheusWriteToByteArray | 496.06K | ± 7.19K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 484.63K | ± 5.69K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 470.91K | ± 14.23K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47373.815    ± 242.034  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3535.854    ± 291.217  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3218.342    ± 411.826  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3417.129    ± 192.901  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51489.320    ± 164.917  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64946.319   ± 1442.699  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56350.364   ± 1037.559  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6218.578    ± 207.566  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6629.245     ± 61.340  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6421.349    ± 155.893  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        767.101     ± 30.875  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        547.949     ± 18.273  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5815.543   ± 2747.206  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2814.131    ± 345.953  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4445.478     ± 24.476  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23767.079    ± 359.364  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23252.590    ± 945.225  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     470908.978  ± 14229.149  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     484626.283   ± 5685.393  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496060.648   ± 7192.046  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     498527.311   ± 4599.410  ops/s
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

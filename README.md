# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-12T04:30:35Z
- **Commit:** [`63967bd`](https://github.com/prometheus/client_java/commit/63967bd36ebc638234742ec58ad28f6098a92b3a)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** INTEL(R) XEON(R) PLATINUM 8573C, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| codahaleIncNoLabels | 30.72K | ± 778.14 | ops/s | **fastest** |
| prometheusNoLabelsInc | 28.04K | ± 832.90 | ops/s | 1.1x slower |
| prometheusInc | 27.42K | ± 412.42 | ops/s | 1.1x slower |
| prometheusAdd | 26.61K | ± 377.46 | ops/s | 1.2x slower |
| simpleclientInc | 7.06K | ± 204.41 | ops/s | 4.4x slower |
| simpleclientNoLabelsInc | 6.99K | ± 121.93 | ops/s | 4.4x slower |
| simpleclientAdd | 6.72K | ± 129.09 | ops/s | 4.6x slower |
| openTelemetryInc | 2.55K | ± 281.23 | ops/s | 12x slower |
| openTelemetryIncNoLabels | 2.40K | ± 158.53 | ops/s | 13x slower |
| openTelemetryAdd | 2.35K | ± 197.94 | ops/s | 13x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| simpleclient | 4.57K | ± 109.82 | ops/s | **fastest** |
| prometheusClassic | 2.88K | ± 335.11 | ops/s | 1.6x slower |
| prometheusNative | 1.90K | ± 136.25 | ops/s | 2.4x slower |
| openTelemetryClassic | 447.32 | ± 3.82 | ops/s | 10x slower |
| openTelemetryExponential | 343.45 | ± 17.62 | ops/s | 13x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 18.32K | ± 180.70 | ops/s | **fastest** |
| prometheusWriteToNull | 17.98K | ± 202.23 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 299.65K | ± 2.53K | ops/s | **fastest** |
| prometheusWriteToByteArray | 293.71K | ± 2.18K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 275.79K | ± 1.94K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 274.92K | ± 1.95K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      30724.097    ± 778.143  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2353.813    ± 197.940  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2552.485    ± 281.227  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2396.040    ± 158.534  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      26610.762    ± 377.462  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      27419.283    ± 412.419  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      28038.153    ± 832.904  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6717.084    ± 129.094  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7057.843    ± 204.412  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6993.959    ± 121.926  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        447.325      ± 3.822  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        343.452     ± 17.622  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2882.648    ± 335.110  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       1902.448    ± 136.254  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4569.887    ± 109.819  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18316.664    ± 180.698  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      17983.662    ± 202.225  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     274923.188   ± 1954.708  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     275793.243   ± 1939.567  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     293709.510   ± 2180.339  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     299647.650   ± 2533.400  ops/s
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

# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-03T04:25:37Z
- **Commit:** [`1f3865f`](https://github.com/prometheus/client_java/commit/1f3865fd7a03f8e835795117f835ab99f675f67e)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.94K | ± 1.54K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.41K | ± 72.41 | ops/s | 1.1x slower |
| prometheusAdd | 51.41K | ± 229.98 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.75K | ± 1.32K | ops/s | 1.4x slower |
| simpleclientInc | 6.68K | ± 118.94 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.60K | ± 168.16 | ops/s | 10.0x slower |
| simpleclientAdd | 6.00K | ± 118.45 | ops/s | 11x slower |
| openTelemetryAdd | 1.59K | ± 234.44 | ops/s | 41x slower |
| openTelemetryIncNoLabels | 1.28K | ± 150.51 | ops/s | 52x slower |
| openTelemetryInc | 1.24K | ± 12.10 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.29K | ± 77.29 | ops/s | **fastest** |
| simpleclient | 4.54K | ± 33.68 | ops/s | 1.2x slower |
| prometheusNative | 3.02K | ± 180.91 | ops/s | 1.8x slower |
| openTelemetryClassic | 658.37 | ± 34.51 | ops/s | 8.0x slower |
| openTelemetryExponential | 514.61 | ± 9.53 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 548.36K | ± 12.10K | ops/s | **fastest** |
| prometheusWriteToByteArray | 530.59K | ± 6.83K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 527.96K | ± 4.63K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 518.78K | ± 10.94K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48746.441   ± 1322.699  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1589.488    ± 234.437  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1241.429     ± 12.098  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1277.039    ± 150.507  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51413.114    ± 229.984  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65943.077   ± 1539.044  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57411.083     ± 72.411  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6001.549    ± 118.454  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6680.777    ± 118.938  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6600.977    ± 168.159  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        658.374     ± 34.506  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        514.609      ± 9.528  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5291.360     ± 77.292  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3023.390    ± 180.907  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4542.099     ± 33.682  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     518779.578  ± 10942.142  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     527962.682   ± 4630.616  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     530587.492   ± 6828.447  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     548360.378  ± 12101.219  ops/s
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

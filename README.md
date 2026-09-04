# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-04T03:52:29Z
- **Commit:** [`058e544`](https://github.com/prometheus/client_java/commit/058e54406ef2edfbe1885b414c8cd2999279cf47)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusInc | 58.65K | ± 2.17K | ops/s |
| prometheusNoLabelsInc | 51.45K | ± 449.48 | ops/s |
| prometheusAdd | 47.95K | ± 247.51 | ops/s |
| codahaleIncNoLabels | 38.19K | ± 9.95K | ops/s |
| openTelemetryIncNoLabels | 16.63K | ± 921.01 | ops/s |
| openTelemetryInc | 14.34K | ± 323.13 | ops/s |
| openTelemetryAdd | 12.13K | ± 165.05 | ops/s |
| simpleclientInc | 6.13K | ± 62.35 | ops/s |
| simpleclientNoLabelsInc | 6.01K | ± 178.51 | ops/s |
| simpleclientAdd | 5.80K | ± 320.62 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 13.65K | ± 115.56 | ops/s |
| prometheusClassicSingleThread | 5.81K | ± 17.28 | ops/s |
| simpleclient | 4.53K | ± 37.89 | ops/s |
| prometheusClassic | 4.45K | ± 624.70 | ops/s |
| prometheusNative | 3.01K | ± 218.95 | ops/s |
| openTelemetryClassic | 739.17 | ± 17.06 | ops/s |
| openTelemetryExponential | 671.30 | ± 26.15 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 27.48K | ± 200.72 | ops/s |
| prometheusWriteToNull | 27.33K | ± 393.14 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 585.10K | ± 2.86K | ops/s |
| prometheusWriteToByteArray | 571.24K | ± 4.67K | ops/s |
| openMetricsWriteToNull | 551.54K | ± 6.14K | ops/s |
| openMetricsWriteToByteArray | 533.86K | ± 5.44K | ops/s |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      38189.571   ± 9952.090  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12126.648    ± 165.055  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14342.547    ± 323.134  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      16633.269    ± 921.013  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47945.663    ± 247.505  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58650.378   ± 2170.986  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51451.477    ± 449.481  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5804.098    ± 320.615  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6127.175     ± 62.347  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6005.780    ± 178.506  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        739.173     ± 17.057  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        671.301     ± 26.145  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4448.694    ± 624.702  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      13649.940    ± 115.558  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       5812.634     ± 17.278  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3006.396    ± 218.949  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4531.532     ± 37.891  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27484.379    ± 200.717  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27325.814    ± 393.142  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     533863.623   ± 5444.949  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     551543.911   ± 6140.325  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     571238.202   ± 4670.174  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     585100.709   ± 2864.052  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |

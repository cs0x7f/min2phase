# Benchmark

- Memory: ~1M with twist-flip-pruning (TFP) table, ~0.7M without TFP table.

## Solving time for diffecent target length

    |   Init   | Unlimited |  21 moves |  20 moves | Init Time |
    |:--------:|:---------:|:---------:|:---------:|:---------:|
    |   Full   |**.685 ms**|**.805 ms**|**4.62 ms**|**195. ms**|
    |  Partial |  4.12 ms  |  4.52 ms  |  19.0 ms  |  60.0 ms  |

## Solving time and average solution length for different probes

    | probeMin | Avg Length |   Time   |
    |:--------:|:----------:|:--------:|
    |      5   |    20.63   |  .827 ms |
    |     10   |    20.48   |  1.07 ms |
    |     20   |    20.27   |  1.55 ms |
    |     50   |    19.95   |  2.83 ms |
    |    100   |    19.68   |  4.87 ms |
    |    200   |    19.48   |  8.58 ms |
    |    500   |    19.29   |  19.4 ms |
    |   1000   |    19.11   |  36.3 ms |
    |   2000   |    18.94   |    -  ms |
    |   5000   |    18.75   |    -  ms |
    |  10000   |    18.63   |    -  ms |

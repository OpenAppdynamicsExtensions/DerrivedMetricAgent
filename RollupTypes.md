##Aggregation

The aggregator qualifier specifies how the values reported during a one-minute period are aggregated.

| Aggregator Type | Description |
| --------------- | ------------|
| SUM | Sum of all reported values in the minute. This operation causes the metric to behave like a counter. |
| OBSERVATION  | Last reported value in the minute. If no value is reported in that minute, the value from the last time it was reported is used. |
| AVERAGE | Average of all reported values in the minute. The default operation. |


##Time Roll Up

The time-rollup qualifier specifies how the Controller rolls up the values when it converts from one-minute granularity tables to 10-minute granularity and 60-minute granularity tables over time.

| Roll up Strategy | Description |
| --- | --- |
| AVERAGE | Average of all one-minute data points when adding it to the 10-minute or 60-minute granularity table. |
| SUM | Sum of all one-minute data points when adding it to the 10-minute or 60-minute granularity table. |
| CURRENT | Last reported one-minute data point in that 10-minute or 60-minute interval. |


##Cluster Roll Up

The cluster-rollup qualifier specifies how the controller aggregates metric values in a tier.
| Roll up Strategy | Description |
|--- | --- |
| INDIVIDUAL | Aggregates the metric value by averaging the metric values across each node in the tier. |
| COLLECTIVE | Aggregates the metric value by adding up the metric values for all the nodes in the tier. |


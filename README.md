# DerrivedMetricAgent Quick Starter Guide

This agent allows to perform aggregation functions using a custom Groovy DSL on metric data returned by the AppDynamics REST Query interface (https://github.com/Appdynamics/RestMetricQueries). After performing the aggregations the resulting values will be reported back into an AppDynamics controller as custom metrics.

## Installation

The following steps describe the process of installing the DerviceMetric agent

```
- Download the file 'DerrivedMetricsAgent.zip' from this GitHub page
- Install a machine agent on a host that can communicate with the AppDynamics Controller or install on the Controller host itself.
- Unzip the zip file 'DerrivedMetricsAgent.zip' into <machine_agent>/monitors/.
- Navigate into the calculations directory <machine_agent>/monitors/DerrivedMetricAgent/calculations and add at least one calculation  file (see Configuration for more details)
- Start the machine agent and verify by using the Metric Browser in the Controller UI that the custom metrics are getting ingested properly.
```

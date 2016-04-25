# DerrivedMetricAgent Quick Starter Guide

This agent allows to perform aggregation functions using a custom Groovy DSL on metric data returned by the AppDynamics REST Query interface (https://github.com/Appdynamics/RestMetricQueries). After performing the aggregations the resulting values will be reported back into an AppDynamics controller as custom metrics.

## Installation

The following steps describe the process of installing the DerivedMetric agent

1. Download the file 'DerrivedMetricsAgent.zip' from this GitHub page
2. Install a machine agent on a host that can communicate with the AppDynamics Controller or install on the Controller host itself.
3. Unzip the file 'DerrivedMetricsAgent.zip' into `<machine_agent>/monitors/`.
4. Navigate into the calculations directory `<machine_agent>/monitors/DerrivedMetricAgent/calculations` and add at least one calculation  file (see Configuration for more details)
5. Start the machine agent and verify by using the Metric Browser in the Controller UI that the custom metrics are getting ingested properly.

## Configuration

Connection details as well as metrics to be collected, the aggregation functions and the re-ingestion as custom metrics is all defined using a custom Groovy DSL. One or more files containing respective instructions can be added into the calculations sub directory of the DerrivedMetricAgent. The agent will consider all files with a suffix of `".metric"`.


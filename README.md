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

Connection details as well as metrics to be collected, the aggregation functions and the re-ingestion as custom metrics is all defined using a custom Groovy DSL. One or more files containing respective instructions can be added into the calculations sub directory of the DerrivedMetricAgent. The agent will consider all files with a suffix of `.metric`.

There are typically three blocks in a `.metric` file as explained below

### Connection details

Connection details are configured using the connect instruction

```
connect {
     controller "controller.mycompany.com:8080"
     account "customer1"
     user "user"
     password "password"
 }
```

Details about which metric data should be fetched (using the REST query interface) are configured using the calculate instruction

```
calculate (""" export  'Calls per Minute'  as calls ,'Errors per Minute' 
                        from 'Overall Application Performance'
                        on Application 'ECommerce'
                        for 30 minutes 5 hours ago
           """, {
           
            })
```

Please note from the example above that the REST query supports metric aliases which can be used for the metric aggregation functions. It is also possible to specify a list of metrics to be fetched delimited by comma. Special attention should be given to the way how the timerange can be specified for which to get the data. In the above example metric values spanning 30 minutes will be fetched from the current point in time 5 hours ago. It should be worth mentioning that the above query will actually return aggregated values already where 1 minute data is aggregated to 10 minute data.

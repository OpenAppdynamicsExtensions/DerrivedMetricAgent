# DerrivedMetricAgent Quick Starter Guide

This agent allows to perform aggregation functions using a custom Groovy DSL on metric data returned by the AppDynamics REST Query interface (https://github.com/Appdynamics/RestMetricQueries). After performing the aggregations the resulting values will be reported back into an AppDynamics controller as custom metrics.

## Installation

The following steps describe the process of installing the DerivedMetric agent

1. Download the file 'DerrivedMetricsAgent.zip' from this GitHub page
2. Install a machine agent on a host that can communicate with the AppDynamics Controller or install on the Controller host itself.
3. Unzip the file 'DerrivedMetricsAgent.zip' into `<machine_agent>/monitors/`.
4. Navigate into the calculations directory `<machine_agent>/monitors/DerrivedMetricAgent/calculations/scripts` and add at least one calculation  file (see Configuration for more details)
5. Start the machine agent and verify by using the Metric Browser in the Controller UI that the custom metrics are getting ingested properly.

## Configuration

Connection details as well as metrics to be collected, the aggregation functions and the re-ingestion as custom metrics is all defined using a custom Groovy DSL. One or more files containing respective instructions can be added into the calculations sub directory of the DerrivedMetricAgent. The agent will consider all files with a suffix of `.metric`.

There are typically four blocks in a `.metric` file as explained below

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

### Metric data query

Details about which metric data should be fetched (using the REST query interface) are configured using the calculate instruction

```
calculate (""" export  'Calls per Minute'  as calls ,'Errors per Minute' 
                        from 'Overall Application Performance'
                        on Application 'ECommerce'
                        for 30 minutes 5 hours ago
           """, {
               // this is where the aggregation functions will be specified
               // this is also where the re-ingestion will be specified
            })
```

Please note from the example above that the REST query supports metric aliases which can be used for the metric aggregation functions. It is also possible to specify a list of metrics to be fetched (seperated by comma). Special attention should be given to the way how the timerange can be specified for which to get the data. In the example above metric values spanning 30 minutes will be fetched from the current point in time 5 hours ago. It should be worth mentioning that the above query will actually return aggregated values  where 1 minute data is already aggregated to 10 minute data.

### Aggregation functions

Within the `calculate` block one or more aggregation functions can be specified. Please see the groovy docs (http://leika.github.io/DerrivedMetricAgent/groovydoc/com/appdynamics/ace/agents/derrivedMetrics/CalculationDelegate.html) for a complete list of available functions. Below are two examples of aggregation functions: one which will take all values and calculate an average value from them and a second example showing how to calculate a delta value which is the result of the subtraction of the second last value of a metric from the last value of a metric in a given timerange.

```
avg('calls');
delta('calls');
```

### Re-ingesting metric values as custom metrics

Finally the result of the aggregation functions can be re-ingested into the Controller as a new (custom) metric. This would be done through an instruction also in the `calculate` block as shown below:

```
reportMetric("errorRate",(avg('calls')/avg('Errors per Minute'))*100 )
```

### Example of complete metric file

Below is an example of a metric file where all the different blocks have been specified in sequence:

```
connect {
     controller "controller.mycompany.com:8080"
     account "customer1"
     user "user"
     password "password"
 }

calculate (""" export  'Calls per Minute'  as calls ,'Errors per Minute' ,'Average Response Time (ms)'
                        from 'Overall Application Performance'
                        on Application 'ECommerce'
                        for 30 minutes 5 hours ago
           """, {
           reportMetric("errorRate",(avg('calls')/avg('Errors per Minute'))*100 )
           })
```

### Example of a more complex example

Below is a more complex example where the average response time for a number of BTs is averaged again

```
double sumOfResponseTimes = 0;
double count = 0;

calculate (""" export 'Average Response Time (ms)' as responseTime
                        from 'Business Transaction Performance|Business Transactions'. * as Tier. * as BT
                        on Application 'ECommerce'
                        for 15 minutes
           """, {
                println "Individual response time: " + responseTime
                sumOfResponseTimes += avg('responseTime')
                count++
           })
println "Avg. Response Time over all BTs: "+ sumOfResponseTimes / count
```
package com.appdynamics.ace.agents.derivedMetrics.groovy

import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.CalculationException
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.MetricValueContainer
import org.apache.log4j.Logger

/**
 * Created by stefan.marx on 12.02.17.
 */
class CalculationDelegateBase extends Script{
    Logger _logger;

    public Map<String,List<MetricValueContainer>> _metricValues = [:];

    Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(this.getClass().name)
        }
        return _logger;
    }

    @Override
    Object run() {
        return super.run();
    }

    public static final String AVERAGE = "AVERAGE";
    public static final String SUM = "SUM";
    public static final String OBSERVATION = "OBSERVATION";
    public static final String CURRENT = "CURRENT";
    public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String COLLECTIVE = "COLLECTIVE";

    /** Report a Metric to Agent or CLI,
     *
     * @param path
     * @param value
     * @param aggregation multi execution (node to tier aggregation) [ AVERAGE, SUM, OBSERVATION ]
     * @param timeRollup Time Rollout (1min -> 10 min -> 60 min) [AVERAGE, SUM, CURRENT  ]
     * @param cluster  Cluster Rollup  (node to tier aggregation) [INDIVIDUAL, COLLECTIVE]
     */
    def reportMetric(String path,def value,
                     String aggregation = AVERAGE,
                     String timeRollup = AVERAGE,
                     String cluster = INDIVIDUAL) {
        logger.info("report metric $path : $value");
        def metricValue = new MetricValueContainer ( path, (long) value,
                aggregation,timeRollup,cluster) ;

        def values = _metricValues[path] ?: []
        values += metricValue;
        _metricValues [path] = values;
    }


    def methodMissing(String name, args) {
        def argList = args.collect { return it}
        getLogger().error("Missing Method : $name ( ${argList.join(',')} ) ");
        throw new CalculationException("Missing Method : $name  ")
    }


}

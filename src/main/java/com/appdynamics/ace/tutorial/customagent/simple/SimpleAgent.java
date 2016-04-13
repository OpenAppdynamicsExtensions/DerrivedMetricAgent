package com.appdynamics.ace.tutorial.customagent.simple;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Random;

/**
 * Createdkjj with IntelliJ IDEA.
 * User: stefan.marx
 * Date: 09.10.13
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class SimpleAgent extends AManagedMonitor {

    private static Logger logger = Logger.getLogger(SimpleAgent.class);
    private final Random rnd;
    int run = 0;




    private final static String metricPrefix = "Custom Metrics|MySimpleAgent|MyShelter|";


    public SimpleAgent() {
        logger.log(Level.INFO," LOADED !!!!");
         rnd = new Random();

    }

    public TaskOutput execute(Map<String, String> stringStringMap, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {

        System.out.println("RUN:"+(run++));

        MetricWriter mw = getMetricWriter(metricPrefix + "Base AVG Metric",
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);

        mw.printMetric("765215");


        mw = getMetricWriter(metricPrefix + "Base Sum Metric",
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);


        Random r = new Random(System.currentTimeMillis());
        long l = r.nextInt(1000);

        mw.printMetric(String.valueOf( (int) Math.sqrt(l)) );



        mw = getMetricWriter(metricPrefix + "Base Current Metric",
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);

        if (run%3 == 0) {
            System.out.println("MONITORED");
            mw.printMetric("5245");
        }


        mw = getMetricWriter(metricPrefix + "Base random2 Metric",
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);

        if (run > 1000) run = 1;
        if (run%10 > 0) {
             int r2 = rnd.nextInt(10);

            int val = r2+(10*run);
            System.out.println("RANDOM !!! "+val + " ("+r2+")");
            mw.printMetric(""+val);
        } else System.out.println("RANDOM Skipped !!!");


        System.out.println(" ------------->>>>>> MONITORED <<<<<<<<<-------------------");
        return new TaskOutput("Metric Upload Complete");

    }


}


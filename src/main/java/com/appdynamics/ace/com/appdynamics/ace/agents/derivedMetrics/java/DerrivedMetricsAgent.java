package com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java;


import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.util.KeyStoreWrapper;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by stefan.marx on 13.04.16.
 */
public class DerrivedMetricsAgent extends AManagedMonitor {

    private static Logger logger = Logger.getLogger(DerrivedMetricsAgent.class);
    private final Random rnd;
    private final Binding _binder;
    private final CompilerConfiguration _compilerConfig;
    private final GroovyShell _shell;
    private CalculationEngine _calcEngine;
    private String _metricPrefix;
    private boolean _bfMetricCached = true;
    private HashMap<String, HashMap<String,MetricValueContainer>> _metricCache = new HashMap<>();
    private boolean _bfInternalMonitoring = false;


    public DerrivedMetricsAgent (){
        logger.log(Level.INFO," LOADED !!!! "+new File(".").getAbsolutePath());
        rnd = new Random();

        _compilerConfig = new CompilerConfiguration();
      //  _compilerConfig.setScriptBaseClass(DSLDelegate.class.getName());

        _binder = new Binding();
        _shell = new GroovyShell(this.getLoader(),_binder,_compilerConfig);
    }


    @Override
    public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {



        long start = System.currentTimeMillis();

        File calcs = new File(taskExecutionContext.getTaskDir(),map.get("scriptDirectory"));
        String keystoreLocation = map.get("passwordKS");


        if (map.containsKey("metricPrefix")) {
            _metricPrefix = map.get("metricPrefix");
            if (!_metricPrefix.endsWith("|")) _metricPrefix = _metricPrefix+"|";
        }
        else _metricPrefix = null;

        if (map.containsKey("metricCache")) _bfMetricCached = Boolean.parseBoolean(map.get("metricCache"));

        //internalMonitoring
        if (map.containsKey("internalMonitoring")) _bfInternalMonitoring = Boolean.parseBoolean(map.get("internalMonitoring"));

        // Exit if Directory doesn't exists OR not a Directory
        if (!calcs.exists() || !calcs.isDirectory()) return null;

        File[] listFiles = calcs.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".metric");
            }
        });


        try {
            KeyStoreWrapper ks = new KeyStoreWrapper(new File(taskExecutionContext.getTaskDir(), keystoreLocation).getAbsolutePath(),
                        KeyStoreWrapper.PASSWD);

            CalculationEngine engine = getCalculationEngine(ks);


            for (File l: listFiles) {
                String filename = l.getName();

                try {
                    long startTime = System.currentTimeMillis();
                    List<MetricValueContainer> result = engine.execute(l);
                    for (MetricValueContainer value : result) {
                        String path = value.getPath();
                        path = fixPath(path,map);

                        logger.debug("Send Metric to new Path  "+path+"\n  --> "+value);
                        pushMetric(path,value,filename);


                    }

                    long duration = System.currentTimeMillis()-startTime;
                    if (_bfInternalMonitoring) {
                        getMetricWriter(fixPath("groovyAgent|"+filename+"|executeMs",map),
                                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(""+duration);
                        getMetricWriter(fixPath("groovyAgent|executeMs",map),
                                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(""+duration);
                        getMetricWriter(fixPath("groovyAgent|metrics",map),
                                MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(""+result.size());
                        getMetricWriter(fixPath("groovyAgent|"+filename+"|metrics",map),
                                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric(""+result.size());
                        getMetricWriter(fixPath("groovyAgent|"+filename+"|errors",map),
                                MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric("0");
                        getMetricWriter(fixPath("groovyAgent|errors",map),
                                MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric("0");

                    }

                } catch (CalculationException e) {
                    logger.error("Error while executing script :"+l.getAbsolutePath(),e);
                    getMetricWriter(fixPath("groovyAgent|"+filename+"|errors",map),
                            MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                            MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                            MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric("1");
                    getMetricWriter(fixPath("groovyAgent|errors",map),
                            MetricWriter.METRIC_AGGREGATION_TYPE_SUM,
                            MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                            MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL).printMetric("1");
                }

            }
        } catch (Exception e) {
            logger.error("Error while opening Password File",e);
            return new TaskOutput("ERROR");
        }

        postMetricsIfNeeded();
        logger.debug("Task Duration "+ ((System.currentTimeMillis()-start)/1000)+" seconds");
        return new TaskOutput("DONE");
    }

    private void postMetricsIfNeeded() {
        if(_bfMetricCached) {
            for (HashMap<String,MetricValueContainer> cache : _metricCache.values()) {
                for (Map.Entry<String,MetricValueContainer> me:cache.entrySet()) {
                    String path = me.getKey();
                    MetricValueContainer value = me.getValue();

                    if (value.isLife()) {
                        logger.debug("Reported Metric :"+path+" -> "+value);
                        getMetricWriter(path, value.getAggregation(), value.getTimeRollup(), value.getCluster())
                                .printMetric("" + value.getValue());
                        value.reported();
                    }
                }
            }
        }
    }

    private void pushMetric(String path, MetricValueContainer value, String filename) {
        if (_bfMetricCached) {
            if (!_metricCache.containsKey(filename)) {
                _metricCache.put(filename,new HashMap<String,MetricValueContainer>());
            }
            HashMap<String, MetricValueContainer> cache = _metricCache.get(filename);

            cache.put(path,value);
        } else {
            getMetricWriter(path,value.getAggregation(),value.getTimeRollup(),value.getCluster())
                    .printMetric(""+value.getValue());
        }
    }

    private CalculationEngine getCalculationEngine(KeyStoreWrapper ks) {
        if (_calcEngine == null) _calcEngine =  new CalculationEngine(ks);
        return _calcEngine;
    }

    private String fixPath(String path, Map<String, String> map) {
        if (path.startsWith("Custom Metrics|") || path.startsWith("Server|")) return path;
        else {
            if (_metricPrefix != null) return _metricPrefix+path;
            else return "Custom Metrics|"+path;
        }
    }
}

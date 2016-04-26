package com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java;


import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
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


        // Exit if Directory doesn't exists OR not a Directory
        if (!calcs.exists() || !calcs.isDirectory()) return null;

        File[] listFiles = calcs.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".metric");
            }
        });


        CalculationEngine engine = new CalculationEngine();
        for (File l: listFiles) {
            try {
                List<MetricValueContainer> result = engine.execute(l);
                for (MetricValueContainer value : result) {
                    String path = value.getPath();
                    path = fixPath(path,map);

                    logger.debug("Send Metric to new Path  "+path+"\n  --> "+value);
                    getMetricWriter(path,value.getAggregation(),value.getTimeRollup(),value.getCluster())
                            .printMetric(""+value.getValue());


                }
            } catch (CalculationException e) {
                logger.error("Error while executing script :"+l.getAbsolutePath(),e);
            }

        }

        logger.debug("TAsk Duration "+ ((System.currentTimeMillis()-start)/1000)+" seconds");
        return null;
    }

    private String fixPath(String path, Map<String, String> map) {
        if (path.startsWith("Custom Metrics|") || path.startsWith("Server|")) return path;
        else {
            return "Custom Metrics|"+path;
        }
    }
}

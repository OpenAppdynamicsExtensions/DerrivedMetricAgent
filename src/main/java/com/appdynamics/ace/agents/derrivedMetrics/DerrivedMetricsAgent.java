package com.appdynamics.ace.agents.derrivedMetrics;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import groovy.lang.GroovyShell;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.groovy.ant.Groovy;

import java.io.File;
import java.util.Map;
import java.util.Random;

/**
 * Created by stefan.marx on 13.04.16.
 */
public class DerrivedMetricsAgent extends AManagedMonitor {

    private static Logger logger = Logger.getLogger(DerrivedMetricsAgent.class);
    private final Random rnd;


    public DerrivedMetricsAgent (){
        logger.log(Level.INFO," LOADED !!!! "+new File(".").getAbsolutePath());
        rnd = new Random();
    }


    @Override
    public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {

        GroovyShell shell = new GroovyShell();


        return null;
    }
}

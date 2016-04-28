package com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java;

import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.util.KeyStoreWrapper;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by stefan.marx on 14.04.16.
 */
public class CalculationEngine {
    private static Logger logger = Logger.getLogger(CalculationEngine.class);
    private final CompilerConfiguration _compilerConfig;

    private final GroovyShell _shell;

    private File _path;
    private KeyStoreWrapper _ks;

    public CalculationEngine(File path,KeyStoreWrapper ks) {
          this(ks);
        _path = path;


    }

    public CalculationEngine(KeyStoreWrapper ks) {
        _ks = ks;
        _compilerConfig = new CompilerConfiguration();
        _compilerConfig.setScriptBaseClass("com.appdynamics.ace.agents.derivedMetrics.groovy.DSLDelegate"  );


        _shell = new GroovyShell(this.getClass().getClassLoader(),new Binding(),_compilerConfig);
    }

    public List<MetricValueContainer> execute(File calculation) throws CalculationException{

        try {

            MetricsBinding bind = new MetricsBinding(_ks);
            Script script = _shell.parse(calculation);

            try {
                script.setBinding(bind);

                script.run();

            } catch (Exception e) {
                e.printStackTrace();
            }


//            List<MetricValueContainer> metricResult = (List<MetricValueContainer>) values;
//            return metricResult;
            return bind.getValues();

        } catch (IOException e) {
            throw new CalculationException("IO Error while compiling",e);
        }


    }

    public List<MetricValueContainer> executeAll() throws CalculationException {
        List<MetricValueContainer> metricResults = new ArrayList<MetricValueContainer>();

        List<File> calcIterator = iterateFiles();
        for (File calculation : calcIterator) {
            logger.info("Execute Calculation :"+calculation);
            List<MetricValueContainer> r = this.execute(calculation);
            metricResults.addAll(r);

        }

        return metricResults;

    }

    private List<File> iterateFiles() throws CalculationException {
        // Exit if Directory doesn't exists OR not a Directory
        if (!_path.exists() || !_path.isDirectory()) {
            logger.error("Path not found :"+_path);
            return new ArrayList<File>();
        }

        File[] listFiles = _path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".metric");
            }
        });

        return Arrays.asList(listFiles);

    }
}

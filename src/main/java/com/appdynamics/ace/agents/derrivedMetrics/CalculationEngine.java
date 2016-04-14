package com.appdynamics.ace.agents.derrivedMetrics;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * Created by stefan.marx on 14.04.16.
 */
public class CalculationEngine {
    private static Logger logger = Logger.getLogger(CalculationEngine.class);
    private final CompilerConfiguration _compilerConfig;
    private final Binding _binder;
    private final GroovyShell _shell;

    private File _path;

    public CalculationEngine(File path) {


        _path = path;

        _compilerConfig = new CompilerConfiguration();
        _compilerConfig.setScriptBaseClass(DSLDelegate.class.getName());

        _binder = new Binding();
        _shell = new GroovyShell(this.getClass().getClassLoader(),_binder,_compilerConfig);
    }

    public void execute(File calculation) throws CalculationException{

        try {
            Script script = _shell.parse(calculation);
            Object result = script.run();

        } catch (IOException e) {
            throw new CalculationException("IO Error while compiling",e);
        }


    }

    public void executeAll() throws CalculationException {
        List<File> calcIterator = iterateFiles();
        for (File calculation : calcIterator) {
            logger.info("Execute Calculation :"+calculation);
            this.execute(calculation);
        }

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

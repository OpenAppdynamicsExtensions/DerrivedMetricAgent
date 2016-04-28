package com.appdynamics.ace.agents.derivedMetrics.groovy.test;

import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.CalculationEngine;
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.CalculationException;
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.MetricValueContainer;
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.util.KeyStoreWrapper;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.util.List;

/**
 * Created by stefan.marx on 14.04.16.
 */
public class TestCalculations {



    public static void main(String[] args) throws Exception {

        KeyStoreWrapper ksUtil = new KeyStoreWrapper("./metrics.ks", KeyStoreWrapper.PASSWD);
        ksUtil.setPasswd("demoEnvironment","Ghed7ped0geN");
        ksUtil.store();

        CalculationEngine engine = new CalculationEngine(new File("./src/main/calculations"),ksUtil);
        try {

            ConsoleAppender console = new ConsoleAppender(); //create appender
            //configure the appender
            String PATTERN = "%d [%p:%c] %m%n";
            console.setLayout(new PatternLayout(PATTERN));
            console.setThreshold(Level.INFO);
            console.activateOptions();

            Logger.getRootLogger().addAppender(console);


            List<MetricValueContainer> result = engine.executeAll();
            System.out.println(" RESULTS:");
            for (MetricValueContainer c : result) {
                System.out.println(c.toString());
            }


        } catch (CalculationException e) {
            e.printStackTrace();
        }

    }
}

package com.appdynamics.ace.agents.derrivedMetrics.test;

import com.appdynamics.ace.agents.derrivedMetrics.CalculationEngine;
import com.appdynamics.ace.agents.derrivedMetrics.CalculationException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;

/**
 * Created by stefan.marx on 14.04.16.
 */
public class TestCalculations {



    public static void main(String[] args) {
        CalculationEngine engine = new CalculationEngine(new File("./src/main/calculations"));
        try {

            ConsoleAppender console = new ConsoleAppender(); //create appender
            //configure the appender
            String PATTERN = "%d [%p:%C{1}] %m%n";
            console.setLayout(new PatternLayout(PATTERN));
            console.setThreshold(Level.INFO);
            console.activateOptions();
            //add appender to any Logger (here is root)
            //Logger.getLogger("com.appdynamics").addAppender(console);
            Logger.getRootLogger().addAppender(console);


            engine.executeAll();


        } catch (CalculationException e) {
            e.printStackTrace();
        }

    }
}

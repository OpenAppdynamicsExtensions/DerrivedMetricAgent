package com.appdynamics.ace.agents.derivedMetrics.test;

import com.appdynamics.ace.agents.derivedMetrics.CalculationEngine;
import com.appdynamics.ace.agents.derivedMetrics.CalculationException;
import com.appdynamics.ace.agents.derivedMetrics.util.KeyStoreWrapper;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;

/**
 * Created by stefan.marx on 14.04.16.
 */
public class TestCalculations {



    public static void main(String[] args) throws Exception {

        KeyStoreWrapper ksUtil = new KeyStoreWrapper("./metrics.ks", "Nigeheim");
        ksUtil.setPasswd("klkl","opop");
        System.out.println("PASSWORD :"+ksUtil.getPasswd("klkl"));
        ksUtil.store();

        CalculationEngine engine = new CalculationEngine(new File("./src/main/calculations"));
        try {

            ConsoleAppender console = new ConsoleAppender(); //create appender
            //configure the appender
            String PATTERN = "%d [%p:%c] %m%n";
            console.setLayout(new PatternLayout(PATTERN));
            console.setThreshold(Level.INFO);
            console.activateOptions();

            Logger.getRootLogger().addAppender(console);


            engine.executeAll();


        } catch (CalculationException e) {
            e.printStackTrace();
        }

    }
}

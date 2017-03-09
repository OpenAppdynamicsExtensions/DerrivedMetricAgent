package com.appdynamics.ace.agents.derivedMetrics.groovy.test;

import org.quartz.CronTrigger;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by stefan.marx on 08.03.17.
 */
public class TESTER {

    public static void main(String[] args) {
        try {

            //TODO: Build in processing power

            CronTrigger ct = new CronTrigger("script", "scriptname", "*/5 * * * * ?");
            CronTrigger ct2 = new CronTrigger("scripts", "scriptname", "0 0 02 * * ?");

            ct.triggered(null);
            System.out.println(ct.getNextFireTime());
            for (int j = 0; j <= 5; j++) {
                if(ct.getNextFireTime().before(new Date())) ct.triggered(null);
                Thread.sleep(2000);
                System.out.println("J:"+j+"::"+ct.getNextFireTime());


            }

            System.out.println(ct2.equals(ct));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

package com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java;

import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.util.KeyStoreWrapper;
import groovy.lang.Binding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan.marx on 26.04.16.
 */
public class MetricsBinding extends Binding {


    public KeyStoreWrapper getKs() {
        return _ks;
    }

    private KeyStoreWrapper _ks;

    public MetricsBinding(KeyStoreWrapper ks) {
        //TODO :

        _ks = ks;
    }

    public List<MetricValueContainer> getValues() {
        return values;
    }

    public void addAllToValues(List<MetricValueContainer> val) {
        values.addAll(val);
    }

    public void addToValues(MetricValueContainer val) {
        values.add(val);
    }


    List<MetricValueContainer> values = new ArrayList<MetricValueContainer>();
}

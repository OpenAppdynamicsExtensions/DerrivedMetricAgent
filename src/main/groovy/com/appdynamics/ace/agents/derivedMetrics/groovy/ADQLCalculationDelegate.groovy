package com.appdynamics.ace.agents.derivedMetrics.groovy

import de.appdynamics.client.eventservice.adql.dto.PayloadDataElement

/**
 * Created by stefan.marx on 12.02.17.
 */
class ADQLCalculationDelegate extends CalculationDelegateBase{

    PayloadDataElement _data

    public ADQLCalculationDelegate(PayloadDataElement data) {
        _data = data;
    }

    public String dumpHeader() {
        return _data.dumpHeader();
    }

    public def children(String key) {
        return _data.getChildren(key)
    }

    public def value (String path) {
        return _data.get(path)
    }

    public Date dateValue ( String path )  {
        return _data.getDate(path);
    }
}

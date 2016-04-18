package com.appdynamics.ace.agents.derrivedMetrics

import de.appdynamics.ace.metric.query.data.DataMap
import org.apache.log4j.Logger

/**
 * Created by stefan.marx on 18.04.16.
 */
class CalculationDelegate extends Script {

    private DataMap _allData
    private DataMap _filteredData
    Logger _logger;



    public CalculationDelegate (DataMap allData, DataMap filteredData) {

        this._filteredData = filteredData
        this._allData = allData
    }


    public void dumpData() {
        println "kkk"
        getLogger().info("Data:\n"+_filteredData.dumpData());
    }


    Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(CalculationDelegate.class.name)
        }

        return _logger;

    }

    @Override
    Object run() {
        return super.run();
    }
}

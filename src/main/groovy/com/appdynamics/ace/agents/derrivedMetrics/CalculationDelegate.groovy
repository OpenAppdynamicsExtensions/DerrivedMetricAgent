package com.appdynamics.ace.agents.derrivedMetrics

import de.appdynamics.ace.metric.query.data.DataMap
import de.appdynamics.ace.metric.query.data.ValueColumn
import de.appdynamics.ace.metric.query.data.ValueDataObject
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
        // getLogger().info("dump data for path starts");
        getLogger().debug("Data:\n"+_filteredData.dumpData());
        // getLogger().info("dump data for path ends");
    }

    def avg(String metricName) {
        def values = this.getValues(metricName);
        getLogger().debug("Avg is " + values.sum()/values.size());
        return values.sum()/values.size();
    }

    def min(String metricName) {
        def values = this.getValues(metricName);
        getLogger().debug("Min is " + values.min());
    }

    def max(String metricName) {
        def values = this.getValues(metricName);
        getLogger().debug("Max is " + values.max());
    }

    def sum(String metricName) {
        def values = this.getValues(metricName);
        getLogger().debug("Sum is " + values.sum());
    }

    def count(String metricName) {
        def values = this.getValues(metricName);
        getLogger().debug("Count is " + values.size());
    }

    def getValues(String metricName) {
        def values = [];
        ValueColumn metricColumn = _filteredData.findOrCreateValueColumn(metricName);
        ArrayList columnList = _filteredData._columns.getColumnsList();
        _filteredData.getOrderedRows().each { row ->
            columnList.each { column ->
                if(column.equals(metricColumn)) {
                    ValueDataObject data = row.findData(column);
                    values << data.getValue();
                }
            }
        }
        return values;
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

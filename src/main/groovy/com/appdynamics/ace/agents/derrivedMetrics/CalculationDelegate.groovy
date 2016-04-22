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

    private def _reportedMetrics = [];



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
        if(!hasColumn(metricName)) throw new CalculationException("Column $metricName not found!");

        def values ;
        if (hasColumn(metricName+" (min)")) values = this.getValues(metricName+" (min)");
        else values = this.getValues(metricName);

        getLogger().debug("Min is " + values.min());
        return values.min();
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

    boolean hasColumn(String metricName) {
        return _filteredData.getHeaderColumn(metricName) != null;
    }

    def getValues(String metricName) throws CalculationException {
        def values = [];
        ValueColumn metricColumn = _filteredData.getHeaderColumn(metricName);

        if (metricColumn == null) throw new CalculationException("Column $metricName not found!")

        return _filteredData.getValues(metricColumn).collect {ValueDataObject it-> it.getValue();};
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

    def methodMissing(String name, args) {

        def argList = args.collect { return it}
        getLogger().error("Missing Method : $name ( ${argList} ) ");
    }
}

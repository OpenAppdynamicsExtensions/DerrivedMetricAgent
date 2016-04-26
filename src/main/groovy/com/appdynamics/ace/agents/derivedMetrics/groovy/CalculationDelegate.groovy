package com.appdynamics.ace.agents.derivedMetrics.groovy

import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.CalculationException
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.MetricValueContainer
import de.appdynamics.ace.metric.query.data.DataMap
import de.appdynamics.ace.metric.query.data.DataRow
import de.appdynamics.ace.metric.query.data.ValueColumn
import de.appdynamics.ace.metric.query.data.ValueDataObject
import de.appdynamics.ace.metric.query.data.TimestampColumn
import de.appdynamics.ace.metric.query.data.TimestampDataObject
import org.apache.log4j.Logger

/**
 * Created by stefan.marx on 18.04.16.
 */
class CalculationDelegate extends Script {

    private DataMap _allData
    private DataMap _filteredData
    Logger _logger;

    public Map<String,List<MetricValueContainer>> _metricValues = [:];




    public CalculationDelegate (DataMap allData, DataMap filteredData) {

        this._filteredData = filteredData
        this._allData = allData
    }

    public void dumpData() {
        getLogger().info("Data:\n"+_filteredData.dumpData());
    }

    def avg(String metricName) {
        assertColumnExist(metricName);
        def values = this.getValues(metricName);
        getLogger().debug("Avg is " + values.sum()/values.size());
        return values.sum()/values.size();
    }

    def min(String metricName) {
        assertColumnExist(metricName);

        def values;
        if (hasColumn(metricName+" (min)"))
            values = this.getValues(metricName+" (min)");
        else
            values = this.getValues(metricName);
        getLogger().debug("Min is " + values.min());
        return values.min();
    }

    def max(String metricName) {
        assertColumnExist(metricName);

        def values;
        if (hasColumn(metricName+" (max)"))
            values = this.getValues(metricName+" (max)");
        else
            values = this.getValues(metricName);
        getLogger().debug("Max is " + values.max());
        return values.max();
    }

    def sum(String metricName) {
        assertColumnExist(metricName);

        def values;
        if (hasColumn(metricName+" (sum)"))
            values = this.getValues(metricName+" (sum)");
        else
            values = this.getValues(metricName);
        getLogger().debug("Sum is " + values.sum());
        return values.sum();
    }

    def count(String metricName) {
        assertColumnExist(metricName);

        def values = this.getValues(metricName);
        getLogger().debug("Count is " + values.size());
        return values.size();
    }

    def values(String metricName) {
        assertColumnExist(metricName);

        def values = this.getValues(metricName);
        getLogger().debug("Values are " + values);
        return values;
    }

    def values(String ... metricNames) {

        def cols = metricNames.collect { name ->
            assertColumnExist(name)
            _filteredData.getHeaderColumn(name);
        }

        //assertions hapened in line, set list of rows

        def result = _filteredData.getOrderedRows().collect() { DataRow row ->
            cols.collectEntries() { col ->
                def val;
                def d = row.getData(col);
                val = d.getTextValue();
                if (d instanceof ValueDataObject) val = (d as ValueDataObject).getValue();
                if (d instanceof  TimestampDataObject) val = (d as TimestampDataObject).getTimestampValue();

                [(col.getName()): val]
            }
        }
    }

    def first(String metricName) {
        assertColumnExist(metricName);

        def values = this.getValues(metricName);
        getLogger().debug("First value is " + values.first());
        return values.first();
    }

    def last(String metricName) {
        assertColumnExist(metricName);

        def values = this.getValues(metricName);
        getLogger().debug("Last value is " + values.last());
        return values.last();
    }

    def delta(String metricName) {
        assertColumnExist(metricName);

        def values = this.getValues(metricName);
        if(values.size()>1) {
            getLogger().debug("Last value is " + values.last() + ", second last value is " + values[-2] + ". Hence the delta is " + values.last() - values[-2]);
            return values.last() - values[-2];
        } else {
           throw new CalculationException("There are less than two values to derive a delta from.");
            return null;
        }
    }

    def deltaMin(String metricName) {
        assertColumnExist(metricName);

        def timestampValues = this.getValueTimestamps();
        def values = this.getValues(metricName);
        if(timestampValues.size()>1) {
            def timestampDelta = timestampValues.last().getTime() - timestampValues[-2].getTime();
            if(timestampDelta == 60000) {
                return values.last() - values[-2];
            } else if(timestampDelta > 60000){
                return (values.last() - values[-2]) / (timestampDelta / 60000);
            } else {
                // the timestamp delta should actually never be less than 1 minute since this is the finest granularity where data is collected.
            }
        } else {
            throw new CalculationException("There are less than two values to derive a delta from.");
            return null;
        }
    }

    def percentage(String metricName, double percent) {
        assertColumnExist(metricName);

        def values = this.getValues(metricName);
        def baseValue;
        if(metricName.endsWith("(min)"))
            baseValue = values.min()
        else if(metricName.endsWith("(max)"))
            baseValue = values.max()
        else if(metricName.endsWith("(sum)"))
            baseValue = values.sum()
        else
            baseValue = values.sum()/values.size()
        getLogger().debug(percent + "% from " + metricName + " is " + (baseValue/100)*percent);
        return (baseValue/100)*percent;
    }

    def startTime() {
        def timestamps = this.getValueTimestamps();
        return timestamps.first();
    }

    def endTime() {
        def timestamps = this.getValueTimestamps();
        return timestamps.last();
    }

    def duration() {
        def timestamps = this.getValueTimestamps();
        return timestamps.last().getTime() - timestamps.first().getTime();
    }

    boolean hasColumn(String metricName) {
        return _filteredData.getHeaderColumn(metricName) != null;
    }

    def getValues(String metricName) throws CalculationException {
        def values = [];
        ValueColumn metricColumn = _filteredData.getHeaderColumn(metricName);
        ArrayList columnList = _filteredData._columns.getColumnsList();
        _filteredData.getOrderedRows().each { row ->
            columnList.each { column ->
                if(column.equals(metricColumn)) {
                    ValueDataObject data = row.findData(column);
                    values << data?.getValue()?:0; // no value equals to 0
                }
            }
        }
        /*
        return _filteredData.getValues(metricColumn).collect {ValueDataObject it ->
            it.getValue();
        };
        */
        return values;
    }

    def getValueTimestamps() throws CalculationException {
        def timestamps = [];
        TimestampColumn metricColumn = _filteredData.getHeaderColumn("time");
        ArrayList columnList = _filteredData._columns.getColumnsList();
        _filteredData.getOrderedRows().each { row ->
            columnList.each { column ->
                if(column.equals(metricColumn)) {
                    TimestampDataObject timestamp = row.findData(column);
                    timestamps << timestamp.getTimestampValue();
                }
            }
        }
        return timestamps;
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

    public static final String AVERAGE = "AVERAGE";
    public static final String SUM = "SUM";
    public static final String OBSERVATION = "OBSERVATION";
    public static final String CURRENT = "CURRENT";
    public static final String INDIVIDUAL = "INDIVIDUAL";
    public static final String COLLECTIVE = "COLLECTIVE";

    /** Report a Metric to Agent or CLI,
     *
     * @param path
     * @param value
     * @param aggregation multi execution (node to tier aggregation) [ AVERAGE, SUM, OBSERVATION ]
     * @param timeRollup Time Rollout (1min -> 10 min -> 60 min) [AVERAGE, SUM, CURRENT  ]
     * @param cluster  Cluster Rollup  (node to tier aggregation) [INDIVIDUAL, COLLECTIVE]
     */
    def reportMetric(String path,def value,
                     String aggregation = AVERAGE,
                     String timeRollup = AVERAGE,
                     String cluster = INDIVIDUAL) {
        def metricValue = new MetricValueContainer ( path, (long)value,
                        aggregation,timeRollup,cluster) ;

        def values = _metricValues[path] ?: []
        values += metricValue;
        _metricValues [path] = values;
    }


    def methodMissing(String name, args) {
        def argList = args.collect { return it}
        getLogger().error("Missing Method : $name ( ${argList.join(',')} ) ");
        throw new CalculationException("Missing Method : $name  ")
    }

    private void assertColumnExist(String name) {
        if (!hasColumn(name)) throw new CalculationException("Column $metricName not found!");
    }
}

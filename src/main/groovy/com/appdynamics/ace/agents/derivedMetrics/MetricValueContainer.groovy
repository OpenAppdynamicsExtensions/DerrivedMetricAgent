package com.appdynamics.ace.agents.derivedMetrics

/**
 * Created by stefan.marx on 25.04.16.
 */
class MetricValueContainer {

    private String _path
    private long _value
    private String _aggregation
    private String _timeRollup
    private String _cluster
    private Date _evalTime = new Date();

    String get_path() {
        return _path
    }

    long get_value() {
        return _value
    }

    String get_aggregation() {
        return _aggregation
    }

    String get_timeRollup() {
        return _timeRollup
    }

    String get_cluster() {
        return _cluster
    }

    Date get_evalTime() {
        return _evalTime
    }

    public MetricValueContainer (String path, long value,
                                 String aggregation, String timeRollup, String cluster) {

        this._cluster = cluster
        this._timeRollup = timeRollup
        this._aggregation = aggregation
        this._value = value
        this._path = path
    }


    public String toString () {
        return "$_evalTime : ($_path) -> $_value [$_aggregation,$_timeRollup,$_cluster]";
    }

    public String toValueString () {
        return " ($_path) \t-> $_value ";
    }

}

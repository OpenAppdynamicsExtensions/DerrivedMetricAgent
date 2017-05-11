package com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java;

import java.util.Date;

/**
 * Created by stefan.marx on 25.04.16.
 */
public class MetricValueContainer {

        private String _path;
        private long _value ;
        private String _aggregation;
        private String _timeRollup  ;
    private int _ttl;
    private String _cluster      ;
        private Date _evalTime = new Date();

        String getPath() {
            return _path ;
        }

        long getValue() {
            return _value ;
        }

        String getAggregation() {
            return _aggregation;
        }

        String getTimeRollup() {
            return _timeRollup;
        }

        String getCluster() {
            return _cluster    ;
        }

        Date getEvalTime() {
            return _evalTime    ;
        }

        public MetricValueContainer (String path, long value,int ttl,
                                     String aggregation, String timeRollup, String cluster) {
            this._ttl = ttl;

            this._cluster = cluster;
            this._timeRollup = timeRollup;
            this._aggregation = aggregation;
            this._value = value;
            this._path = path;
        }


        public String toString () {
            return String.format("%s : (%s) -> %d [%s,%s,%s]  TTL:%d",_evalTime,_path,_value,_aggregation,_timeRollup,_cluster,_ttl);

        }

        public String getValueString () {
            return String.format(" (%s) \t-> %d",_path,_value);
        }

    public synchronized boolean isLife() {
        return _ttl>0;
    }

    public synchronized void reported() {
        _ttl--;
    }
}

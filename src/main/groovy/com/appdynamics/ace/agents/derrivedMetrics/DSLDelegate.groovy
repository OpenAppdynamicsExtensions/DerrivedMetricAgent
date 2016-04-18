package com.appdynamics.ace.agents.derrivedMetrics

import de.appdynamics.ace.metric.query.data.Column
import de.appdynamics.ace.metric.query.data.DataMap
import de.appdynamics.ace.metric.query.data.DataObject
import de.appdynamics.ace.metric.query.data.DataRow
import de.appdynamics.ace.metric.query.parser.CompiledRestMetricQuery
import de.appdynamics.ace.metric.query.parser.MetricQuery
import de.appdynamics.ace.metric.query.rest.ControllerRestAccess
import org.apache.log4j.Logger



class DSLDelegate extends Script  {
    ControllerRestAccess _connection;
    Logger _logger;


    def connect(Closure cl) {
        def conn = new Connection();
        def code = cl.rehydrate(conn, this, this);
        code.resolveStrategy = Closure.DELEGATE_FIRST;
        code();
        _connection = conn.connect();

        getLogger().info("Connected :"+_connection.dump());
    }


    def calculate (String query, Closure calculation) {
        MetricQuery mq = new MetricQuery();

        CompiledRestMetricQuery erg = mq.parse( query);
        DataMap map = erg.execute(_connection);
        Column pCol = map.getHeader().find(){Column c->
            return c.name == "path";
        }

        Set<String> paths = map.getOrderedRows().collect { DataRow row ->
            DataObject data = row.findData(pCol);
            return data.textValue;
        } as Set;

        List<DataMap> maps
        maps = map.splitBy(pCol) ;
        maps.each { splitMap ->

            // maybe this needs to become delegate
            // NEED ATTENTION, Goal is to r4edelegate to calculations ....!!!
            //
            cal = new  CalculationDelegate(map,splitMap);

            def code = calculation.rehydrate(cal,cal,cal);
            code.resolveStrategy = Closure.DELEGATE_FIRST;
            getLogger().info("Before execute")
            code();

            getLogger().info("After execute")
        }



        // iterate on all Paths



    }


    /*
    * connect {
    *  controller "hostname:8090"
    *  account "customer1"
    *  user "smarx"
    *  password "lld"
    * }
     */
    private class Connection {
        private String _host ="localhost";
        private int _port =8090 ;
        String _account = "Customer1";
        String _user ;
        String _password;
        boolean _ssl = false;

        void controller (String h) {
            String[] segments ;
            segments = h.split(":");
            _host = segments[0];
            _port = Integer.parseInt(segments[1]);
        }

        void account (String ac) {
            _account = ac;
        }

        void user(String u) {
            _user = u;
        }

        void password(String p) {
            _password=p;
        }

        void using_ssl() {
            _ssl = true;
        }

        ControllerRestAccess connect() {
            assert(_user != null);
            assert(_password != null);
            return new ControllerRestAccess(_host,""+_port,_ssl,_user,_password,_account);
        }


    }




    Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(DSLDelegate.class)
        }

        return _logger;

    }

    @Override
    Object run() {
        return super.run();
    }


}

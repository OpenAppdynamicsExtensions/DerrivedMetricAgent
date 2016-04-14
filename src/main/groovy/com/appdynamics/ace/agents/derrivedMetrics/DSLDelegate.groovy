package com.appdynamics.ace.agents.derrivedMetrics

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

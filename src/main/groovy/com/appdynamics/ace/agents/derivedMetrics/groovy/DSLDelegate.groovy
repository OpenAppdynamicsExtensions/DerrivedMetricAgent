package com.appdynamics.ace.agents.derivedMetrics.groovy

import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.CalculationException
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.MetricValueContainer
import com.appdynamics.ace.com.appdynamics.ace.agents.derivedMetrics.java.MetricsBinding
import de.appdynamics.ace.metric.query.data.Column
import de.appdynamics.ace.metric.query.data.DataMap
import de.appdynamics.ace.metric.query.data.DataObject
import de.appdynamics.ace.metric.query.data.DataRow
import de.appdynamics.ace.metric.query.parser.CompiledRestMetricQuery
import de.appdynamics.ace.metric.query.parser.MetricQuery
import de.appdynamics.ace.metric.query.rest.ControllerRestAccess
import de.appdynamics.client.eventservice.adql.ADQLConnection
import de.appdynamics.client.eventservice.adql.ADQLConnectionBuilder
import de.appdynamics.client.eventservice.adql.ADQLQuery
import de.appdynamics.client.eventservice.adql.ADQLResult
import de.appdynamics.client.eventservice.adql.AnalyticsException
import de.appdynamics.client.eventservice.adql.api.ADQLDataListener
import de.appdynamics.client.eventservice.adql.api.FilterState
import de.appdynamics.client.eventservice.adql.dto.PayloadDataElement
import org.apache.log4j.Logger
import org.quartz.CronTrigger


class DSLDelegate extends Script   {
    ControllerRestAccess _connection;
    ADQLConnection _connectionADQL;
    Logger _logger;


    public List<MetricValueContainer> _allValues = [];
    private MetricsBinding _metricsBinding


    public DSLDelegate() {

    }

    def connect(Closure cl) {
        def conn = new Connection(_metricsBinding);
        def code = cl.rehydrate(conn, this, this);
        code.resolveStrategy = Closure.DELEGATE_FIRST;
        code();
        _connection = conn.connect();

        getLogger().info("Connected :"+_connection.dump());
    }

    def connectAnalytics(Closure cl) {
        def conn = new ConnectionAnalytics(_metricsBinding);
        def code = cl.rehydrate(conn, this, this);
        code.resolveStrategy = Closure.DELEGATE_FIRST;
        code();

        _connectionADQL = conn.connect();

        getLogger().info("Connected :"+_connectionADQL.dump());
    }




    List<MetricValueContainer> calculateAnalytics (String query,Date start = new Date(new Date().time-(1000*60*60*4)),
                                                   Date end = new Date(),
                                                   boolean retrieveAll=false,
                                                   Closure calculation) {

        int limit = 1000;
        if (query.toLowerCase().contains(" limit ")) limit = 0;

        ADQLQuery q =  _connectionADQL.query()

        List<MetricValueContainer> _values = []


        q.withADQL(query)
            .withLimit(limit)
            .withStart(start)
            .withEnd(end)
            .withDataListener( new ADQLDataListener() {
            @Override
            FilterState filter(PayloadDataElement data) throws AnalyticsException {
                return FilterState.NONE
            }

            @Override
            void process(PayloadDataElement data) throws AnalyticsException {
                ADQLCalculationDelegate cal = new  ADQLCalculationDelegate(data);

                def code = calculation.rehydrate(cal,cal,cal);
                code.resolveStrategy = Closure.DELEGATE_FIRST;
                try {
                    code();
                    def v = cal._metricValues.collect(){k, e -> e}.flatten();
                    _values.addAll(v);

                } catch (CalculationException e) {
                    getLogger().error("Error during calculation : $query ",e);
                }
            }
        })

        ADQLResult res = q.execute(true);

        while (res.isPartialComplete() && retrieveAll ) {
           try {
               List<PayloadDataElement> data = res.getPayload().getAllData()
               Date et = data.get(data.size()-1).getDate("eventTimestamp");
               res = q.continueQuery(et,true,data.get(data.size()-1));
           } catch (Exception ex ) {
               getLogger().error("Limit reached, continuing Transaction not possible because Field eventTimestamp not found.")
               break;
           }
        }


        this._metricsBinding.addAllToValues(_values)
        return _values;
    }

    List<MetricValueContainer> calculate (String query, Closure calculation) {
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
        List<MetricValueContainer> _values = []
        maps = map.splitBy(pCol) ;
        maps.each { splitMap ->

            // maybe this needs to become delegate
            // NEED ATTENTION, Goal is to r4edelegate to calculations ....!!!
            //
            CalculationDelegate cal = new  CalculationDelegate(map,splitMap);

            def code = calculation.rehydrate(cal,cal,cal);
            code.resolveStrategy = Closure.DELEGATE_FIRST;
            try {
                code();
                def v = cal._metricValues.collect(){k, e -> e}.flatten();
                _values.addAll(v);

            } catch (CalculationException e) {
                this.getLogger().error("Error during calculation : $query ",e);
            }

        }


        this._metricsBinding.addAllToValues(_values)
        return _values;


        // iterate on all Paths



    }

    def cron(String name,String cron,boolean shouldPreStart = false,Closure exec) {

        name = name ?: "CR:$cron"
        String triggername = _metricsBinding.scriptName+"::"+name;
        CronTrigger cronTrigger = _metricsBinding.calculationEngine.getCronTrigger(triggername)
        if (cronTrigger == null) {
            cronTrigger = new CronTrigger(triggername,"SCRIPT",cron)
            if(shouldPreStart) cronTrigger.setNextFireTime(new Date (System.currentTimeMillis()-1000))
            else cronTrigger.triggered(null);


            _metricsBinding.calculationEngine.storeCronTrigger(cronTrigger)
        }

        if (cronTrigger.nextFireTime.before(new Date())) {
            exec.call();
            cronTrigger.triggered(null);
            logger.debug("Next Execution at :${cronTrigger.nextFireTime}")
        } else {
            logger.debug("Skipping execution, next valid trigger at: $cronTrigger.nextFireTime")
        }


    }

    def cron(Map args,Closure cl) {
        cron(args?.name,args?.pattern,args?.startOnInit as boolean,cl);
    }

    /*
    * connectAnalytics {
    *   url "analyticsUrl"
    *   user  ""
    *   passwd ""
    *   account ""
    *   token ""
     */
    private class ConnectionAnalytics {
        private MetricsBinding _bind
        ProxyConnection _proxy

        public ConnectionAnalytics(MetricsBinding bind) {
            this._bind = bind
        }

        String _user ;
        String _password;
        URI _uri;
        String _account;
        String _token;

        void url(String url) {
            _uri = new URI(url);
        }
        void user(String u) {
            _user = u;
        }

        void password(String p) {
            _password=p;
        }

        void passwordKey(String p) {
            _password=_bind.getKs().getPasswd(p);
        }

        void account(String u) {
            _account = u;
        }

        void token (String p) {
            _token=p;
        }

        void tokenKey(String p) {
            _token=_bind.getKs().getPasswd(p);
        }

        def proxy (Closure cl) {
            def proxy = new ProxyConnection(_bind);
            def code = cl.rehydrate(proxy,this,this);
            code.resolveStrategy = Closure.DELEGATE_FIRST;
            code();

            _proxy = proxy;
        }

        ADQLConnection connect(){
            ADQLConnectionBuilder builder = new ADQLConnectionBuilder();
            if (_uri != null) builder.withUri(_uri)

            if (_user != null && _password != null) {
                builder.withBasicAuth(_user,_password);
            }

            if (_token != null && _account != null) {
                builder.withTokenAuth(_account,_token);
            }

            if (_proxy != null) {
                builder.withProxy(_proxy.host,_proxy.port)
            }


            return builder.build();
        }
    }


    private class ProxyConnection {
        private MetricsBinding _bind

        public ProxyConnection(MetricsBinding bind) {
            this._bind = bind
        }

        private String host = "localhost";
        private int port = 8080;

        void host (String h) {
            host = h
        }

        void port (int p) {
            port = p
        }

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


        private MetricsBinding _bind

        public Connection(MetricsBinding bind) {
            this._bind = bind
        }


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

        void passwordKey(String p) {
            _password=_bind.getKs().getPasswd(p);
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
                     int ttl = 1,
                     String aggregation = AVERAGE,
                     String timeRollup = AVERAGE,
                     String cluster = INDIVIDUAL) {
        logger.info("report metric $path : $value");
        def metricValue = new MetricValueContainer ( path, (long)value,ttl,
                aggregation,timeRollup,cluster) ;

        this._metricsBinding.addToValues(metricValue)
    }


    Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(DSLDelegate.class)
        }

        return _logger;

    }

    @Override
    void setBinding(Binding binding) {
        if(binding instanceof MetricsBinding)   this._metricsBinding = binding ;
    }

    @Override
    Object run() {

          super.run();

    }

    Object executeMetricScript(MetricsBinding metricsBinding) {
        this._metricsBinding = metricsBinding
        this.run()

    }


}

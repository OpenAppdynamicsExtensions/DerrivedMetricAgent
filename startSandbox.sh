#!/bin/bash
JAVA_OPTIONS='-Xmx512m -Xms512m -Dappdynamics.agent.applicationName=MAU
-Dappdynamics.controller.hostName=smarxdocker2.ddns.net
-Dappdynamics.controller.port=8090
-Dappdynamics.agent.tierName=DATA
-Dappdynamics.agent.nodeName=MAU_Collector
-Dappdynamics.agent.uniqueHostId=TCollector2
-Dappdynamics.agent.accountAccessKey=f6be7c52-1b59-4d5e-abd8-a982ea78a594'

java $JAVA_OPTIONS -jar ./build/sandbox/agent/machineagent.jar
#34178a39-50e2-4e7e-bbd9-b5c83c737299

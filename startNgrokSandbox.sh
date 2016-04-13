#!/bin/bash
JAVA_OPTIONS="-Dappdynamics.agent.applicationName=APPDEX_LBG
-Dappdynamics.controller.hostName=master.appdynamics.ngrok.com.ngrok.com
-Dappdynamics.controller.port=80
-Dappdynamics.agent.tierName=APPDEX
-Dappdynamics.agent.nodeName=NodeUTIL
-Dappdynamics.agent.accountAccessKey=d2258e09-d607-44e6-9d54-81fb4c066541
-Dcom.appdynamics.machineAgent.ignoreEmptyMetrics=true
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

java $JAVA_OPTIONS -jar ./build/sandbox/agent/machineagent.jar

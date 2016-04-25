#!/bin/bash
JAVA_OPTIONS='-Xmx512m -Xms512m -Dappdynamics.agent.applicationName=APPDEX_LBG_test
-Dappdynamics.controller.hostName=docker.noip.me
-Dappdynamics.controller.port=8090
-Dappdynamics.agent.tierName=APPDEX
-Dappdynamics.agent.nodeName=NodeUTIL
-Dappdynamics.agent.uniqueHostId=TESTER123
-Dappdynamics.agent.accountAccessKey=dbce05e5-c093-46ed-b267-d82750caf1bf
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'

java $JAVA_OPTIONS -jar ./build/sandbox/agent/machineagent.jar
#34178a39-50e2-4e7e-bbd9-b5c83c737299
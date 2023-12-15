#!/bin/bash

OCP_HOSTNAME=$1
IP=$(ping -c1 -t1 console-openshift-console.apps.$OCP_HOSTNAME | grep "PING")
IP=${IP%)*}
IP=${IP##*(}
echo $IP
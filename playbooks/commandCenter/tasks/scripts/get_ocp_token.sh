#!/bin/bash

OCP_USERNAME=$1
OCP_PASSWORD=$2
OCP_HOSTNAME=$3
OUT=$(curl -u $OCP_USERNAME:$OCP_PASSWORD -kv -H "X-CSRF-Token: xxx" "https://oauth-openshift.apps.$OCP_HOSTNAME/oauth/authorize?client_id=openshift-challenging-client&response_type=token" 2>&1 | less | grep "Location:")
ACCESS_TOKEN=${OUT##*access_token=}
ACCESS_TOKEN=${ACCESS_TOKEN%%&*}
echo $ACCESS_TOKEN

#!/bin/bash

#################################################################################
# Copyright (c) 2022 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     IBM Corporation - initial API and implementation
#################################################################################

#####################################################################################
# Pre-requisites: 
# Must have python 3.4 installed and 'python3' in your PATH
# Must have docker-compose on PATH
# Must have docker service running
#####################################################################################

docker-compose -f services/docker-compose.yml down
docker-compose -f services/docker-compose.yml up -d
if [ ! -d /tmp/space-rover-venv ]
then
	echo "Creating venv for space rover gesture control service in /tmp/space-rover-venv"
	python3.7 -m venv /tmp/space-rover-venv
fi
source /tmp/space-rover-venv/bin/activate
pip install -r gestures/openCV_implementation/src/requirements.txt

while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:9070/health)" != "200" ]]; 
do 
	sleep 5
	echo "waiting for game service to come online"
done

python3 gestures/openCV_implementation/src/GestureRecognitionCVv2.py



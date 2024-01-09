#!/bin/bash

#################################################################################
# Copyright (c) 2022 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#################################################################################

#####################################################################################
# Pre-requisites:
# Must have python 3.7 installed and 'python3.7' in your PATH
# Must have docker-compose on PATH
# Must have docker service running
#####################################################################################

podman-compose -f services/docker-compose.yml down
podman-compose -f services/docker-compose.yml up -d

PYTHON_VENV_DIR="./venv"
if [ ! -d ${PYTHON_VENV_DIR} ]
then
    echo "Creating venv for space rover gesture control service in ${PYTHON_VENV_DIR}"
    python3.7 -m venv ${PYTHON_VENV_DIR}
fi
source ${PYTHON_VENV_DIR}/bin/activate
pip install -r gestures/openCV_implementation/src/requirements.txt

while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:9070/health)" != "200" ]];
do
    sleep 5
    echo "waiting for game service to come online"
done

python3 gestures/openCV_implementation/src/GestureRecognitionCVv2.py


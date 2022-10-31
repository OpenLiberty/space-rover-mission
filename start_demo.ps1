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
# Must have python 3.7 installed and 'python3.7' in your PATH
# Must have docker-compose on PATH
# Must have docker service running
#####################################################################################

function Test-GameServerHealth {
	try {
		Invoke-RestMethod -Uri http://localhost:9070/health; 
		return $true;
	}
	catch {
		Write-Output $_
		return $false;
	}
		
		
}

docker-compose -f services/docker-compose.yml down
docker-compose -f services/docker-compose.yml up -d

$VenvPath = "$env:TEMP\space-rover-venv"
if (-not(Test-Path -Path $VenvPath -PathType Leaf)) {

	echo "Creating venv for space rover gesture control service in $env:TEMP\space-rover-venv"
	python3.7 -m venv $VenvPath
}

& "$VenvPath\Scripts\Activate.ps1"
pip install -r gestures/openCV_implementation/src/requirements.txt

while (-not(Test-GameServerHealth)) {
	Start-Sleep -Seconds 5
	Write-Output "Waiting for games service to come online"
}

python gestures/openCV_implementation/src/GestureRecognitionCVv2.py

deactivate
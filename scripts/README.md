# Space Rover Mission - Ansible Playbook Scripts

# Prerequisites
- Install `ansible-playbook` CLI
- Docker Desktop
  - Enable Kubernetes in settings
  - If using ARM processor, enable Rosetta emulation

# Cluster Administrator
## 1. Cluster First Time Setup
- **Prerequisite**: You must have access to an OpenShift Container Platform cluster (>= v4.11)
- Login to OpenShift Container Platform (>= v4.11) cluster and install `oc` CLI onto your local machine
- Create a `.env` file from template [.env.template](../.env.template)
  - In the OpenShift UI, generate a login token and set the token (starts with `sha256~...`) as the `OCP_TOKEN` field in `.env`
    - On this same page, `OCP_HOSTNAME` field can be found in the text between `https://` and `:6443`, also set this in `.env`
  - Set `OCP_PROJECT_NAME` to a project name of choice (i.e. `space-rover`)
  - Set `OCP_THANOS_PROJECT_NAME` to a project name of choice (i.e. `thanos`)
- Run the `./clusterFirstTimeSetup` script and add the `OCP_INTERNAL_REGISTRY` flag to `.env` once completed
- Finally, add the `OCP_INTERNAL_REGISTRY` string to Docker Desktop as an insecure registry. 

## 2. Build a Command Center
- **Prerequisite**: You must have access to an OpenShift Container Platform cluster (>= v4.11) and have ran the `./clusterFirstTimeSetup` script
- Run `./buildCommandCenter` to provision OpenShift Container Platform cluster that is ready to aggregate Prometheus Metrics from multiple Space Rover games with Thanos
  -  Distribute the `THANOS_RECEIVE_URL` value to individuals who want to run Space Rover into their local environment's `.env`
  -  For each local Space Rover game, update `MACHINE_NAME` in `.env` to a unique lowercase + alphanumeric value and run `./generateToken`
    - This generates a `THANOS_RECEIVE_CREDENTIALS` value that the client can use to authenticate to the OpenShift cluster

# User
## 1. Build Docker images
- Navigate to `cd ../kubernetes/` and run `./buildServices.sh` to build all the Space Rover microservices

## 2. Launch the Space Rover
- Forward local Prometheus metrics data to the cloud deployment of Thanos by specifying `THANOS_RECEIVE_URL` and `THANOS_RECEIVE_CREDENTIALS` in `.env`
(You must obtain this from a cluster administrator who has followed the cluster deployment steps above)
- Run `./launch` to start the game on your local machine

## 3. Land the Space Rover
- Run `./land` to stop the game on your local machine
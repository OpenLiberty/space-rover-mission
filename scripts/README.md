# Ansible Playbook Scripts

# Prerequisites
- Install `ansible-playbook` CLI
- Create a `.env` file from template [.env.template](../.env.template)
  - Login to OpenShift Container Platform (>= v4.11) cluster and install `oc` binary onto your local machine
  - In the OpenShift UI, generate a login token and paste the token (starting with `sha...`) into the `.env` file's `OCP_TOKEN` field
    - On this same page, `OCP_HOSTNAME` field can be found in the text between `https://` and `:6443`
  - Set `OCP_PROJECT_NAME` to a project name of choice (i.e. `space-rover`)
  - Set `OCP_THANOS_PROJECT_NAME` to a project name of choice (i.e. `thanos`)
- Docker Desktop
  - Enable Kubernetes in settings
  - If using ARM processor, enable Rosetta emulation

# Build a Command Center
- **Prerequisite**: You must have access to an OpenShift Container Platform cluster (>= v4.11)
- Run `./buildCommandCenter` to provision OpenShift Container Platform cluster that is ready to aggregate Prometheus Metrics from multiple Space Rover games with Thanos

# Launch the Space Rover
- Forward local Prometheus metrics data to Thanos online by specifying `THANOS_RECEIVE_URL` in `.env`
  - In order to use this, you must also receive a token from the OpenShift cluster
  - Run the `./generateToken` script which creates the `THANOS_RECEIVE_CREDENTIALS` you can set in `.env`
- Annotate the metrics data using a unique ID by setting the `MACHINE_NAME` field in `.env`
- Run `./launch` to start the game on your local machine.

# Land the Space Rover
- Run `./land` to stop the game on your local machine.
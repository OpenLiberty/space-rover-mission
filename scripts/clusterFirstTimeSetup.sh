#!/bin/bash
echo "==== Cluster First Time Setup ===="

REPO_DIR=$(git rev-parse --show-toplevel)
set -a
source $REPO_DIR/.env
set +a
ansible-playbook $REPO_DIR/ansible-playbooks/clusterFirstTimeSetup/main.yml
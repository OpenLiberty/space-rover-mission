#!/bin/bash
echo "==== Space Rover Launch ===="

REPO_DIR=$(git rev-parse --show-toplevel)
set -a
source $REPO_DIR/.env
set +a
ansible-playbook $REPO_DIR/ansible-playbooks/launchWithLocalKafka/main.yml

#!/bin/bash
echo "==== Generate Token from Command Center ===="

REPO_DIR=$(git rev-parse --show-toplevel)
set -a
source $REPO_DIR/.env
set +a
ansible-playbook $REPO_DIR/ansible-playbooks/generateToken/main.yml
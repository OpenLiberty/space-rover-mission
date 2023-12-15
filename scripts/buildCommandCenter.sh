#!/bin/bash
echo "==== Build Command Center ===="

REPO_DIR=$(git rev-parse --show-toplevel)
set -a
source $REPO_DIR/.env
set +a
ansible-playbook $REPO_DIR/ansible-playbooks/commandcenter/main.yml
#!/bin/bash
echo "==== Space Rover Land ===="

REPO_DIR=$(git rev-parse --show-toplevel)
ansible-playbook $REPO_DIR/ansible-playbooks/land/main.yml

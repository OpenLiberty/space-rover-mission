#!/bin/bash

NAMESPACE=space-rover
RUN_WITH_MOCK=true

cd $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

kubectl create namespace $NAMESPACE
kubectl config set-context --current --namespace=$NAMESPACE

# Install Cert Manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

# Install Open Liberty Operator
OPERATOR_RELEASE=1.3.0
kubectl apply --server-side -f https://raw.githubusercontent.com/OpenLiberty/open-liberty-operator/main/deploy/releases/$OPERATOR_RELEASE/kubectl/openliberty-app-crd.yaml
curl -L https://raw.githubusercontent.com/OpenLiberty/open-liberty-operator/main/deploy/releases/$OPERATOR_RELEASE/kubectl/openliberty-app-operator.yaml \
    | sed -e "s/OPEN_LIBERTY_WATCH_NAMESPACE/${NAMESPACE}/" \
    | kubectl apply -n ${NAMESPACE} -f -

# Apply yaml files
kubectl delete -n ${NAMESPACE} -f ../src/rover/yaml/apps.yaml
if [[ "$RUN_WITH_MOCK" == "true" ]]; then
    kubectl delete -n ${NAMESPACE} -f ../src/rover/yaml/mock-apps.yaml
fi

kubectl apply -n ${NAMESPACE} -f ../src/rover/yaml/apps.yaml
if [[ "$RUN_WITH_MOCK" == "true" ]]; then
    kubectl apply -n ${NAMESPACE} -f ../src/rover/yaml/mock-apps.yaml
fi
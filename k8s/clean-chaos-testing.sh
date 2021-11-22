#!/bin/sh
kubectl delete -f scala-sample.yaml -n kafka
kubectl delete -f bank-account-state-topic.yaml -n kafka
kubectl delete -f bank-account-events-topic.yaml -n kafka
kubectl delete -f https://strimzi.io/examples/latest/kafka/kafka-persistent-single.yaml -n kafka 
kubectl delete -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
kubectl delete namespace kafka
#kubectl delete crd chaosblades.chaosblade.io
helm uninstall chaosblade-operator -n kube-system
minikube delete --all

#!/bin/sh
#echo "## Switching Kubectl Version to match Chasoblade"
#case "$(uname -s)" in
#
#   Darwin)
#     echo 'Mac OS X'
#     curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.21.5/bin/darwin/amd64/kubectl 
#    chmod +x ./kubectl
#    sudo mv ./kubectl /usr/local/bin/kubectl 
#
#     ;;
#
#   Linux)
#     echo 'Linux'
#     curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.21.5/bin/linux/amd64/kubectl
#     chmod +x ./kubectl
#     sudo mv ./kubectl /usr/local/bin/kubectl
#     ;;
#
#   CYGWIN*|MINGW32*|MSYS*|MINGW*)
#     echo 'MS Windows'
#     ;;
#
#    *)
#     echo 'Other OS' 
#     ;;
#esac
echo "## Starting Minikube Cluster ##"
minikube start --memory=8192 --kubernetes-version=v1.21.5
echo "## Building surge scala sample project ##"
eval $(minikube docker-env)
cd ..
sbt "project surge-scala-sample" "compile" "docker:publishLocal"
echo "## Installing Helm ##"
cd k8s
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
echo "## Installing Chaosblade ##"
helm install chaosblade-operator chaosblade-operator-1.3.0-v3.tgz --namespace kube-system
kubectl wait --for=condition=ready pod -l part-of=chaosblade -n kube-system
echo "## Installing Kafka ##"
kubectl create namespace kafka
kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
kubectl apply -f https://strimzi.io/examples/latest/kafka/kafka-persistent-single.yaml -n kafka 
kubectl wait kafka/my-cluster --for=condition=Ready --timeout=300s -n kafka 
kubectl create -f bank-account-events-topic.yaml -n kafka
kubectl create -f bank-account-state-topic.yaml -n kafka
echo "## Starting Scala-Sample App ##"
kubectl create -f scala-sample.yaml -n kafka
kubectl wait --for=condition=ready pod -l app=bank-account-scala -n kafka
SCALA_APP_URL=$(minikube service --url scala-app -n kafka)
#SCALA_APP_URL=$(kubectl get service scala-app -n kafka --output='jsonpath='http://''{.spec.clusterIP}':{.spec.ports[0].nodePort}')
export SCALA_APP_URL=$SCALA_APP_URL
echo "## Starting Chaos App ##"
kubectl apply -f chaos-case-yaml
echo "## Starting Gatling Test App ##"
cd ..
sbt "project surge-gatling-sample" "compile" "Gatling / test"
echo "## Stopping Chaos Test ##"
cd k8s
kubectl delete -f chaos-case-yaml
Chaos Testing
======================================

How to Run
-----------

1. Start Chaos Testing

   This script perfoming following steps:

   - Starting minikube cluster
   - Building surge scala sample project
   - Installing Helm & Chaosblade
   - Starting Kafka using Streamzi
   - Starting Scala-Sample App in cluster
   - Starting Chaos Test cases
   - Gatling Test using scala

   ```sh
   Execute below script from k8s folder
   $ sh start-chaos-testing.sh
   ```

2. Clean Chaos Testing

   ```sh
   Execute below script to clean chaos testing 
   $ sh clean-chaos-testing.sh
   ```

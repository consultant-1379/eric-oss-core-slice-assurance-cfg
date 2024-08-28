# Partial Integration Environment

The document provides instructions on how to setup partial integration test environment. This integration chart installs following:

- Core applications: AAS, CSAC, PMSC
- Stateful services and applications which are not mocked: EDB/Postgres, Zookeeper, Kafka, spark, Data Catalog, Schema Registry, Parser Configurator.
- Wiremock instance to mock responses from CARDQ

## Prerequisites

1. Ensure you have following helm repos added to your helm configuration. Use `helm repo add <repo-name> <repo-url>` to add repos.
   - https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm
   - https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm
   - https://arm.seli.gic.ericsson.se/artifactory/proj-river-helm-local
   - https://arm.seli.gic.ericsson.se/artifactory/proj-ec-son-drop-helm

2. Run `helm dependency build` to download all the charts locally.

3. Set following variables in your environemnt.

```
SIGNUM=signum
S_PASSWORD=password
WORK_EMAIL=email@ericsson.com
```

4. Build helm chart

```shell
helm dependency build

```


## Auto Install

Install PIT charts in one command line

```shell
./quick-install.bash -p -i -a -n <NAMESPACE>
```

`quick-install.bash` is a convience script to setup environment for you. All the step can also done by below manual steps

## Manual Steps

### Installation


2. This integration chart can be installed manually or by using `quick-install.bash` script in `dev/helmCharts/partial-integration` folder.

   a. Using script:
   ```
   ./quick-install.bash -i -n <NAMESPACE>
   ```
   b. Using manual instructions:
   ```
   NAMESPACE=swordform-signum
   kubectl create namespace $NAMESPACE
   kubectl config set-context --current --namespace=$NAMESPACE

   kubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username=${SIGNUM} --docker-password=${S_PASSWORD} --docker-email=${WORK_EMAIL}

   CFG_MOCK_DATA_PATH=${HOME}/gerrit/eric-oss-core-slice-assurance-cfg/dev/helmCharts/wiremock/mocker/data
   kubectl create configmap cfg-dep-mocker-responses-configs --from-file=$CFG_MOCK_DATA_PATH/__files
   kubectl create configmap cfg-dep-mocker-mappings-configs --from-file=$CFG_MOCK_DATA_PATH/mappings

   AUG_MOCK_DATA_PATH=${HOME}/gerrit/eric-oss-assurance-augmentation/dev/helmCharts/wiremock/mocker/data
   kubectl create configmap aas-dep-mocker-responses-configs --from-file=$AUG_MOCK_DATA_PATH/__files
   kubectl create configmap aas-dep-mocker-mappings-configs --from-file=$AUG_MOCK_DATA_PATH/mappings

   kubectl label configmap cfg-dep-mocker-responses-configs app.kubernetes.io/managed-by=Helm && kubectl annotate configmap cfg-dep-mocker-responses-configs meta.helm.sh/release-name=small && kubectl annotate configmap cfg-dep-mocker-responses-configs meta.helm.sh/release-namespace=$NAMESPACE
   kubectl label configmap cfg-dep-mocker-mappings-configs app.kubernetes.io/managed-by=Helm && kubectl annotate configmap cfg-dep-mocker-mappings-configs meta.helm.sh/release-name=small && kubectl annotate configmap cfg-dep-mocker-mappings-configs meta.helm.sh/release-namespace=$NAMESPACE
   kubectl label configmap aas-dep-mocker-responses-configs app.kubernetes.io/managed-by=Helm && kubectl annotate configmap aas-dep-mocker-responses-configs meta.helm.sh/release-name=small && kubectl annotate configmap aas-dep-mocker-responses-configs meta.helm.sh/release-namespace=$NAMESPACE
   kubectl label configmap aas-dep-mocker-mappings-configs app.kubernetes.io/managed-by=Helm && kubectl annotate configmap aas-dep-mocker-mappings-configs meta.helm.sh/release-name=small && kubectl annotate configmap aas-dep-mocker-mappings-configs meta.helm.sh/release-namespace=$NAMESPACE

   helm install small --set eric-data-message-bus-kf.configurationOverrides."auto\.create\.topics\.enable=true" .
   ```

### Post Install Steps

#### Populate data and enabled readable logs

Post install steps can be run manually or using the script.

a. Using script

   ```
   ./quick-install.bash -p -n <NAMESPACE>
   ```

b. Using manual instruction

1. Populdate DC data before installing configurator:
   Run following command after eric-oss-data-catalog pod come up. Replace <NAMESPACE> with correct namespace.

```
kubectl exec -it edb-0 -c edb -- curl -X POST "http://eric-oss-data-catalog:9590/catalog/v1/message-bus" -H "Content-Type:application/json" -d "
{\"name\":\"eric-oss-dmm-kf\",\"clusterName\":\"haber020\",\"nameSpace\":\"<NAMESPACE>\",\"accessEndpoints\":[\"eric-oss-dmm-kf:9092\"]}"
```

2. Enable configurator to populate schemas

```
kubectl scale deployment eric-oss-stats-parser-configurator --replicas=1
```

3. Enable readable log
```
kubectl get deployments eric-oss-assurance-augmentation -o yaml | sed -E 's/classpath:logback-json.xml//' | kubectl replace -f -
kubectl get deployments eric-oss-core-slice-assurance-cfg -o yaml | sed -E 's/classpath:logback-json.xml//' | kubectl replace -f -
```

### Start CSAC and AAS

After PMSC pod comes up, start AAS and CSAC.

```
kubectl scale deployment eric-oss-assurance-augmentation --replicas=1
kubectl scale deployment eric-oss-core-slice-assurance-cfg --replicas=1
```

or
```shell
./quick-install -a

```

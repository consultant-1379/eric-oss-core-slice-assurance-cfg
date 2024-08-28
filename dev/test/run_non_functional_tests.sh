#!/bin/bash
#
# COPYRIGHT Ericsson 2023
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

DEPENDENCY_BUILD_FLAG=0 #This flag avoids building integration chart dependencies everytime csac dependencies are installed.

#Sets service specific variables which will be used in master script.
set_service_variables () {
  SERVICE_NAME="CSAC" #Used in log statements in master script.
  SERVICE_HELM_CHART_NAME="csac" #Used as helm chart name in service script and master script.
  DEPLOYMENT_NAME="eric-oss-core-slice-assurance-cfg" #Used in various commands in master script like 1.deployment scaling, 2.patch, 3.pulling docker image, 4.finding csac pod name.
  POD_SELECTOR="app.kubernetes.io/name=eric-oss-core-slice-assurance-cfg,app.kubernetes.io/instance=csac" #Used in master script to find csac pod.
  DOCKER_RESPOSITORY="armdocker.rnd.ericsson.se/proj-eric-oss-ci-internal" #Used in master script to load csac docker image
  HELM_REPOSITORY="https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-ci-internal-helm-local/eric-oss-core-slice-assurance-cfg/"
  POD_READY_TIMEOUT_SEC=120 #This should be equal to the initialDelaySeconds configured for any service. This is used to check if the POD comes to Running state within this period.
  SCRIPT_NAME="run_non_functional_tests" #This string is used in master script for help text.
}

#Install all secrets required for CSAC deployment
create_secrets () {
  echo "Creating secrets..."

  #create secret to pull docker images
  kubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username=$SIGNUM --docker-password=$ENCRYPTED_PASSWORD --docker-email=$EMAIL --namespace=$NAMESPACE

  #create secret for DDB
  kubectl create secret generic eric-oss-core-slice-assurance-cfg-db-secret --from-literal=pguserid=csac --from-literal=pgpasswd=custompwd --from-literal=super-pwd=superpwd --from-literal=super-user=postgres --from-literal=metrics-pwd=metricspwd --from-literal=replica-user=replicauser --from-literal=replica-pwd=replicapwd --namespace=$NAMESPACE
}

#Installs all dependent services required for CSAC
install_dependent_services () {

  if [[ $DEPENDENCY_BUILD_FLAG -eq 0 ]]
  then
    helm dependency build ../helmCharts/csac-dependencies
    DEPENDENCY_BUILD_FLAG=1
  fi

  helm install csac-dependencies ../helmCharts/csac-dependencies --namespace $NAMESPACE
}

#Installs CSAC service
install_service () {

  helm install $SERVICE_HELM_CHART_NAME "$HELM_REPOSITORY/$DEPLOYMENT_NAME-$INSTALL_CHART_VERSION.tgz" --set global.pullSecret=k8s-registry,validation.external.schemaRegistry.url="http://wiremock:8080",validation.external.dataCatalog.url="http://wiremock:8080",provisioning.pmsc.enabled=true,provisioning.aas.enabled=true,provisioning.index.enabled=true,provisioning.pmsc.url="http://wiremock:8080",provisioning.aas.url="http://wiremock:8080",provisioning.index.url="http://wiremock:8080",provisioning.aas.ardq.cardq="http://wiremock:8080",global.security.tls.enabled=false,log.streamingMethod=indirect --namespace $NAMESPACE --username=$SIGNUM --password=$ENCRYPTED_PASSWORD
}

#Upgrade CSAC service
upgrade_service () {

  helm upgrade $SERVICE_HELM_CHART_NAME "$HELM_REPOSITORY/$DEPLOYMENT_NAME-$UPGRADE_CHART_VERSION.tgz" --set global.pullSecret=k8s-registry,validation.external.schemaRegistry.url="http://wiremock:8080",validation.external.dataCatalog.url="http://wiremock:8080",provisioning.pmsc.enabled=true,provisioning.aas.enabled=true,provisioning.index.enabled=true,provisioning.pmsc.url="http://wiremock:8080",provisioning.aas.url="http://wiremock:8080",provisioning.index.url="http://wiremock:8080",provisioning.aas.ardq.cardq="http://wiremock:8080",global.security.tls.enabled=false,log.streamingMethod=indirect --namespace $NAMESPACE --username=$SIGNUM --password=$ENCRYPTED_PASSWORD
}

#Uninstall CSAC and all dependent services
uninstall_services () {
  echo "Uninstalling services..."
  helm uninstall $SERVICE_HELM_CHART_NAME csac-dependencies --namespace $NAMESPACE
}

#Delete PVC which will delete PVs
delete_pvc () {
  echo "Deleting PVCs..."
  kubectl delete pvc pg-data-eric-oss-core-slice-assurance-cfg-db-0 pg-data-eric-oss-core-slice-assurance-cfg-db-1 --namespace=$NAMESPACE
}

#Delete secrets
delete_secrets () {
  echo "Deleting secrets..."
  kubectl delete secret k8s-registry eric-oss-core-slice-assurance-cfg-db-secret --namespace=$NAMESPACE
}

#Checks if CSAC is up and running by checking logs.
#Returns 100 for successful new install
#Returns 101 for successful re-install. This code identifies upgraded pod/new pod created after deleting existing pod/scaled out pod/pod moved to new node.
#The test conditions checks for this code 101 to ensure the pod did not attempt to perform actions specific to new install (re-provisioning resources.)
#Returns 1 for failed CSAC deployment.
wait_for_service_to_run () {
  local POD_NAME
  local DEPLOYMENT="fail"
  local RETURN_CODE
  local DEPLOYMENT_START_TIME=$SECONDS
  local DEPLOYMENT_TIME_DIFF=0

  #If pod name is not passed as argument get pod name else use the pod name provided as argument.
  if [[ $# -eq 0 ]]; then
    POD_NAME=$(kubectl get pod -l "$POD_SELECTOR" -o jsonpath="{.items[0].metadata.name}" --namespace=$NAMESPACE)
  else
    POD_NAME=$1
  fi

  echo "Checking status of pod: $POD_NAME"

  #Checks logs for successful provisioning or no resources change text.
  #While loop runs until text found or timeout(3min)
  while [[ $DEPLOYMENT == "fail" ]] && [[ $DEPLOYMENT_TIME_DIFF -le 180 ]]
  do
    sleep 10

    kubectl logs $POD_NAME --namespace=$NAMESPACE | fgrep "AAS provisioning completed successfully."
    AAS_PROVISIONING_STATUS_CODE="$?"
    kubectl logs $POD_NAME --namespace=$NAMESPACE | fgrep "KPI submission provisioning successful"
    KPI_PROVISIONING_STATUS_CODE="$?"
    kubectl logs $POD_NAME --namespace=$NAMESPACE | fgrep "AIS response : 200 - OK"
    AIS_PROVISIONING_STATUS_CODE="$?"
    if [[ $AAS_PROVISIONING_STATUS_CODE -eq 0 ]] && [[ $KPI_PROVISIONING_STATUS_CODE -eq 0 ]] && [[ $AIS_PROVISIONING_STATUS_CODE -eq 0 ]]; then
      echo "CSAC install deployment is successful"
      echo "CSAC is up and running."
      DEPLOYMENT="success"
      RETURN_CODE=$NEW_INSTALL
      break
    fi

    kubectl logs $POD_NAME --namespace=$NAMESPACE | fgrep "No runtime resource changes detected.  Skipping provisioning."
    RESTART_STATUS_CODE="$?"
    
    if [[ $RESTART_STATUS_CODE -eq 0 ]]; then
      echo "CSAC upgrade deployment is successful"
      echo "CSAC is up and running."
      DEPLOYMENT="success"
      RETURN_CODE=$RE_INSTALL
      break
    fi

    echo "Waiting for CSAC to start..."

    DEPLOYMENT_TIME_DIFF=$(($SECONDS - $DEPLOYMENT_START_TIME))
  done

  if [[ $DEPLOYMENT == "fail" ]]; then
    echo "CSAC deployment failed."
    return 1
  fi

  #Waits for the pod to be ready and running
  is_service_ready
  local IS_READY="$?"

  if [[ $IS_READY -eq 1 ]]; then
    echo "CSAC deployment failed."
    return 1
  fi

  return $RETURN_CODE
}

# Each service have different liveness and readiness criteria. So this is service specific code.
liveness_readiness_test () {
  local POD_NAME=$(get_pod_name)
  wait_for_service_to_run $POD_NAME
  local SERVICE_STATUS="$?"

  kubectl describe pod $POD_NAME --namespace=$NAMESPACE | fgrep "Liveness:   tcp-socket"
  local IS_LIVENESS_CONFIGURED="$?"
  kubectl describe pod $POD_NAME --namespace=$NAMESPACE | fgrep "Readiness:  tcp-socket"
  local IS_READINESS_CONFIGURED="$?"

  local LIVENESS_READINESS_RESULT
  if [[ $IS_LIVENESS_CONFIGURED -eq 0 ]] && [[ $IS_READINESS_CONFIGURED -eq 0 ]] && [[ $SERVICE_STATUS -ne 1 ]]
  then
    LIVENESS_READINESS_RESULT="PASSED"
    echo "Successful initialization of CSAC Springboot Application Context is considered as the criteria for Liveness and Readiness for CSAC deployment."
  else
    LIVENESS_READINESS_RESULT="FAILED"
  fi

  echo "*************************************"
  echo "   LIVENESS and READINESS $LIVENESS_READINESS_RESULT     "
  echo "*************************************"
  echo "liveness_readiness: $LIVENESS_READINESS_RESULT"  | tee -a $RESULTS_FILE
}

#Condition of this case is service specific.
#CSAC check if a DB connection retry is attempted.
restart_case2_condition_for_result () {

  #Search for "Retrying in 1 sec..." in csac logs to ensure csac is retrying from DB connection.
  local POD_NAME=$(get_pod_name)
  kubectl logs $POD_NAME --namespace=$NAMESPACE | fgrep "Retrying in 1 sec..."
  CSAC_WAITING_FOR_DB="$?"

  return $CSAC_WAITING_FOR_DB
}

get_service_metrics () {
  #port-forward 8080 port to CSAC pod to get metrics from actuator endpoint
  local POD_NAME=$(get_pod_name)
  echo "port-forwarding 8080 port..."
  kubectl port-forward $POD_NAME --namespace=$NAMESPACE 8080:8080 &
  local PID="$!"
  sleep 20 #Needed this delay because sometimes the curl command is attempted before the port-forwarding and causing failures.
  local METRICS=$(curl http://localhost:8080/actuator/prometheus)


  echo "*********** $SERVICE_NAME startup time**********"
  echo "$METRICS" | grep "csac_application_ready_time_seconds{" | sed -e 's/{.*}//' | awk '{print $1": " $2}' | tee -a $RESULTS_FILE
  echo
  echo "*********** $SERVICE_NAME Metrics *********"
  echo "$METRICS" | grep "csac_custom_metric" | sed -e 's/{.*}//' | awk '{print $1": " $2}' | tee -a $RESULTS_FILE
  kill $PID
}

source common_functions.sh

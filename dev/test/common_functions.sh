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

SECONDS=0
NEW_INSTALL=100
RE_INSTALL=101
FAIL=1
GRACE_PERIOD=30
WAIT_TIME_TO_START_DEPENDENCIES_FOR_RESTART_TEST2=30
FILE_DATE_SUFFIX=`date +%Y.%m.%d.%H.%M.%S`


# Define variables
SIGNUM=${SIGNUM:-}
ENCRYPTED_PASSWORD=${ENCRYPTED_PASSWORD:-}
EMAIL=${EMAIL:-}
REUSE_NAMESPACE=${REUSE_NAMESPACE:-}
NAMESPACE=${NAMESPACE:-}
INSTALL_CHART_VERSION=${INSTALL_CHART_VERSION:-}
DELETE_NS=${DELETE_NS:-}
UPGRADE_CHART_VERSION=${UPGRADE_CHART_VERSION:-}
DOCKER_VERSION=${DOCKER_VERSION:-}

create_log_file() {
  local LOG_FILE="./log_$FILE_DATE_SUFFIX.log"
  exec 1> >(tee $LOG_FILE) 2>&1
}

create_results_file() {
  echo "Creating results file..."
  RESULTS_FILE="./results_$FILE_DATE_SUFFIX.txt"
  touch $RESULTS_FILE
}

print_var() {
  echo "Printing user input..."
  echo "SIGNUM: $SIGNUM"
  echo "EMAIL: $EMAIL"
  echo "REUSE_NAMESPACE: $REUSE_NAMESPACE"
  echo "NAMESPACE: $NAMESPACE"
  echo "INSTALL_CHART_VERSION: $INSTALL_CHART_VERSION"
  echo "DELETE_NS: $DELETE_NS"
  echo "UPGRADE_CHART_VERSION: $UPGRADE_CHART_VERSION"
  echo "DOCKER_VERSION: $DOCKER_VERSION"
}

create_namespace () {
  if [[ "$REUSE_NAMESPACE" == "y" ]] || [[ "$REUSE_NAMESPACE" == "Y" ]]
  then
    echo "Using existing namespace $NAMESPACE"
  else
    echo "Creating namespace $NAMESPACE..."
    kubectl create namespace $NAMESPACE
  fi
}

setup () {
  echo "Installing $SERVICE_NAME... "

  create_namespace
  create_secrets
  install_dependent_services

  # Wait for all the dependencies to start
  sleep 60

  install_service
}


teardown () {
  uninstall_services
  delete_pvc
  delete_secrets
}

delete_namespace() {
  #Delete namespace based on input
  if [[ "$DELETE_NS" == "y" ]] || [[ "$DELETE_NS" == "Y" ]]
  then
    echo "Deleting namespace $NAMESPACE..."
    kubectl delete ns $NAMESPACE
  fi
}

#Checks for pod ready status to be 'true' to ensure pod is ready and running

is_service_ready () {
  local IS_READY_START_TIME=$SECONDS
  local IS_READY_TIME_DIFF=0
  local IS_READY="false"

  #While loop runs until pod is Ready or timeout
  while [[ $IS_READY == "false" ]] && [[ $IS_READY_TIME_DIFF -le $POD_READY_TIMEOUT_SEC ]]
  do
    sleep 10
    IS_READY=$(kubectl get pod $POD_NAME -o jsonpath='{.status.containerStatuses[0].ready}' --namespace=$NAMESPACE)
    echo "ready status: $IS_READY"
    if [[ $IS_READY == "true" ]]; then
      echo "$SERVICE_NAME pod is ready"
      return 0
    fi

    echo "Waiting for $SERVICE_NAME to be ready..."
    IS_READY_TIME_DIFF=$(($SECONDS - $IS_READY_START_TIME))
    echo "time diff : $IS_READY_TIME_DIFF"
  done

  return 1

}

scale_in () {
  echo "*************************************"
  echo "            scale-in                 "
  echo "*************************************"

  kubectl scale deployment $DEPLOYMENT_NAME --replicas=1 --namespace=$NAMESPACE

  #Wait for second pod to terminate
  sleep 60

  wait_for_service_to_run
  local SERVICE_INSTALL_STATUS="$?"
  local RESULT

  if [[ $SERVICE_INSTALL_STATUS -ne $FAIL ]]
  then
    RESULT="PASSED"
  else
    RESULT="FAILED"
  fi

  echo "*************************************"
  echo "       scale-in test $RESULT         "
  echo "*************************************"
  echo "scale_in: $RESULT" | tee -a $RESULTS_FILE

}

scale_out () {
  echo "*************************************"
  echo "            scale-out                "
  echo "*************************************"

  setup

  local POD_BEFORE_SCALE_OUT=$(get_pod_name)
  wait_for_service_to_run $POD_BEFORE_SCALE_OUT

  kubectl scale deployment $DEPLOYMENT_NAME --replicas=2 --namespace=$NAMESPACE


  local POD1_AFTER_SCALE_OUT=$(get_pod_name)
  local POD2_AFTER_SCALE_OUT=$(get_second_pod_name)

  wait_for_service_to_run $POD1_AFTER_SCALE_OUT
  local POD1_INSTALL_TYPE="$?"
  echo "Pod1 type: $POD1_INSTALL_TYPE"

  wait_for_service_to_run $POD2_AFTER_SCALE_OUT
  local POD2_INSTALL_TYPE="$?"
  echo "Pod2 type: $POD2_INSTALL_TYPE"

  if [[ ($POD1_INSTALL_TYPE -eq $NEW_INSTALL && $POD2_INSTALL_TYPE -eq $RE_INSTALL) || ($POD2_INSTALL_TYPE -eq $NEW_INSTALL && $POD1_INSTALL_TYPE -eq $RE_INSTALL) ]]
  then
    echo "*************************************"
    echo "       scale-out test PASSED         "
    echo "*************************************"
    echo "scale_out: PASSED" | tee -a $RESULTS_FILE
  else
    echo "*************************************"
    echo "       scale-out test FAILED         "
    echo "*************************************"
    echo "scale-in test SKIPPED"
    echo "Scale-in test is dependent on scale-out test setup. Since scale-out test failed, skipping scale-in test..."
    echo "Fix scale-out issue to run scale-in test..."
    echo "scale_out: FAILED" | tee -a $RESULTS_FILE
    echo "scale_in: SKIPPED" | tee -a $RESULTS_FILE
    teardown
    return
  fi

  scale_in
  teardown
}

sigterm_n_liveness_readiness () {
  echo "*************************************"
  echo "        SIGTERM and SIGKILL         "
  echo "*************************************"

  setup
  wait_for_service_to_run
  local SERVICE_INSTALL_STATUS_BEFORE_DELETE="$?"

  local POD_NAME_BEFORE_DELETE=$(get_pod_name)

  # Use this deployment to check liveness and readiness
  liveness_readiness_test

  kubectl delete pod $POD_NAME_BEFORE_DELETE --namespace=$NAMESPACE

  sleep $GRACE_PERIOD

  local POD_NAME_AFTER_DELETE=$(get_pod_name)

  wait_for_service_to_run
  local POD_INSTALL_TYPE="$?"

  local SIGTERM_RESULT
  if [[ $POD_NAME_BEFORE_DELETE != $POD_NAME_AFTER_DELETE ]] && [[ $POD_INSTALL_TYPE -eq $RE_INSTALL ]]
  then
    SIGTERM_RESULT="PASSED"
    echo "$SERVICE_NAME pod terminated and restarted successfully."
  else
    SIGTERM_RESULT="FAILED"
    echo "$SERVICE_NAME pod terminated but did not restart successfully."
  fi

  echo "*************************************"
  echo "     SIGTERM and SIGKILL $SIGTERM_RESULT    "
  echo "*************************************"
  echo "sigterm_sigkill: $SIGTERM_RESULT"  | tee -a $RESULTS_FILE

  teardown
}

move_btw_workers () {
  echo "*************************************"
  echo "        Move between workers         "
  echo "*************************************"

  setup
  wait_for_service_to_run

  local POD_NAME_BEFORE_MOVE=$(get_pod_name)
  local POD_NODE_BEFORE_MOVE=$(kubectl get pod $POD_NAME_BEFORE_MOVE -o jsonpath='{.spec.nodeName}' --namespace=$NAMESPACE)

  local TARGET_NODE=$(kubectl get node -o "jsonpath={.items[0].metadata.name}" --namespace=$NAMESPACE)

  if [[ $TARGET_NODE == $POD_NODE_BEFORE_MOVE ]]
  then
    TARGET_NODE=$(kubectl get node -o "jsonpath={.items[1].metadata.name}" --namespace=$NAMESPACE)
  fi

  echo "Moving $SERVICE_NAME pod from worker node $POD_NODE_BEFORE_MOVE to $TARGET_NODE"

  local PATCH_STRING='{"spec": {"template": {"spec": {"nodeSelector": {"kubernetes.io/hostname":"target_node"}}}}}'
  local PATCH_COMMAND=$(echo kubectl patch deployment $DEPLOYMENT_NAME --namespace=$NAMESPACE --patch \'$(echo ${PATCH_STRING/target_node/$TARGET_NODE})\')
  eval $PATCH_COMMAND

  echo "Moved $SERVICE_NAME pod from worker node $POD_NODE_BEFORE_MOVE to $TARGET_NODE"

  local POD1=$(get_pod_name)
  local POD2=$(get_second_pod_name)

  if [[ $POD1 == $POD_NAME_BEFORE_MOVE ]]
  then
    POD_NAME_AFTER_MOVE=$POD2
  else
    POD_NAME_AFTER_MOVE=$POD1
  fi

  wait_for_service_to_run $POD_NAME_AFTER_MOVE
  local POD_INSTALL_TYPE="$?"
  echo "Install type: $POD_INSTALL_TYPE"

  local POD_NODE_AFTER_MOVE=$(kubectl get pod $POD_NAME_AFTER_MOVE -o jsonpath='{.spec.nodeName}' --namespace=$NAMESPACE)

  local RESULT
  if [[ $TARGET_NODE == $POD_NODE_AFTER_MOVE ]] && [[ $POD_INSTALL_TYPE -eq $RE_INSTALL ]]
  then
    echo "$SERVICE_NAME pod successfully moved between worker nodes: $POD_NODE_BEFORE_MOVE to $TARGET_NODE"
    RESULT="PASSED"
  else
    echo "$SERVICE_NAME pod failed to move between worker nodes: $POD_NODE_BEFORE_MOVE to $TARGET_NODE"
    RESULT="FAILED"
  fi

  echo "*************************************"
  echo "     Move between workers $RESULT     "
  echo "*************************************"
  echo "move_btw_workers: $RESULT" | tee -a $RESULTS_FILE

  teardown
}

restart_case1 () {
  echo "All instances of $SERVICE_NAME service restart simultaneously -> service should come back again without any problem."
  echo "*************************************"
  echo "           Restart case1             "
  echo "*************************************"

  #Install service with dependencies and scale out to 2 replicas for this test and wait for both the pods to start.
  setup
  local POD_BEFORE_SCALE_OUT=$(get_pod_name)
  wait_for_service_to_run $POD_BEFORE_SCALE_OUT

  kubectl scale deployment $DEPLOYMENT_NAME --replicas=2 --namespace=$NAMESPACE

  local POD1_AFTER_SCALE_OUT=$(get_pod_name)
  local POD2_AFTER_SCALE_OUT=$(get_second_pod_name)
  wait_for_service_to_run $POD1_AFTER_SCALE_OUT
  wait_for_service_to_run $POD2_AFTER_SCALE_OUT

  #Restart pods by deleting them.
  kubectl delete pod $POD1_AFTER_SCALE_OUT $POD2_AFTER_SCALE_OUT --namespace=$NAMESPACE
  sleep $GRACE_PERIOD #This wait is required to ensure the old pods are deleted and we fetch new pod names in next step.

  local RESTARTED_POD1=$(get_pod_name)
  local RESTARTED_POD2=$(get_second_pod_name)

  wait_for_service_to_run $RESTARTED_POD1
  local POD1_INSTALL_TYPE="$?"
  echo "Pod1 type: $POD1_INSTALL_TYPE"

  wait_for_service_to_run $RESTARTED_POD2
  local POD2_INSTALL_TYPE="$?"
  echo "Pod2 type: $POD2_INSTALL_TYPE"

  local RESULT
  #Both the pods should be up and running and both should not provision the resources again.
  if [[ $POD1_INSTALL_TYPE -eq $RE_INSTALL && $POD2_INSTALL_TYPE -eq $RE_INSTALL ]]
  then
    echo "All instances of $SERVICE_NAME service restart simultaneously -> service comes back again without any problem."
    RESULT="PASSED"
  else
    echo "All instances of $SERVICE_NAME service restart simultaneously -> service does not come back properly."
    RESULT="FAILED"
  fi

  echo "*************************************"
  echo "        Restart case1 $RESULT         "
  echo "*************************************"
  echo "restart_case1: $RESULT" | tee -a $RESULTS_FILE

  teardown
}

restart_case2 () {
  echo "Dependent services started 30sec after $SERVICE_NAME installed. $SERVICE_NAME should wait for dependent services and should come up and running successfully."
  echo "*************************************"
  echo "           Restart case2             "
  echo "*************************************"

  create_secrets
  install_service

  #Wait 30sec to start dependencies
  sleep $WAIT_TIME_TO_START_DEPENDENCIES_FOR_RESTART_TEST2

  install_dependent_services

  wait_for_service_to_run
  local INSTALL_TYPE="$?"
  echo "Install Type: $INSTALL_TYPE"

  restart_case2_condition_for_result
  local CONDITION_RESULT="$?"

  local RESULT
  if [[ $INSTALL_TYPE -eq $NEW_INSTALL && $CONDITION_RESULT -eq 0 ]]
  then
    echo "Dependent services started $WAIT_TIME_TO_START_DEPENDENCIES_FOR_RESTART_TEST2 sec after $SERVICE_NAME installed. $SERVICE_NAME waited for dependencies and came up and running successfully after DDB started."
    RESULT="PASSED"
  else
    echo "Dependent services started $WAIT_TIME_TO_START_DEPENDENCIES_FOR_RESTART_TEST2 sec after $SERVICE_NAME installed. $SERVICE_NAME failed to come up successfully."
    RESULT="FAILED"
  fi

  echo "*************************************"
  echo "       Restart case2 $RESULT          "
  echo "*************************************"
  echo "restart-case2: $RESULT" | tee -a $RESULTS_FILE

  teardown
}

metrics_and_resources () {
  echo "**************************************************"
  echo "  Calculating $SERVICE_NAME metrics and resources "
  echo "**************************************************"
  setup
  wait_for_service_to_run
  local INSTALL_STATUS="$?"

  if [[ $INSTALL_STATUS -eq 0 ]]
  then
    echo "$SERVICE_NAME installation failed."
    echo "*********************************************************"
    echo " Calculating $SERVICE_NAME metrics and resources FAILED "
    echo "*********************************************************"

    teardown
    return
  fi

  get_service_metrics

  #Memory and CPU usage
  local MEMORY_N_CPU=$(kubectl top pod -l $POD_SELECTOR --namespace=$NAMESPACE)
  local MEMORY=$(echo $MEMORY_N_CPU | awk '{print $NF}')
  local CPU=$(echo $MEMORY_N_CPU | awk '{print $(NF-1)}')

  echo "*******Microservice memory and CPU footprint********"
  echo
  echo "memory: $MEMORY" | tee -a $RESULTS_FILE
  echo "cpu: $CPU" | tee -a $RESULTS_FILE
  echo

  #Docker image size
  podman login -u $SIGNUM -p $ENCRYPTED_PASSWORD $DOCKER_RESPOSITORY
  podman pull $DOCKER_RESPOSITORY/$DEPLOYMENT_NAME:$DOCKER_VERSION
  sleep 30 # wait for the docker image to be pulled
  local IMAGE_SIZE=$(podman image ls | grep "$DOCKER_VERSION" | awk '{print $(NF-1) $NF}')

  echo "*******Docker Image Size and details********"
  echo
  echo "image_size: $IMAGE_SIZE" | tee -a $RESULTS_FILE
  echo

 teardown
}

restart_upgrade_rollback_time () {
  #New Install
  setup
  wait_for_service_to_run
  local NEW_INSTALL_POD=$(get_pod_name)

  # Restart
  RESTART_START_TIME=$SECONDS
  echo "restart start time : $RESTART_START_TIME"
  kubectl delete pod $NEW_INSTALL_POD --namespace=$NAMESPACE

  local POD1=$(get_pod_name)
  local POD2=$(get_second_pod_name)

  #`kubectl get pods` display both old pod and new pod as running. This condition ensure we check restarted pod.
  if [[ $POD1 == $NEW_INSTALL_POD ]]
  then
    RESTARTED_POD=$POD2
  else
    RESTARTED_POD=$POD1
  fi

  wait_for_service_to_run $RESTARTED_POD
  RESTART_END_TIME=$SECONDS
  echo "restart end time : $RESTART_END_TIME"

  local RESTART_TIME=$(($RESTART_END_TIME-$RESTART_START_TIME))
  echo "restart_time: $RESTART_TIME" | tee -a $RESULTS_FILE
  echo "*******$SERVICE_NAME restart time********"
  echo
  echo $RESTART_TIME
  echo

  #Upgrade
  UPGRADE_START_TIME=$SECONDS
  echo "upgrade start time : $UPGRADE_START_TIME"

  upgrade_service

  local POD3=$(get_pod_name)
  local POD4=$(get_second_pod_name)

  #`kubectl get pods` display both old pod and new pod as running. This condition ensure we check upgrade pod.
  if [[ $POD3 == $RESTARTED_POD ]]
  then
    UPGRADED_POD=$POD4
  else
    UPGRADED_POD=$POD3
  fi

  wait_for_service_to_run $UPGRADED_POD
  UPGRADE_END_TIME=$SECONDS
  echo "upgrade end time : $UPGRADE_END_TIME"

  local UPGRADE_TIME=$(($UPGRADE_END_TIME-$UPGRADE_START_TIME))

  echo "upgrade_time: $UPGRADE_TIME" | tee -a $RESULTS_FILE
  echo "*******$SERVICE_NAME upgrade time********"
  echo
  echo  $UPGRADE_TIME
  echo

  #Rollback
  ROLLBACK_START_TIME=$SECONDS
  echo "rollback start time : $ROLLBACK_START_TIME"
  helm rollback $SERVICE_HELM_CHART_NAME 1 --namespace $NAMESPACE

  local POD5=$(get_pod_name)
  local POD6=$(get_second_pod_name)

  #`kubectl get pods` display both old pod and new pod as running. This condition ensure we check rolledback pod.
  if [[ $POD5 == $UPGRADED_POD ]]
  then
    ROLLBACK_POD=$POD6
  else
    ROLLBACK_POD=$POD5
  fi

  wait_for_service_to_run $ROLLBACK_POD
  ROLLBACK_END_TIME=$SECONDS
  echo "rollback end time : $ROLLBACK_END_TIME"

  local ROLLBACK_TIME=$(($ROLLBACK_END_TIME-$ROLLBACK_START_TIME))

  echo "rollback_time: $ROLLBACK_TIME" | tee -a $RESULTS_FILE
  echo -e "*******$SERVICE_NAME rollback time********"
  echo -e $ROLLBACK_TIME

  teardown
}

#Gets service pod name. If there are multiple instances(pods) of a service then this function returns first pod in 'kubectl get pods' response
get_pod_name () {
  local POD_NAME=$(kubectl get pods --namespace $NAMESPACE -l $POD_SELECTOR -o name | grep "pod/$DEPLOYMENT_NAME.*" | sed -n 1p | cut -c 5-)
  echo $POD_NAME
}

#If there are multiple instances(pods) of a service then this function returns second pod in 'kubectl get pods' response
get_second_pod_name () {
  local POD_NAME=$(kubectl get pods --namespace $NAMESPACE -l $POD_SELECTOR -o name | grep "pod/$DEPLOYMENT_NAME.*" | sed -n 2p | cut -c 5-)
  echo $POD_NAME
}

run_non_functional_tests () {
  # Create file with timestamp
  print_var
  create_results_file

  echo "Starting non_functional tests..."

  echo "Starting Characteristic tests..."
  echo "characteristics: " | tee -a $RESULTS_FILE
  metrics_and_resources
  restart_upgrade_rollback_time

  echo "Starting scale-out and scale-in tests..."
  echo "deployment:" | tee -a $RESULTS_FILE
  scale_out

  echo "Starting robustness tests..."
  echo "robustness:" | tee -a $RESULTS_FILE
  sigterm_n_liveness_readiness
  move_btw_workers
  restart_case1
  restart_case2

}

# Function to prompt for user input if variable is not set
prompt_input() {
  local var_name=$1
  local prompt_message=$2
  if [[ -z "${!var_name}" ]]
  then
    echo -n "$prompt_message"
    read -r "${var_name?}"
  fi
}

# Function to handle installation
handle_install() {
  prompt_input SIGNUM "Enter SIGNUM: "
  [[ -z "${!var_name}" ]] && ENCRYPTED_PASSWORD=$(curl -s -u "$SIGNUM"  https://arm.seli.gic.ericsson.se/artifactory/api/security/encryptedPassword)
  prompt_input EMAIL "Enter email: "
  prompt_input REUSE_NAMESPACE "Do you want to use existing namespace (y/n): "
  prompt_input NAMESPACE "Enter namespace: "
  prompt_input INSTALL_CHART_VERSION "Enter $SERVICE_NAME helm chart version: "
}

# Function to handle uninstallation
handle_uninstall() {
  prompt_input NAMESPACE "Enter namespace: "
  prompt_input DELETE_NS "Do you want to delete namespace (y/n): "
  teardown
  delete_namespace
}

# Function to handle running tests
handle_run_tests() {
  handle_install
  prompt_input UPGRADE_CHART_VERSION "Enter $SERVICE_NAME helm chart version for upgrade: "
  prompt_input DOCKER_VERSION "Docker image tag: "
  prompt_input DELETE_NS "Do you want to delete namespace after tests executed (y/n): "
  run_non_functional_tests
  delete_namespace
}

# Function to display help information
display_help() {
  cat <<-EOF
**************************************************************************************
$SCRIPT_NAME is a utility to setup an ESOA service on k8s cluster and run manual tests.
**************************************************************************************
Usage: $SCRIPT_NAME [-i] [-u] [-r] [-h]

Options:
  -i : Installs $SERVICE_NAME and its dependencies
  -u : Uninstalls $SERVICE_NAME and its dependencies
  -r : Installs $SERVICE_NAME with dependencies and runs manual tests
  -h : Displays help information

EOF
}

# Main function to handle script options
main() {
  set_service_variables
  create_log_file

  while getopts 'iurh' OPTION; do
    case "$OPTION" in
      i) handle_install && setup ;;
      u) handle_uninstall ;;
      r) handle_run_tests ;;
      h) display_help; exit 0 ;;
      ?) display_help; exit 1 ;;
    esac
  done

  [[ $OPTION == 'i' || $OPTION == 'u' || $OPTION == 'r' ]] && echo "***Script took $SECONDS seconds to complete.***"
}

# Run the main function
main "$@"

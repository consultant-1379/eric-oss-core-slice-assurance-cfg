#!/bin/bash
#
# COPYRIGHT Ericsson 2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

# ------------------------------------------------------------------
# [Yongqinchuan] Bash script template
#                Description
# ------------------------------------------------------------------
set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

# Section: Utility FUNCTIONS {{{1
# --------------------------------------------------------------------------

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

setup_colors() {
  if [[ -t 2 ]] && [[ -z "${NO_COLOR-}" ]] && [[ "${TERM-}" != "dumb" ]]; then
    NOFORMAT='\033[0m' RED='\033[0;31m' GREEN='\033[0;32m' ORANGE='\033[0;33m' BLUE='\033[0;34m' PURPLE='\033[0;35m' CYAN='\033[0;36m' YELLOW='\033[1;33m'
  else
    NOFORMAT='' RED='' GREEN='' ORANGE='' BLUE='' PURPLE='' CYAN='' YELLOW=''
  fi
}

confirm() {
    echo "Continue? y or n? "
    read -r REPLY
    case $REPLY in
    [Yy]) echo 'Continue' ;;
    [Nn]) break && exit ;;
    *) confirm ;;
    esac
}

msg(){ echo -e >&2 "${1-}${NOFORMAT}"; }

msgt() { echo -e >&2 "[$(date +"%H:%M:%S")]${1-}${NOFORMAT}"; }

die() {
  local msg=$1
  local code=${2-1} # default exit status 1
  msg "$msg"
  exit "$code"
}

# Section: Usage and Parser {{{1
# --------------------------------------------------------------------------

usage() {
  cat << EOF
Usage: $(basename "${BASH_SOURCE[0]}") [-i] [-p] [-a] -n namespace

A quick script to setup PIT environment

Available options:

-h, --help        Print this help and exit
-n, --namespace   Specify the namespace
-i, --install     Install PIT chart
-p, --postconfig  Trigger post config after install
-a, --application Enable CSAC and AAS after install
EOF
  exit
}

CURRENT_NS=$(kubectl config view --minify --output 'jsonpath={..namespace}' 2> /dev/null)
TARGET_NS=''

parse_params() {
  # default values of variables set from params
  postconfig=0
  install=0
  application=0
  param=''

  while :; do
    case "${1-}" in

    -h | --help) usage ;;
    --no-color) NO_COLOR=1 ;;
    -p | --postconfig) postconfig=1 ;;
    -i | --install) install=1 ;;
    -a | --application) application=1 ;;
    -n | --namespace)
      TARGET_NS="${2-}"
      shift
      ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")

  return 0
}

setup_colors
parse_params "$@"


if [[ "$TARGET_NS" == "" ]]; then
  msg "${GREEN}Default target namespace to ${CURRENT_NS}\n"
  TARGET_NS="$CURRENT_NS"
fi


# Section: Main Script logic {{{1
# --------------------------------------------------------------------------

if [[ $install == "1" ]]; then

  msg "${BLUE}Executing commands:"

  if [[ "$CURRENT_NS" != "$TARGET_NS" ]]; then
    msg "\nkubectl create namespace ${TARGET_NS}"
    kubectl create namespace "${TARGET_NS}"
  fi

  msg "\nkubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username=${SIGNUM} --docker-password="***********" --docker-email=${WORK_EMAIL} --namespace=${TARGET_NS}"
  kubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username="${SIGNUM}" --docker-password="${S_PASSWORD}" --docker-email="${WORK_EMAIL}" --namespace="${TARGET_NS}"

  CFG_MOCK_DATA_PATH=${HOME}/gerrit/eric-oss-core-slice-assurance-cfg/dev/helmCharts/wiremock/mocker/data
  AUG_MOCK_DATA_PATH=${HOME}/gerrit/eric-oss-assurance-augmentation/dev/helmCharts/wiremock/mocker/data
  msg "\nCFG mock file path: ${CFG_MOCK_DATA_PATH}"
  msg "AUG mock file path: ${AUG_MOCK_DATA_PATH}"

  msg "\nkubectl create configmap cfg-dep-mocker-responses-configs --from-file=$CFG_MOCK_DATA_PATH/__files --namespace=${TARGET_NS}"
  kubectl create configmap cfg-dep-mocker-responses-configs --from-file="$CFG_MOCK_DATA_PATH/__files" --namespace="${TARGET_NS}"
  kubectl label configmap cfg-dep-mocker-responses-configs app.kubernetes.io/managed-by=Helm --namespace="${TARGET_NS}" && kubectl annotate configmap cfg-dep-mocker-responses-configs meta.helm.sh/release-name=small --namespace="${TARGET_NS}" && kubectl annotate configmap cfg-dep-mocker-responses-configs meta.helm.sh/release-namespace="${TARGET_NS}" --namespace="${TARGET_NS}"

  msg "\nkubectl create configmap cfg-dep-mocker-mappings-configs --from-file=$CFG_MOCK_DATA_PATH/mappings --namespace=${TARGET_NS}"
  kubectl create configmap cfg-dep-mocker-mappings-configs --from-file="$CFG_MOCK_DATA_PATH/mappings" --namespace="${TARGET_NS}"
  kubectl label configmap cfg-dep-mocker-mappings-configs app.kubernetes.io/managed-by=Helm --namespace="${TARGET_NS}" && kubectl annotate configmap cfg-dep-mocker-mappings-configs meta.helm.sh/release-name=small --namespace="${TARGET_NS}" && kubectl annotate configmap cfg-dep-mocker-mappings-configs meta.helm.sh/release-namespace="${TARGET_NS}" --namespace="${TARGET_NS}"

  msg "\nkubectl create configmap aas-dep-mocker-responses-configs --from-file=$AUG_MOCK_DATA_PATH/__files --namespace=${TARGET_NS}"
  kubectl create configmap aas-dep-mocker-responses-configs --from-file="$AUG_MOCK_DATA_PATH/__files" --namespace="${TARGET_NS}"
  kubectl label configmap aas-dep-mocker-responses-configs app.kubernetes.io/managed-by=Helm --namespace="${TARGET_NS}" && kubectl annotate configmap aas-dep-mocker-responses-configs meta.helm.sh/release-name=small --namespace="${TARGET_NS}" && kubectl annotate configmap aas-dep-mocker-responses-configs meta.helm.sh/release-namespace="${TARGET_NS}" --namespace="${TARGET_NS}"

  msg "\nkubectl create configmap aas-dep-mocker-mappings-configs --from-file=$AUG_MOCK_DATA_PATH/mappings --namespace=${TARGET_NS}"
  kubectl create configmap aas-dep-mocker-mappings-configs --from-file="$AUG_MOCK_DATA_PATH/mappings" --namespace="${TARGET_NS}"
  kubectl label configmap aas-dep-mocker-mappings-configs app.kubernetes.io/managed-by=Helm --namespace="${TARGET_NS}" && kubectl annotate configmap aas-dep-mocker-mappings-configs meta.helm.sh/release-name=small --namespace="${TARGET_NS}" && kubectl annotate configmap aas-dep-mocker-mappings-configs meta.helm.sh/release-namespace="${TARGET_NS}" --namespace="${TARGET_NS}"

  msg "\n${GREEN}helm install small ${script_dir}"
  helm install small "${script_dir}" --namespace="${TARGET_NS}"

fi

if [[ $postconfig == "1" ]]; then

  msg "\n${BLUE}Executing post config"

  if [[ $install == "1" ]]; then
    msg "${GREEN}Will sleep 4 minutes before deployment ready"
    sleep 240
  fi


  msg "\n${GREEN}Updating DC message-bus"

  kubectl exec -it edb-0 -c edb --namespace="${TARGET_NS}" -- curl -X POST "http://eric-oss-data-catalog:9590/catalog/v1/message-bus" -H "Content-Type:application/json" -d "{\"name\":\"eric-oss-dmm-kf\",\"clusterName\":\"haber020\",\"nameSpace\":\"${TARGET_NS}\",\"accessEndpoints\":[\"eric-oss-dmm-kf:9092\"]}"

  msg "\n${GREEN}Enable parser configurator:"
  msg "kubectl scale deployment eric-oss-stats-parser-configurator --namespace=${TARGET_NS} --replicas=1"
  kubectl scale deployment eric-oss-stats-parser-configurator --namespace="${TARGET_NS}" --replicas=1

  msg "\n${GREEN}Updating aug/cfg log"
  kubectl get deployments eric-oss-assurance-augmentation --namespace="${TARGET_NS}" -o yaml | sed -E 's/classpath:logback-json.xml//' | kubectl replace -f -
  kubectl get deployments eric-oss-core-slice-assurance-cfg --namespace="${TARGET_NS}" -o yaml | sed -E 's/classpath:logback-json.xml//' | kubectl replace -f -

fi

if [[ $application == "1" ]]; then
  if [[ $postconfig == "1" ]]; then
    msg "${GREEN}Will sleep 3 minutes before deployment ready"
    sleep 180
  fi
  kubectl scale deployment eric-oss-assurance-augmentation --namespace="${TARGET_NS}" --replicas=1
  kubectl scale deployment eric-oss-core-slice-assurance-cfg --namespace="${TARGET_NS}" --replicas=1
fi

if [[ $install == "1" ]]; then

  msg "\n${BLUE}Change current namespace context to ${TARGET_NS}"
  msg "kubectl config set-context --current --namespace=${TARGET_NS}"
  kubectl config set-context --current --namespace="${TARGET_NS}"

fi

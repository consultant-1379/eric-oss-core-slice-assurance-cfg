#
# COPYRIGHT Ericsson 2023
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

#!/bin/bash

R_TOKEN=$(curl --silent --get https://armdocker.rnd.ericsson.se/v2/token?service=armdocker.rnd.ericsson.se&scope=repository:proj-ldc:pull)


REGISTRY_TOKEN=$(echo $_TOKEN | jq -r '.token')

curl --silent --get -H "Accept: application/json" -H "Authorization: Bearer $REGISTRY_TOKEN" https://armdocker.rnd.ericsson.se/v2/proj-ldc/common_base_os_release/sles/tags/list | jq --raw-output '.tags' | grep -v '\[' | grep -v '\]' | tr -d "\"" | tr -s ' ' | tr -d ',' | tr -d ' ' | sort -n -t '.' -k 1,1 -k2,2 | tail

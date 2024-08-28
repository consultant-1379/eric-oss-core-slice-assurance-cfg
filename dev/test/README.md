# Automated script to run CSAC non-functional tests

### Introduction:

- `eric-oss-core-slice-assurance-cfg/dev/test` folder contains two script `common_functions.sh` and `run_non_functional_tests.sh`
- `common_functions.sh` contains the core functionality required to run the non-functional tests irrespective of the service.
- `run_non_functional_tests.sh` contains the service specific code.
- This document lists out the automated test cases, pre-requisites, clone and own process, supported functionality of this script and instructions on how to use `run_non_functional_tests.sh` script.

### Automated test cases:

1. Characteristics
    1. Startup time (to fully ready)
    2. Restart time (to fully ready)
    3. Upgrade time (to fully ready)
    4. Rollback time (to fully ready)
    5. Image Size
    6. Microservice memory footprint required
    7. Microservice CPU footprint required
    8. Some kind of meaningful latency or throughput for your “API”
        1. Time-to-provision AAS
        2. Time-to-provision PMSC
        3. Time-to-provision AIS
        4. Total provisioning time
    9. service custom metrics
2. Deployment
    1. Scale-out
    2. Scale-in
3. Robustness
    1. Service restart case 1: All instances of a given service restart simultaneously -> The service should come back again without problem.
    2. Service restart case 2: All instances of a given service restart simultaneously, and our dependant services are not immediately available.
    3. Liveness and Readiness probes test
    4. SIGTERM and SIGKILL handling
    5. Move between workers

### Prerequisites:

Following are the prerequisites to run this script.

1. helm
2. kubectl
3. podman (podman should be up and running)


### Supported functionality:

1. Install - Installs CSAC and it's dependencies
2. Uninstall - Uninstalls CSAC and it's dependencies
3. Run tests - Installs CSAC and it's dependencies and runs all non-functional tests.

> Test results and logs are written to `results_<timestamp>.txt` and `log_<timestamp>.log` files respectively in `eric-oss-core-slice-assurance-cfg/dev/test` folder.

### Clone and own:

- This functionality to run non-functional tests can be used by other services.
- To facilitate re-using this functionality to by other services, `eric-oss-core-slice-assurance-cfg/dev/test` folder can be copied to the respective service repo.
- No changes in code are required for `common_functions.sh` file, unless, new tests or enhancements are needed.
- Service specific code resides in `run_non_functional_tests.sh` (like instructions to install a target service, install a target service's dependencies, uninstall target service and its dependent services, setting target service specific variables,...)
- Functions in `run_non_functional_tests.sh` needs to be substituted with target service specific functionality and variables.
- `common_functions.sh` (core functionality) is used in `run_non_functional_tests.sh` by adding this line `source run_sanity_test.sh` in `run_non_functional_tests.sh`. Please refer `run_non_functional_tests.sh` for the functions that need to be implemented.

### Running the script:

The script takes one of these options `-i`, `-u`, `-r`, `-h`

#### Usage

1. To install CSAC and its dependencies

```text
./run_non_functional_tests.sh -i
```

Running this command prompts for following info

example:

```text
Enter SIGNUM: signum
Enter host password for user 'signum':
Enter email: your.email@ericsson.com
Do you want to use existing namespace (y/n): n
Enter namespace: swordform-signum
Enter CSAC helm chart version: 1.243.0-1
```

2. To uninstall CSAC and it's dependencies

```text
./run_non_functional_tests.sh -u
```

Running this command prompts for following info

example:

```text
Enter namespace:swordform-signum
Do you want to delete namespace(y/n):y
```

3. To install CSAC and it's dependencies and run all non-functional tests

```text
./run_non_functional_tests.sh -r
```

Running this command prompts for following info

example:

```text
Enter SIGNUM: signum
Enter host password for user 'signum':
Enter email: your.email@ericsson.com
Do you want to use existing namespace (y/n): n
Enter namespace: swordform-signum
Enter CSAC helm chart version: 1.243.0-1
Enter CSAC helm chart version for upgrade: 1.244.0-1
Docker image tag: 1.243.0-1
Do you want to delete namespace after tests executed (y/n): y
```

4. To display help information

```text
./run_non_functional_tests.sh -h
```

Running this command will display below info

```text

**************************************************************************************
run_non_functional_tests.sh is a utility to setup an ESOA service on k8s cluster and run manual tests.
**************************************************************************************
Usage: run_non_functional_tests.sh [-i] [-u] [-r] [-h]

Options:
  -i : Installs CSAC and its dependencies
  -u : Uninstalls CSAC and its dependencies
  -r : Installs CSAC with dependencies and runs manual tests
  -h : Displays help information
```

> Note: This script must be run from within `eric-oss-core-slice-assurance-cfg/dev/non_functional_tests` folder since this refers to the integration chart in dev folder.

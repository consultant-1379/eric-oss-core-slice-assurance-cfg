<!--Document Template information:
Prepared:Stefano Volpe
Approved:***
Document Name:user-guide-template
Revision: {!.bob/var.user-guide-version!}
Date: {!.bob/var.date!}
-->

# Core Slice Assurance Configurator User Guide

[TOC]

## Overview

This document provides an overview of the Core Slice Assurance Configurator (CSAC) microservice including a brief description of its main features and interfaces.



### Revision History

| Date       | Comment                                                      | Author  |
|------------|--------------------------------------------------------------|---------|
| 2022-11-23 | Initial draft                                                | EBETRON |
| 2023-07-17 | SIP-TLS Support                                              | EWIRKRI |
| 2023-10-03 | AAS Provisioning                                             | EUNIWLE |
| 2023-10-17 | AIS Provisioning                                             | LMCLIAN |
| 2023-10-26 | EDB support                                                  | EYONDUU |
| 2023-11-02 | Add Hardening Section and update Security Guidelines Section | LMCLIAN |

### Description of the Service

The CSAC service provides cloud-native (kubernetes) configuration capabilities for statically defined assurance configuration.

This centralized assurance configuration is used to provision core services needed for calculation and visualization of assurance resources, for example network slice key performance indicators (KPIs).  Assurance configuration includes:

- Performance Marker (PM) counter definitions
- Key Performance Indicator (KPI) calculation definitions
- Augmentation definitions
- Simple profiles providing additional information needed to instantiate runtime configuratoin for downstream systems.

The following runtime configuration is calculated and submitted to downstream systems:

| Target System                      | Configuration                                                     |
|------------------------------------|-------------------------------------------------------------------|
| PM Stats Calculator (PMSC)         | - PMSC KPI Definition <br>- KPI Calculation                       |
| Assurance Augmentation (AAS)       | - Augmentation definition                                         |
| Augmentation Indexer Service (AIS) | - Index metadata <br>- Target definition <br>- Writer definitions |

CSAC integrates with the following services:

- [PM Server](https://adp.ericsson.se/marketplace/pm-server) to provide metrics
- [Log Transformer service](https://adp.ericsson.se/marketplace/log-transformer) to supply logs
- [Service Identity Provider TLS (SIP-TLS)](https://adp.ericsson.se/marketplace/service-identity-provider-tls) to enable cluster-internal-secured communication with server and client authentication based on mTLS


### Features

- Out-of-box (OOB) assurance configuration - the primary use case for CSAC is the provisioning of default, out-of-box assurance configuration which provides the Ericsson Core Assurance application with baseline assurance and monitoring capabilities.
- Support for custom assurance configuration - a deployer or system integrator (SI) may install custom assurance configuration such as custom KPI definitions by creating a custom kubernetes ConfigMap resource with the requisite assurance configuration.


### Main Concepts

### Supported Use Cases

This chapter gives an overview of the supported use cases.

| Use Case ID               | Use Case Title                                                        | Compliance      | Maturity |
|---------------------------|-----------------------------------------------------------------------|-----------------|----------|
| UC.ECA.CSAC.PROVISION.KPI | Provision OOB KPIs in the PM Stats Calculator                         | Fully supported | Alpha    |
| UC.ECA.CSAC.PROVISION.AAS | Provision OOB Augmentations in the Assurance Augmentation Service     | Fully supported | Alpha    |
| UC.ECA.CSAC.PROVISION.AIS | Provision OOB KPI index and metadata in the Assurance Indexer Service | Fully supported | Alpha    |

### Maturing Features

| Feature Slogan                      | Feature Maturity | Enabled by Default | Related Use Case(s)       |
|-------------------------------------|------------------|--------------------|---------------------------|
| PM Definition Validation            | Alpha            | Yes                | UC.ECA.CSAC.PROVISION.KPI |
| PM Stats Calculator Provisioning    | Alpha            | No                 | UC.ECA.CSAC.PROVISION.KPI |
| Assurance Augmentation Provisioning | Alpha            | No                 | UC.ECA.CSAC.PROVISION.AAS |
| Assurance Indexer Provisioning      | Alpha            | No                 | UC.ECA.CSAC.PROVISION.AIS |

#### Enabling Maturing Features

 **PM Definition Validation**

| Service | Helm Parameters                  |
|---------|----------------------------------|
| CSAC    | validation.external.enabled=true |

**PM Stats Calculator Provisioning**

| Service | Helm Parameters                |
|---------|--------------------------------|
| CSAC    | provisioning.pmsc.enabled=true |

**Assurance Augmentation Service Provisioning**

| Service | Helm Parameters               |
|---------|-------------------------------|
| CSAC    | provisioning.aas.enabled=true |


**Assurance Indexer Service Provisioning**

| Service | Helm Parameters                 |
|---------|---------------------------------|
| CSAC    | provisioning.index.enabled=true |


### Architecture

On startup, CSAC loads the assurance configuration from JSON files deployed using kubernetes ConfigMap resources.  The assurance configuration is validated to ensure that the generated runtime configuration will contain valid resources.  When validated, shareable resources such as PM specifications and KPI definitions are stored in the Data Dictionary.  Runtime configuration as needed by the downstream PM Stats Calculator is then calculated based on changes detected in the incoming assurance configuration, and the resulting runtime KPI instances are submitted to the PM Stats Calculator for subsequent periodic calculation. If successfully deployed, the runtime configuration is persisted in the runtime data store.

Clients can view Data Dictionary resources and deployed runtime configuration using the REST API defined in IF.OSS_AIR.CSAC.CFG.

The following picture shows the CSAC Service and its
architectural context.

![Architecture](csac_service_architecture.png)

Figure 1 Architecture view of Core Slice Assurance Configurator


#### Application Programming Interfaces (APIs)

CSAC provides the following interfaces:

| Interface Logical Name | Description                                                                                                                                                      | Maturity |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| IF.OSS_AIR.CSAC.CFG    | Read-only API for Data Dictionary and deployed runtime configuration. This interface is intended for internal use only and is not available to external clients. | Alpha    |

See the Application Developers Guide and the API Documentation for further information.

### Deployment View

CSAC is packaged as a Docker container. It supports deployment in
Kubernetes using Helm.  Assurance configuration is deployed using kubernetes ConfigMap resources.

CSAC depends on PostgreSQL, which must be deployed as the database backend for the service.

CSAC depends on the Data Management and Movement (DMM) Data Catalog and Schema Registry for retrieving PM counter Avro schemas. Avro schemas are used for validating the PM definition configuration supplied with CSAC.

CSAC depends on the PM Stats Calculator service for provisioning KPIs supplied with the CSAC service.

CSAC depends on the Assurance Augmentation service for provisioning Augmentation definitions configured with the CSAC service. CSAC depends on the following interface:
- IF.OSS_AIR.AUG.REG -augmentation registration REST API

CSAC depends on the Assurance Indexer Service for provisioning Index definitions configured with the CSAC service. CSAC depends on the following interface:
- IF.OSS_AIR.INDEX.REG -Assurance Indexer Service REST API

CSAC depends on Analytics Reporting Dimensioning Query services specified in its deployment configuration for validating Augmentation definitions. CSAC depends on the following interface:
- IF.OSS_AIR.AUG.DATA -Analytics Reporting Dimensioning Query REST API

CSAC integrates with the PM Server to provide metrics.

CSAC integrates with the Log Transformer service to supply logs.

CSAC integrates with the Service Identity Provider TLS (SIP-TLS) service to enable cluster-internal-secured communication with server and client authentication based on mTLS.

![Deployment Overview](csac-deployment-view.png)

Figure 2 Deployment view of CSAC

To deploy the Service, refer to the [Deployment section](#deployment), which:

- explains how to get started using the CSAC Service in the
supported environments.
- specifies configuration options for starting the <*Service Name*> docker
container.

If problems occur when using the service, refer to the [Troubleshooting section](#troubleshooting).

### Dimensioning and Characteristics

#### Dimensioning

To handle dimensioning configuration at deployment time,
refer to the [Deployment section](#deployment).

#### Scaling

| Scaling Supported (Yes/No) | Minimum number of instances | Maximum number of recommended instances                       |
|----------------------------|-----------------------------|---------------------------------------------------------------|
 | Yes                        | 1                           | 2 for high availability, otherwise based on application needs |

<!-- To be added at a later date when there is an established approach for determining baselines
#### Characteristics

| Characteristic                                               | Description                                                                                                                                                                                                                                                    | Result |
|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| Startup time (to fully ready)                                | It is from the point where the service is allowed to start, i.e. dependencies startup and scheduling time excluded from the measurement.                                                                                                                       | TBD    |
| Restart time (to fully ready)                                | Measure from the point where a POD is killed until the service instance is fully ready.                                                                                                                                                                        | TBD    |
| Upgrade time (to fully ready)                                | Measure time from the point 'helm upgrade' is executed until the upgrade is completed and service instance is fully ready. Upgrade shall be executed under "traffic" and service shall be considered fully ready when traffic disturbance (if any) is over.    | TBD    |
| Rollback time (to fully ready)                               | Measure time from the point 'helm rollback' is executed until the rollback is completed and service instance is fully ready. Rollback shall be executed under "traffic" and service shall be considered fully ready when traffic disturbance (if any) is over. | TBD    |
| Image Size                                                   | CSAC docker image size                                                                                                                                                                                                                                         | TBD    |
| Microservice memory footprint required                       | This is the memory footprint required to achieve your published SLO                                                                                                                                                                                            | TBD    |
| Microservice CPU footprint required                          | This is the CPU footprint required to achieve your published SLO                                                                                                                                                                                               | TBD    |
| Some kind of meaningful latency or throughput for your “API” | This is characterized by the time from loading the resource configuration from file to completion of provisioning of the downstream consumers (PMSC, AAS, Indexing)                                                                                            | TBD    |
| Average KPI resource loading time                            | This is the average time taken for loading a single KPI. It is calculated by incrementally adding 1 KPI to the config file and calculating average of the load time per KPI                                                                                    | TBD    |
-->

#### Resilience

CSAC supports high availability of its REST API through multiple pod instances.

<!--
#### Upgrade - In Service Software Upgrade

<*Describe how the service supports upgrades, that is how the service fulfills
the ISSU requirement. Describe from a function point of view how ISSU is
supported and what a service unavailability would mean for this particular
service. Do not include practical commands, that is the scope of the Deployment
section*>
-->

#### High Availability

CSAC supports high availability, and the following configurations are provided by default in the values.yaml file:

1. Set the pod anti-affinity rule to `soft` in values.yaml such that scheduler still schedules the pod even if it can't find a matching node.
   There are two options: preferredDuringScheduling (hard), IgnoredDuringExecution (soft).
````text
affinity:
  podAntiAffinity: "soft"
````
2. Support setting the topology spread constraints (default = undefined) in the values.yaml to help schedule pods across user-defined topologies, such as zones or regions.
   In `topologySpreadConstraints`, options include `maxSkew`, `minDomains`, `topologyKey`, `labelSelector`, label selectors and `matchLabelKeys`.
````text
nodeSelector: { }
topologySpreadConstraints:
  deployment: [ ]
  test: [ ]
````
3. Configure pre-stop hook with terminationGracePeriodSeconds in values.yaml.
````text
terminationGracePeriodSeconds: 30
````
4. Set the Quality of Service config for pod eviction policy for resource issues on the node to Guaranteed by making the resource request memory equals to resource limit memory.
   There are 3 option for Quality of Service class - Guaranteed, Burstable or BestEffort.
   This can be viewed in the specification of containers (pods).
````text
status:
    qosclass: Guaranteed
````
5. Configure the mandatory liveness and readiness health probes in the values.yaml.
````text
probes:
    eric-oss-core-slice-assurance-cfg:
        livenessProbe:
            failureThreshold: 3
            initialDelaySeconds: 120
            periodSeconds: 10
            timeoutSeconds: 10
        readinessProbe:
            failureThreshold: 3
            initialDelaySeconds: 120
            periodSeconds: 10
            timeoutSeconds: 10
````

## Deployment

This section describes the operational procedures for how to deploy and upgrade
the CSAC Service in a Kubernetes environment with Helm. It also
covers hardening guidelines to consider when deploying this service.

### Prerequisites

-  A running Kubernetes environment with helm support, some knowledge
    of the Kubernetes environment, including the networking detail, and
    access rights to deploy and manage workloads.

-   Access rights to deploy and manage workloads.

-   Availability of the kubectl CLI tool with correct authentication
    details. Contact the Kubernetes System Admin if necessary.

-   Availability of the helm package.

-   Availability of Helm charts and Docker images for the service and
    all dependent services.

### Custom Resource Definition (CRD) Deployment

Custom Resource Definitions (CRD) are cluster-wide resources that must be
installed prior to the Generic Service deployment.
The service that requires the latest CRD charts versions sets which CRD charts
versions to use in the deployment.

> Note: The release name chosen for the CRD charts in the Kubernetes deployment must be
> kept and cannot be changed later.

Helm ALWAYS requires a namespace to deploy a chart and the namespace to be used
for the same per-service CRD charts must be created before the CRD charts are
loaded.

> Note: The upgrade of the CRDs must be done before deploying a service that
> requires additional CRDs or a CRD version newer than the one already deployed on
> the Kubernetes cluster.

The CRD chart is deployed as follows:

1. Download the latest applicable CRD charts files from the Helm charts
   repository.

2. Create the `namespace` where CRD chart will be deployed in case it doesn't
   already exist. This step is only done once on the Kubernetes cluster.

    ```
    kubectl create namespace <NAMESPACE-FOR-CRDs>
    ```

3. Deploy the CRD using `helm upgrade` command:

    ```
    helm upgrade --install --atomic <release name> <CRD chart with version> --namespace <NAMESPACE-FOR-CRDs>
    ```

4. Validate the CRD installation with the `helm ls` command:

    ```
    helm ls -a
    ```

In the table locate the entry for the `<release name>`,  validate the
`NAMESPACE` value and check that the STATUS is set to `deployed`.
When the output is validated continue with the installation of the helm charts.

<*To verify that the CRD was installed correctly, the logs of that job can
be used instead of the step above. In this case it should be listed which log
entries indicate a successful CRD charts deployment*>

### Deployment in a Kubernetes Environment Using Helm

This section describes how to deploy the service in Kubernetes using Helm and
the `kubectl` CLI client. Helm is a package manager for Kubernetes that
streamlines the installation and management of Kubernetes applications.

#### Preparation

Prepare helm chart and docker images. Helm chart in the following link
can be used for installation:

https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm/eric-oss-core-slice-assurance-cfg/eric-oss-core-slice-assurance-cfg-<version>.tgz

#### Pre-Deployment Checks for CSAC

Ensure the following:

- The <RELEASE_NAME> is not used already in the corresponding cluster.
Use `helm list` command to list the existing deployments (and delete previous
deployment with the corresponding <RELEASE_NAME> if needed).

- The same namespace is used for all deployments.

#### Deployment of Required Services

1. [Helm Chart Installation of Service Identity Provider TLS Service](#helm-chart-installation-of-service-identity-provider-tls-service) is recommended to be completed first if any provided or consumed internal interfaces are secured.

2. Mandatory services: [Enterprise Postgres Database](#enterprise-postgres-database) and [Log Transformer service](https://adp.ericsson.se/marketplace/log-transformer) unless the configuration parameter `log.streamingMethod` is set to "indirect" as described in [Helm Chart Installation of Log Transformer](#helm-chart-installation-of-log-transformer)

3. When service metrics are to be scraped: [PM Server](https://adp.ericsson.se/marketplace/pm-server)

4. When provisioning KPIs in the PM Stats Calculator: [PM Stats Calculator](https://adp.ericsson.se/marketplace/pm-stats-calculator)

5. When provisioning Assurance Augmentation Service with augmented schemas: [Assurance Augmentation Service](https://adp.ericsson.se/marketplace/assurance-augmentation)

6. When validating external PM definitions and Augmentation definitions: [Data Catalog](https://adp.ericsson.se/marketplace/data-catalog), [Schema Registry](https://adp.ericsson.se/marketplace/schema-registry-sr) and [Core Analytics Reporting Dimensioning Query](https://adp.ericsson.se/marketplace/core-analytics-reporting-dimensioning-qu)

7. When provisioning KPI index and metadata in the Assurance Indexer Service: [Assurance Indexer](https://adp.ericsson.se/marketplace/assurance-indexer)

##### Helm Chart Installation of Service Identity Provider TLS Service

When any provided or consumed internal interfaces are secured, then the [Service Identity Provider TLS (SIP-TLS)](https://adp.ericsson.se/marketplace/service-identity-provider-tls) service must be deployed to support cluster-internal-secured communication with server and client authentication based on mTLS.

For information related to SIP-TLS service installation,
see [Service Identity Provider TLS User Guide.](https://adp.ericsson.se/marketplace/service-identity-provider-tls/documentation)

###### To Enable mTLS:

Set the configuration parameter `global.security.tls.enabled` to true

The scheme of a consumed REST API must be HTTPS to enable mTLS communication with that API. See the [Configuration Parameters](#configuration-parameters) section for details.

##### Enterprise Postgres Database

CSAC utilizes the Enterprise Postgres Database (EDB) as its backend database, which must be installed prior to CSAC. A user secret must also be created to provide credentials for CSAC.

###### EDB Database user credential secret

Notes: The usernames provided in this secret should be simple text. Do not explicitly enclose the usernames in double quotes ("").
See [Postgresql Documentation](https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS) for details.

```text
kubectl create secret generic <USER_SECRET> --from-literal=pguserid=<CSAC_USER> --from-literal=pgpasswd=<CSAC_USER_PWD>  --from-literal=super-user=<SUPER_USER> --from-literal=super-pwd=<SUPER_PWD> --from-literal=metrics-pwd=<METRICS_PWD> --from-literal=replica-user=<REPLICA_USER> --from-literal=replica-pwd=<REPLICA_PWD> --namespace=<NAMESPACE>
```
The variables specified in the command are as follows:

- `<USER_SECRET>`: String value. A secret specified by users to store usernames and passwords in the database. Value should
  be `eric-oss-core-slice-assurance-cfg-db-secret` for CSAC.
- `<CSAC_USER>`: String value. A name customized by users to identify user and is used with `<CSAC_USER_PWD>`.
- `<CSAC_USER_PWD>`: String value. A password customized by users to identify user and is used with `<CSAC_USER>`.
- `<SUPER_PWD>`: String value. A password specified by users for Postgres.
- `<SUPER_USER>`: String value. An admin user specified by users for Postgres.
- `<METRICS_PWD>`: String value. A password specified by users for metrics user "exporter".
- `<REPLICA_USER>`: String value. A username specified by users for backup and restore in the database.
- `<REPLICA_PWD>`: String value. A password specified by users for backup and restore in the database and is used with `<REPLICA_USER>`.
- `<NAMESPACE>`: String value. An area to deploy your own Helm charts instances, separate from other users.

###### EDB Secure Connection Certificates Secret

CSAC also supports secure connections to EDB. More detailed configurations can be found in the "Configuration Parameters" section. When the CSAC database's `sslMode` is enabled, the Helm chart will mount client and server certificates to CSAC microservices, depending on the availability of the secret.
- If no certificates are provided, a non-secure connection will be used to connect to PostgreSQL.
- If a server certificate is provided and `sslMode` is enabled, a TLS connection will be used to connect to PostgreSQL.
- If both server and client certificates are provided and `sslMode` is enabled, an mTLS connection will be used to connect to PostgreSQL.

Here are example scripts for manually creating certificate secrets.

```shell
# Create client certificate
kubectl create secret generic csac-edb-client-cert --from-file=client.crt=./client/client.crt --from-file=client.key=./client/client.key
# Create server certificate
kubectl create secret generic edb-server-cert --from-file=cacert.crt=./root/root.crt
```

##### Helm Chart Installation of Log Transformer

As per helm Design rule [DR-D470222-010](https://eteamspace.internal.ericsson.com/display/AA/LOG+general+design+rules#LOGgeneraldesignrules-DR-D470222-010), it was required that all services support three logging methods that align with the ADP log collection pattern.
Each of which are controlled by the `log.streamingMethod` configuration.

- "indirect": Stdout to infrastructure logging framework.
- "direct": Direct streaming to the Log Aggregator (Log Transformer).
- "dual": Stdout to infrastructure logging framework and direct streaming to Log Aggregator.

During deployment, the default streaming method is "direct". The logs will be streamed securely if TLS is globally enabled.
The `.Values.log.streamingMethod` or `.Values.global.log.streamingMethod` configuration can be set to modify the streaming method.

For information related to Log Transformer installation,
see [Log Transformer User Guide.](https://adp.ericsson.se/marketplace/log-transformer/documentation)


#### Helm Chart Installation of CSAC Service

>**Note:** Ensure all dependent services are deployed and healthy before you
>continue with this step (see previous section).

Helm is a tool that streamlines installing and managing Kubernetes
applications. CSAC can be deployed on Kubernetes using
Helm Charts. Charts are packages of pre-configured Kubernetes resources.

Users can override the default values provided in the values.yaml template of
the helm chart. The recommended parameters to override are listed in the
following section: [Configuration Parameters](#configuration-parameters).

##### Create Kubernetes Secret for CSAC
Kubernetes secrets need to be created before installing the helm chart.

Image pull secret needs to be created as the authentication information to pull the images of CSAC.</br>
To create an image pull secret:
```text
kubectl create secret docker-registry <pull_secret> --docker-server=<server_url> --docker-username=<user_name> --docker-password=<user_pwd> --docker-email=<user_mail> --namespace=<namespace>
```
   - <pull_secret>: String value. Secret of docker-registry type to authenticate with a container registry to pull a private image. 
   - <user_name>: String value. A name to identify the Docker user and is used with <user_pwd>.
   - <user_pwd>: String value, a password to identify the Docker user and is used with <user_name>.
   - <user_mail>: Docker E-mail addresses of users.

For example:
```text
kubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username=dockerusername --docker-password=dockerpassword --docker-email=example@ericsson.com --namespace=example
```

##### Deploy the CSAC Service
Install the CSAC service on the Kubernetes cluster by using the
helm installation command:

```text
helm install <RELEASE_NAME> <CHART_REFERENCE> --namespace=<NAMESPACE> [--set <parameters>]
```

The variables specified in the command are as follows:


- `<RELEASE_NAME>`: String value, a name to identify and manage your helm chart.

- `<CHART_REFERENCE>`: A path to a packaged chart, a path to an unpacked chart
directory or a URL.

- `<NAMESPACE>`: String value, a name to be used dedicated by the user for
deploying own helm charts.

Helm install command with for successful CSAC installation:

```text
helm install csac eric-oss-core-slice-assurance-cfg-x.x.x-x.tgz --set appArmorProfile.type=unconfined,global.pullSecret=k8s-registry
```

##### Verify the CSAC Service Availability

To verify whether the deployment is successful, do as follows:*

*1.  Check if the chart is installed with the provided release name and
    in related namespace by using the following command:*

```text
$helm ls --namespace=<NAMESPACE>
```

  *Chart status should be reported as "DEPLOYED".*

*2.  Verify the status of the deployed helm chart.*

```text
$helm status <RELEASE_NAME>
```

  *Chart status should be reported as "DEPLOYED". All Pods status should be
  reported as "Running" and number of Deployment Available should be the
  same as the replica count.*

*3.  Verify that the pods are running
    by getting the status for your pods.*

```text
$kubectl get pods --namespace=<NAMESPACE> -L role
```

  *For example:*

```text
$helm ls --namespace=example
$helm status examplerelease
$kubectl get pods --namespace=example -L role
```

### Configuration Parameters

#### Mandatory Configuration Parameters

The parameters in following table are mandatory to set at deployment time.
If not provided, the deployment will fail. There are no default values
provided for this type of parameters.


#### Optional Configuration Parameters

Following parameters are not mandatory. If not explicitly set
(using the --set argument), the default values provided
in the helm chart are used.

Global level parameters allowing customization for eric-oss-core-slice-assurance-cfg:

| Variable Name                                                         | Description                                                                                                                                                                                                                                                                                                                                                                           | Default Value                        |
|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| global.createSchema                                                   | When set to true, service creates SCHEMA object with the schema user defined at service level                                                                                                                                                                                                                                                                                         | true                                 |
| global.security.policyBinding.create                                  | Whether to create a rolebinding for security policy role                                                                                                                                                                                                                                                                                                                              | false                                |
| global.security.policyReferenceMap.default-restricted-security-policy | Map for changing default name of default restricted security policy                                                                                                                                                                                                                                                                                                                   |                                      |
| global.security.tls.enabled                                           | Enables mTLS on all provided CSAC interfaces and allows CSAC to establish mTLS connections with consumed interfaces like the PM Stats Calculator. To enable secured communication with a consumer interface, the scheme of its configured URL must be HTTPS.                                                                                                                          | true                                 |
| global.security.tls.trustedInternalRootCa.secret                      | The secret holding the public certificate bundle of the root CA that issues application-internal server certificates, and optionally, the infrastructure root CA certificates that the application trusts in the file 'ca.crt'. This public certificate bundle is created by a deployment engineer, which should preferably originate from a cloud PKI service as set of private CAs. | "eric-sec-sip-tls-trusted-root-cert" |

Application level parameters allowing customization for eric-oss-core-slice-assurance-cfg:

| Variable Name                                             | Description                                                                                                                                                                                                                                                                                          | Default Value                                                                                                                                                                                                                                           |
|-----------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| csac.resource.oob                                         | List of out-of-box resource files. Files will be loaded in the order they are listed. OOB files are always loaded and validated before custom resources are loaded                                                                                                                                   | - 'csac-oob-kpi-defs.json' <br>- 'csac-oob-site-nssi-aggregation.json' <br>- 'csac-ran-oob-kpi-defs.json' <br>- 'Partial-DRB-Accessibility.json' <br>- 'csac-core-oob-kpi-2.1.0.json' <br>- 'csac-oob-esoa-13026.json' <br>- 'csac-oob-esoa-13341.json' |
| database.host                                             | Service name of the deployed PostgreSQL database service.                                                                                                                                                                                                                                            | eric-oss-core-slice-assurance-cfg-db                                                                                                                                                                                                                    |
| database.secret                                           | Database user credential secret name.                                                                                                                                                                                                                                                                | eric-oss-core-slice-assurance-cfg-db-secret                                                                                                                                                                                                             |
| database.userKey                                          | Key of customized user name in the database credential secret for user connecting to PostgreSQL database.                                                                                                                                                                                            | pguserid                                                                                                                                                                                                                                                |
| database.passwdKey                                        | Key of customized user password in the database credential secret for user connecting to PostgreSQL database.                                                                                                                                                                                        | pgpasswd                                                                                                                                                                                                                                                |
| database.dbaUserKey                                       | Key of DBA user name in the database credential secret, required if creating DATABASE and/or SCHEMA objects.                                                                                                                                                                                         | super-user                                                                                                                                                                                                                                              |
| database.dbaPasswdKey                                     | Key of DBA user password in the database credential secret, required if creating DATABASE and/or SCHEMA objects.                                                                                                                                                                                     | super-pwd                                                                                                                                                                                                                                               |
| database.dbName                                           | Name of database in PostgreSQL database, CSAC persists data in this database.                                                                                                                                                                                                                        | csacdb                                                                                                                                                                                                                                                  |
| database.port                                             | database port.                                                                                                                                                                                                                                                                                       | 5432                                                                                                                                                                                                                                                    |
| database.vendor                                           | database vendor.                                                                                                                                                                                                                                                                                     | postgresql                                                                                                                                                                                                                                              |
| database.jdbcUrl                                          | Database URL. If 'jdbcUrl' is not provided, build the database URL using the vendor, host, port, and dbName.                                                                                                                                                                                         |                                                                                                                                                                                                                                                         |
| database.connectRetries                                   | The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries. The interval between retries doubles with each subsequent attempt. | 9                                                                                                                                                                                                                                                       |
| database.dictSchema                                       | The schema name of data dictionary                                                                                                                                                                                                                                                                   | dict                                                                                                                                                                                                                                                    |
| database.rtSchema                                         | The schema name of runtime data store                                                                                                                                                                                                                                                                | rtds                                                                                                                                                                                                                                                    |
| database.sslMode                                          | The database's SSL mode. The supported values are disable, require, verify-ca and verify-full.                                                                                                                                                                                                       | disable                                                                                                                                                                                                                                                 |
| database.clientCertSecret                                 | The name of the client certificates secret for database.                                                                                                                                                                                                                                             | csac-edb-client-cert                                                                                                                                                                                                                                    |
| database.clientCertKey                                    | The name of the certificate key within client certificates secret.                                                                                                                                                                                                                                   | client.key                                                                                                                                                                                                                                              |
| database.clientCertRoot                                   | The name of the root certificate within client certificates secret.                                                                                                                                                                                                                                  | client.crt                                                                                                                                                                                                                                              |
| database.serverCertSecret                                 | The name of the server certificates secret for database.                                                                                                                                                                                                                                             | edb-server-cert                                                                                                                                                                                                                                         |
| database.serverCertKey                                    | The name of the certificate key within server certificates secret.                                                                                                                                                                                                                                   | cacert.crt                                                                                                                                                                                                                                              |
| log.streamingMethod                                       | Configures how logs are treated in the service. Three potential options of "direct" (logs go directly to log transformer and not to stdout), "indirect" (logs go only to stdout). "dual" (logs go to both the log transformer and stdout)                                                            | "direct"                                                                                                                                                                                                                                                |
| provisioning.aas.enabled                                  | Enables Assurance Augmentation Service (AAS) provisioning.                                                                                                                                                                                                                                           | false                                                                                                                                                                                                                                                   |
| provisioning.aas.url                                      | Kubernetes service URL for accessing AAS service. To enable secured communication, the scheme of the URL must be HTTPS.                                                                                                                                                                              | "http://eric-oss-assurance-augmentation:8080"                                                                                                                                                                                                           |
| provisioning.aas.retry.maxRetryAttempts                   | Maximum number of attempts (including the initial call as the first attempt) of a request to AAS service when a recoverable error is returned.                                                                                                                                                       | 10                                                                                                                                                                                                                                                      |
| provisioning.aas.retry.waitDuration                       | A fixed wait period between retry attempts in milliseconds                                                                                                                                                                                                                                           | 60000                                                                                                                                                                                                                                                   |
| provisioning.aas.retry.maxDelay                           | Maximum delay time between retry attempts in milliseconds                                                                                                                                                                                                                                            | 600000                                                                                                                                                                                                                                                  |
| provisioning.aas.ardq                                     | list of ARDQ Id/URL pairs for known ARDQ services. The default is CARDQ.                                                                                                                                                                                                                             | cardq: http://eric-oss-core-reporting-dimension-query:8080                                                                                                                                                                                              |
| provisioning.aas.ardqRetry.maxRetryAttempts               | Maximum number of attempts (including the initial call as the first attempt) of a request to ARDQ service when a recoverable error is returned.                                                                                                                                                      | 10                                                                                                                                                                                                                                                      |
| provisioning.aas.ardqRetry.waitDuration                   | A fixed wait period between retry attempts in milliseconds                                                                                                                                                                                                                                           | 60000                                                                                                                                                                                                                                                   |
| provisioning.aas.ardqRetry.maxDelay                       | Maximum delay time between retry attempts in milliseconds                                                                                                                                                                                                                                            | 600000                                                                                                                                                                                                                                                  |
| provisioning.pmsc.enabled                                 | Enables PM Stats Calculator provisioning. May not enable PMSC and VM in the same deployment.                                                                                                                                                                                                         | false                                                                                                                                                                                                                                                   |
| provisioning.pmsc.legacy                                  | For backwards compatibility. Set to 'true' to provision a legacy version of the PMSC. Versions older than 1.244.0-3 must set this property to 'true'                                                                                                                                                 | false                                                                                                                                                                                                                                                   |
| provisioning.pmsc.model.legacy                            | Enables legacy output model to supporting legacy PMSC model.                                                                                                                                                                                                                                         | false                                                                                                                                                                                                                                                   |
| provisioning.pmsc.aggregationPeriod.default               | The aggregation period value for the PMSC KPI calculations (in minutes). The permissible values are one of 15, 60, or 1440.                                                                                                                                                                          | 15                                                                                                                                                                                                                                                      |
| provisioning.pmsc.data.reliabilityOffset                  | PMSC data reliability offset.  Please refer to the PMSC Application Developer's Guide for more information. The value must be less than or equal to the aggregation period                                                                                                                           | 0                                                                                                                                                                                                                                                       |
| provisioning.pmsc.url                                     | Kubernetes service URL for accessing the PMSC service. It will be used only when provisioning.pmsc.enabled is true. To enable secured communication, the scheme of the URL must be HTTPS.                                                                                                            | "http://eric-oss-pm-stats-calculator:8080"                                                                                                                                                                                                              |
| provisioning.pmsc.retry.maxRetryAttempts                  | The maximum number of request attempts (including the initial call as the first attempt) to PMSC service when a recoverable error is returned.                                                                                                                                                       | 10                                                                                                                                                                                                                                                      |
| provisioning.pmsc.retry.waitDuration                      | A fixed wait period between retry attempts in milliseconds                                                                                                                                                                                                                                           | 60000                                                                                                                                                                                                                                                   |
| provisioning.pmsc.retry.maxDelay                          | Maximum delay time between retry attempts in milliseconds                                                                                                                                                                                                                                            | 600000                                                                                                                                                                                                                                                  |
| provisioning.vm.enabled                                   | Enables Victoria Metrics provisioning. May not enable PMSC and VM in the same deployment.                                                                                                                                                                                                            | false                                                                                                                                                                                                                                                   |
| provisioning.index.enabled                                | Enables Assurance Indexing Service (AIS) provisioning.                                                                                                                                                                                                                                               | false                                                                                                                                                                                                                                                   |
| provisioning.index.force                                  | When true, forces AIS provisioning when no changes in runtime configuration are detected.                                                                                                                                                                                                            | false                                                                                                                                                                                                                                                   |
| provisioning.index.url                                    | Kubernetes service URL for accessing AIS service. It will be used only when provisioning.index.enabled is true. To enable secured communication, the scheme of the URL must be HTTPS.                                                                                                                | "http://eric-oss-assurance-indexer:8080"                                                                                                                                                                                                                |
| provisioning.index.legacy                                 | For backwards compatibility. Set to 'true' to provision a legacy version of the Index.                                                                                                                                                                                                               | false                                                                                                                                                                                                                                                   |
| provisioning.index.retry.maxRetryAttempts                 | The maximum number of request attempts (including the initial call as the first attempt) to AIS service when a recoverable error is returned.                                                                                                                                                        | 10                                                                                                                                                                                                                                                      |
| provisioning.index.retry.waitDuration                     | A fixed wait period between retry attempts in milliseconds                                                                                                                                                                                                                                           | 60000                                                                                                                                                                                                                                                   |
| provisioning.index.retry.maxDelay                         | Maximum delay time between retry attempts in milliseconds                                                                                                                                                                                                                                            | 600000                                                                                                                                                                                                                                                  |
| provisioning.index.source.pmstatsexporter.name            | Kafka topic name for 'pmstatsexporter' index source. 'pmstatsexporter' needs to match the source type in the template.                                                                                                                                                                               | pm-stats-calc-handling-avro-scheduled                                                                                                                                                                                                                   |
| service.port                                              | The port of the provided IF.OSS_AIR.CSAC.CFG interface. If global.security.tls.enabled is set to false, then the default is 8080.                                                                                                                                                                    | 8443                                                                                                                                                                                                                                                    |
| validation.external.enabled                               | Enables the PM Definition and Augmentation definitions Validation.                                                                                                                                                                                                                                   | true                                                                                                                                                                                                                                                    |
| validation.external.schemaregistry.url                    | Kubernetes service URL for accessing the Schema Registry service. It will be used only when validation.external.enabled is true. To enable secured communication, the scheme of the URL must be HTTPS.                                                                                               | "http://eric-oss-schema-registry-sr:8081"                                                                                                                                                                                                               |
| validation.external.schemaregistry.retry.maxRetryAttempts | Maximum number of attempts (including the initial call as the first attempt) of a request to Schema Registry service when a recoverable error is returned.                                                                                                                                           | 10                                                                                                                                                                                                                                                      |
| validation.external.schemaregistry.retry.waitDuration     | A fixed wait period between retry attempts in milliseconds                                                                                                                                                                                                                                           | 60000                                                                                                                                                                                                                                                   |
| validation.external.schemaregistry.retry.maxDelay         | Maximum delay time between retry attempts in milliseconds                                                                                                                                                                                                                                            | 600000                                                                                                                                                                                                                                                  |
| validation.external.datacatalog.url                       | Kubernetes service URL for accessing the Data Catalog service. It will be used only when validation.external.enabled is true. To enable secured communication, the scheme of the URL must be HTTPS.                                                                                                  | "http://eric-oss-data-catalog:9590"                                                                                                                                                                                                                     |
| validation.external.datacatalog.retry.maxRetryAttempts    | Maximum number of attempts (including the initial call as the first attempt) of a request to Data Catalog service when a recoverable error is returned.                                                                                                                                              | 10                                                                                                                                                                                                                                                      |
| validation.external.datacatalog.retry.waitDuration        | A fixed wait period between retry attempts in milliseconds                                                                                                                                                                                                                                           | 60000                                                                                                                                                                                                                                                   |
| validation.external.datacatalog.retry.maxDelay            | Maximum delay time between retry attempts in milliseconds                                                                                                                                                                                                                                            | 600000                                                                                                                                                                                                                                                  |
| affinity.podAntiAffinity                                  | Set pod anti-affinity scheduling rules. Valid values are "soft" or "hard". "hard" indicates that pods must be scheduled on different nodes. "soft" is the preferred way. When the number of Pods is greater than the number of available nodes, "hard" may cause Pods in an unschedulable state.     | soft                                                                                                                                                                                                                                                    |
| terminationGracePeriodSeconds                             | Time for graceful termination of pod after a termination request is issued.                                                                                                                                                                                                                          | 30                                                                                                                                                                                                                                                      |

### Service Dimensioning

The service provides by default resource request values and resource limit
values as part of the Helm chart. These values correspond to a default size for
deployment of an instance. This chapter gives guidance in how to do service
dimensioning and how to change the default values when needed.

#### Override Default Dimensioning Configuration

If other values than the default resource request and default resource limit
values are preferred, they must be overridden at deployment time.

Here is an example of the `helm install` command where resource requests and
resource limits are set:

```text
helm install https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-helm-dev-generic-local/some/repo/path/eric-data-my-service/eric-data-my-service-1.0.0-999.tgz --name eric-data-myservice --namespace test-deployment-namespace --set <*ADD request and limit parameters valid for this service*>
```
<!--
#### Use Minimum Configuration per Service Instance

This chapter specifies the minimum recommended configuration per service
instance. <*Columns not applicable in table below should be removed*>

| Resource Type (Kubernetes Service) | Resource Request Memory | Resource Limit Memory | Resource Request CPU | Resource Limit CPU |
|------------------------------------|-------------------------|-----------------------|----------------------|--------------------|
| CSAC                               | 2GB                     | 2GB                   | 500m                 | 500m               |

To use minimum configuration, override the default values for resource requests
and resource limits in the helm chart at deployment time.

#### Use Maximum (Default) Configuration per Service Instance

The maximum recommended configuration per instance is provided as default in the
Helm chart. Both Resource Request values and Resource Limit values are included
in the helm charts.
-->

### Hardening

#### General information about hardening

Product hardening is a process which is required to minimize the potential for attack by using only essential libraries and scanning all the vulnerabilities. This is to ensure that there should not be any exposure to malicious attacks.

#### Hardening during product development

- The service is built on minimalistic container images with small footprints, and it includes only those libraries which are essential.
- The service uses a container-optimized operating system (Common Base OS) and latest security patches are also applied on it.
- The service is also configured to strict minimum of services and ports to minimize the attack surface.
- The containers also go through vulnerability scanning to eliminate the vulnerabilities from the code.

#### Hardening during service delivery

The service can be further hardened by configuring settings related to AppArmor and Seccomp which are done through the Helm parameters located under `.Values.appArmorProfile` and `.Values.seccompProfile`, respectively.

To use the default AppArmor and Seccomp profiles, set the following parameters when installing CSAC:

```text
--set appArmorProfile.type=runtime/default,seccompProfile.type=RuntimeDefault
```

See [Configuration Parameters](#configuration-parameters) for further details.

#### Services, Ports, and Protocols

CSAC is dependent on the services:

- DMM Data Catalog
- DMM Schema Registry
- Assurance Augmentation Service (AAS)
- Core Reporting Dimension Query (CARDQ)
- Assurance Indexer Service (AIS)
- PM Status Calculator (PMSC)
- Enterprise Postgres Database (EDB)

CSAC integrates with the services:

- PM Server
- Log Transformer
- Service Identity Provider TLS (SIP-TLS)

CSAC does not expose any ports outside the cluster. See the sections [Internal Ports](#internal-ports) and [External Ports](#external-ports) for more information.

#### Certificate Management

- SIP-TLS manages the generation and maintenance of certificates for the CSAC REST API and its dependencies, such as DMM Schema Registry, and DMM Data Catalog.
- Certificates related to the EDB are managed externally to SIP-TLS and must be provided prior to CSAC deployment. For more information, see the [EDB Secure Connection Certificates Secret](#edb-secure-connection-certificates-secret) section.

#### Handling patches

In order to update the deployment, we have to run following command: (Refer to section [Upgrade Procedures](#upgrade-procedures) for more information)

```
helm upgrade --atomic <RELEASE_NAME> <CHART_PATH> [--set <previous_customized_value> ..]
```

_Note: By adding the `--atomic` flag, the upgrade process can roll back any changes if the upgrade fails. This could strengthen the hardening of our microservice during upgrades._


#### References

See the following documents for more details:

- [Hardening Guideline Instruction](https://erilink.ericsson.se/eridoc/erl/objectId/09004cff8b35654f?docno=LME-16:002235Uen&action=approved&format=msw12)
- [Hardening Guideline Template](https://erilink.ericsson.se/eridoc/erl/objectId/09004cff8b355119?docno=LME-16:002234Uen&action=approved&format=msw12)
- [Recommended Hardening Activities](https://erilink.ericsson.se/eridoc/erl/objectId/09004cffc724ed0d?docno=GFTL-21:000631Uen&action=approved&format=msw12)
- [Kubernetes Security Design Rules](https://eteamspace.internal.ericsson.com/display/AA/Kubernetes+Security+Design+Rules)

### Upgrade Procedures

> **Note:** If any chart value is customized during upgrade through the
> "--set" option of the "helm upgrade" command, all other previously customized
> values will be replaced with the ones included in the new version of the chart.
> To ensure that any customized values are carried forward as part of the
> upgrade, consider keeping a versioned list of such values. The versioned list should be
> provided as input to the upgrade command to use the "--set"
> option without side effects.

Upgrade CSAC using the command:

```text
helm upgrade <RELEASE_NAME> <CHART_REFERENCE> --namespace <NAMESPACE> [--set <other_parameters>]
```

*For Example:*
```text
helm upgrade csac https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm/eric-oss-core-slice-assurance-cfg/eric-oss-core-slice-assurance-cfg-1.214.0-1.tgz --namespace test-deployment-namespace --set appArmorProfile.type=unconfined,global.pullSecret=k8s-registry
```

## Security Guidelines

### Operative Tasks

This service does not include any operative tasks.

### External Ports

No ports are exposed to the outside of the cluster.

### Internal Ports

The following are cluster internal ports that are not exposed outside the cluster. The port number can be changed via the configuration parameter `service.port`.
See the [Configuration Parameters](#configuration-parameters) section.

| Service or Interface name | Protocol | IP Address Type | Default Port | Transport Protocol | IP Version    |
|---------------------------|----------|-----------------|--------------|--------------------|---------------|
| IF.OSS_AIR.CSAC.CFG       | HTTP     | Hostname        | 8080         | TCP                | IPv4 and IPv6 |
| IF.OSS_AIR.CSAC.CFG       | HTTPS    | Hostname        | 8443         | TCP                | IPv4 and IPv6 |


### Certificates

Refer to [Certificate Management](#certificate-management)

### Security Events That Can Be Logged

This service generates log events for

- startup, shutdown and restart of the service.
- on certificate loading and reloading.
- on SSL context generation.
- on connection failures between software components.

| Event Type                                                               | Log Level | Event Log                                                                             |
|--------------------------------------------------------------------------|-----------|---------------------------------------------------------------------------------------|
| Service Starting                                                         | Info      | CSAC starting                                                                         |
| Service Shutting Down                                                    | Info      | CSAC shutting down                                                                    |
| Certificate Loading Starting                                             | Warn      | Subscribing to certificate changes in {directory}                                     |
| Certificate Change Detected                                              | Warn      | Certificate change detected for {certificate name}, content: {store type}             |
| Certificate Loading Started                                              | Warn      | Loading new certificates from {directory}                                             |
| Certificate Loading Started                                              | Warn      | New SSL context set                                                                   |
| Certificate Reloading Starting                                           | Warn      | Refreshing SSL context                                                                |
| Certificate Reloading Started                                            | Warn      | New SSL context set                                                                   |
| Certificate Reloading Started                                            | Warn      | Reloaded REST clients with new SSL context                                            |
| Certificate Reloading Started                                            | Warn      | Reloaded server with new SSL context                                                  |
| Certificate Reloading Completed                                          | Warn      | Completed SSL context refresh                                                         |
| Failed to Refresh SSL Context                                            | Warn      | Cannot refresh server SSL context: {exception}                                        |
| Failure during SSL Context Creation                                      | Warn      | Cannot create SSL context: {exception}                                                |
| Failure during SSL Context Creation                                      | Warn      | Cannot create CSAC's keystore/truststore: {exception}                                 |
| Failure during SSL Context Creation                                      | Warn      | New SSL context is empty                                                              |
| Fatal Connectivity Failure between CSAC and Downstream REST Server       | Error     | Fatal error. {end point}: {HTTP status code} {HTTP status reason phrase}              |
| Recoverable Connectivity Failure between CSAC and Downstream REST Server | Error     | {end point}: {HTTP status code} {HTTP status reason phrase}                           |
| Connectivity Failure between CSAC and Downstream REST Server             | Error     | Unmatched error response. {end point}: {HTTP status code} {HTTP status reason phrase} |
| Retry Attempt Failure between CSAC and Downstream REST Server            | Error     | Attempt {number} failed: {exception message}. Retrying.                               |
| Retry Attempt Successful between CSAC and Downstream REST Server         | Info      | Attempt {number} successful for previous exception: {exception message}               |
| Retry Attempts Exhausted between CSAC and Downstream REST Server         | Error     | Retries exhausted after {number} attempts. Cause: {exception}                         |
| Connectivity Failure between CSAC and EDB                                | Error     | Flyway migration failed: {exception message}                                          |
| Connectivity Failure between CSAC and EDB                                | Error     | Baselining migrations failed: {exception message}                                     |
| Connectivity Failure between CSAC and EDB                                | Error     | Cannot connect to database: {exception}                                               |

*Note: These errors will not cause CSAC to crash. However, if any of these errors are logged, then CSAC may not function properly.*

## Privacy User Guidelines

The microservice does not collect, store or process personal data on its own.

## Operation and Maintenance

### Performance Management

#### Custom Metrics

Following table lists the description for all custom metrics.

| Metric                                                    | Description                                                                                                                                                                                                       |
|-----------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| csac_pm_defs_dict_int_total                               | A count of the PM Definitions persisted in the internal CSAC store                                                                                                                                                |
| csac_kpi_defs_dict_int_total                              | A count of the KPI Definitions persisted in the internal CSAC store                                                                                                                                               |
| csac_deployed_profile_defs_int_total                      | A count of the deployed profile definitions in the runtime data resource                                                                                                                                          |
| csac_deployed_index_instances_int_total                   | A count of the deployed indexes persisted in the runtime data store                                                                                                                                               |
| csac_deployed_kpi_instances_int_total                     | A count of the deployed KPI definitions in the runtime data resource                                                                                                                                              |
| csac_provisioning_aas_time_seconds                        | Time taken for CSAC to complete provisioning of the AAS. Time measured from start of provisioning. |
| csac_provisioning_index_time_seconds                      | Time taken for CSAC to complete provisioning of the AIS. Time measured from start of provisioning. |
| csac_provisioning_pmsc_time_seconds                       | Time taken for CSAC to complete provisioning of the PMSC. Time measured from start of provisioning.|
| csac_provisioning_kpi_time_seconds                        | Time taken for CSAC to complete provisioning of KPIs. Time measured from start of provisioning.    |
| csac_provisioning_total_time_seconds                      | Time taken for CSAC to complete provisioning of all target services. Time measured from start of provisioning. Note: This time is not the cumulative time taken for individual provisioning operations. |
| csac_configuration_reset_db_time_seconds                  | Time taken to reset the CSAC dictionary and runtime configuration in the CSAC datastore                                                                                                                           |
| csac_configuration_reset_kpi_time_seconds                 | Time taken to reset the runtime KPIs created by CSAC                                                                            |
| csac_configuration_reset_augmentation_time_seconds        | Time taken to reset augmentation configuration in the Assurance Augmentation Service            |
| configuration_reset_index_time_seconds        | Time taken to reset index configuration in the Assurance Indexing Service            |
| csac_configuration_reset_total_time_seconds               | Total time taken to complete all configuration operations. Time measured from the start of the reset operation. |
| csac_runtime_augmentation_errors_total                    | A count of the augmentation provisioning failures for AAS (Currently, we only support 0 for no error and 1 for submission failure)                                                                                |
| csac_runtime_index_instance_errors_total                  | A count of the index provisioning failures for AIS (Currently, we only support 0 for no error and 1 for submission failure)                                                                                       |
| csac_runtime_kpi_instance_errors_total                    | A count of the total number of runtime KPI definitions that failed to be provisioned in PMSC (Currently, we only support 0 for no error and 1 for submission failure)                                             |
| csac_file_load_errors_total                               | A count of OOB resource JSON files that failed to be loaded (Currently, we only support 0 for no error and 1 for file loading errors)                                                                             |
| csac_dictionary_pm_definition_errors_total                | A count of the dictionary PM definition validation errors (Currently, we only support 0 for no error and 1 for PM definition validation errors)                                                                   |
| csac_dictionary_kpi_definition_errors_total               | A count of the dictionary KPI definition validation errors (Currently, we only support 0 for no error and 1 for KPI definition validation errors )                                                                |
| csac_augmentation_defs_dict_int_total                     | A count of the Augmentation Definitions persisted in the internal CSAC store                                                                                                                                      |
| csac_deployed_augmentation_defs_int_total                 | A count of the deployed Augmentation Definitions persisted in the runtime data store                                                                                                                              |
| csac_runtime_configuration_consistency_check_errors_total | A count of the identified runtime configuration inconsistencies                                                                                                                                                   |
| csac_configuration_reset_db_errors_total                  | A count of the number of errors encountered during CSAC data store reset operations. This count is cumulative for consecutive reset failures. The error count will be reset to 0 if the reset operation succeeds. |
| csac_configuration_reset_kpi_errors_total                 | A count of the number of errors encountered during KPI reset operations. This count is cumulative for consecutive reset failures. The error count will be reset to 0 if the reset operation succeeds.             |
| csac_configuration_reset_augmentation_errors_total        | A count of the number of errors encountered during augmentation reset operations.  This count is cumulative for consecutive reset failures. The error count will be reset to 0 if the reset operation succeeds.   |
| configuration_reset_index_errors_total        | A count of the number of errors encountered during index reset operations.  This count is cumulative for consecutive reset failures. The error count will be reset to 0 if the reset operation succeeds.   |
| csac_configuration_reset_errors_total | A count of the total number of errors encountered during configuration reset operations. This count is cumulative for consecutive reset failures.  The error count will be reset to 0 if the reset operation succeeds. |


<!--
#### KPIs

<*This section is only applicable for stateful services*
*and can be removed if not used.*>

The table below lists the KPIs that are essential to use in order to properly
monitor the service. These KPIs are used to monitor symptomatic conditions that
can lead to service downtime or significant service degradation. Depending on
context and needs, these KPIs can be used for visualization, triggering
alarms/alerts or remedial actions.

| KPI name      | KPI short description    | KPI specification          | KPI usage |
| ------------ | -------------------- | ------------ |------|
| Persistent volume usage ratio  | Ratio of used bytes over total capacity in bytes in a volume | kubelet_volume_stats_used_bytes\/ kubelet_volume_stats_capacity_bytes*100       | Identify lack of space in filesystem |
| <*Cluster status OR Percentage of down instances OR equivalent*>  | <*short_description*> | <*PromQL_query_expression*>        | <*usage_description*> |
| <*kpi*>   | <*short_description*> | <*PromQL_query_expression*>       |<*usage_description*> |
| ...          | ...                  | ...          |...          |

<*Instructions: The list of KPIs is service specific.*
*However the following KPIs must always be included in the table:*

 - *Persistent volume usage ratio: the entire row SHALL be reused as is.*
 - *&lt;*Cluster status OR Percentage of down instances OR equivalent*&gt;:
*this row SHALL always be present but the KPI name, short description,*
*specification and usage is service specific.*>
-->

### Scaling

Following are the instructions to scale-in and scale-out CSAC service.

- Scale-in:<br/>
  1. The service is deployed with variable replicaCount=2<br/>
     `ex: helm install csac --set appArmorProfile.type=unconfined,global.pullSecret=k8s-registry,replicaCount=2 .`
  2. Execute following command to scale-in CSAC to run 1 replica.<br/>
     `kubectl scale deployment eric-oss-core-slice-assurance-cfg --replicas=1`

- Scale-out:<br/>
  1. The service is deployed with variable replicaCount=1<br/>
     `ex: helm install csac --set appArmorProfile.type=unconfined,global.pullSecret=k8s-registry,replicaCount=1 .`
  2. Execute following command to scale-out CSAC to run 2 replicas.<br/>
     `kubectl scale deployment eric-oss-core-slice-assurance-cfg --replicas=2`

## Troubleshooting

This section describes the troubleshooting functions and procedures for CSAC.

### Prerequisites

#### User

The user performing CSAC troubleshooting must have

- access to the Kubernetes cluster and namespace where CSAC is deployed
- basic knowledge of Linux operations
- basic knowledge of Kubernetes command-line operations using `kubectl`

### Troubleshooting Functions

#### Data Collection

##### CSAC Pod Information

Detailed information about CSAC pods is collected using the `kubectl describe pod` command.  For example

```text
kubectl describe pod <pod_name> --namespace=<namespace>
kubectl exec <pod_name> --namespace=<namespace> -- env
```

#### Health Check

To check the health of a CSAC pod, use the `/actuator/health` end point and examine the "status" field.  If the status is not "UP", the status of internal CSAC components is indicated in the response body. For example,

```bash
$ curl -X GET 'http://localhost:8080/actuator/health'
{"status":"UP","components":{"db":{"status":"UP","details":{"database":"PostgreSQL","validationQuery":"isValid()"}},"discoveryComposite":{"description":"Discovery Client not initialized","status":"UNKNOWN","components":{"discoveryClient":{"description":"Discovery Client not initialized","status":"UNKNOWN"}}},"diskSpace":{"status":"UP","details":{"total":62671097856,"free":39004131328,"threshold":10485760,"path":"/.","exists":true}},"healthCheck":{"status":"UP"},"kubernetes":{"status":"UP","details":{"nodeName":"docker-desktop","podIp":"10.1.2.130","hostIp":"192.168.65.4","namespace":"test","podName":"eric-oss-core-slice-assurance-cfg-85cc4966d-vq68n","serviceAccount":"eric-oss-core-slice-assurance-cfg","inside":true,"labels":{"app.kubernetes.io/instance":"csac","app.kubernetes.io/managed-by":"Helm","app.kubernetes.io/name":"eric-oss-core-slice-assurance-cfg","app.kubernetes.io/version":"1.0","helm.sh/chart":"eric-oss-core-slice-assurance-cfg-1.257.0-SNAPSHOT","pod-template-hash":"85cc4966d"}}},"livenessState":{"status":"UP"},"ping":{"status":"UP"},"readinessState":{"status":"UP"},"refreshScope":{"status":"UP"}},"groups":["liveness","readiness"]}
```

#### Alarms

CSAC does not raise any alarms at this time.

#### Counters

Metrics provide counts of successfully provisioned resources as well as error counts for different provisioning operations and timing metrics indicating the elapsed time for each operation.

Custom metrics provided by CSAC are described in [Custom Metrics](#custom-metrics), above.

#### Accessing the CSAC REST API

Invoking the CSAC REST service provides access to pod status information, metrics, configuration, and REST end points.  To access the CSAC REST service outside the Kubernetes cluster, the CSAC service port must first be exported either from a single pod or the CSAC service.  For example,

```bash

# forward requests from the localhost to a single pod on port 8080. Requires getting the pod Id via 'kubectl get pods'.
$ kubectl -n namespace port-forward eric-oss-core-slice-assurance-cfg-85cc4966d-vq68n 8080:8080 &
$ Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080

# forward requests from the local host to the CSAC service on port 8080
$ kubectl -n namespace port-forward service/eric-oss-core-slice-assurance-cfg 8080:8080 &
$ Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
```

The CSAC REST API can then be queried from the localhost using `curl`.  For example,

```bash
$ curl -X GET 'http://localhost:8080/v1/dictionary/pmdefs'
```

The CSAC REST API is described in the CSAC API documentation.

##### Accessing CSAC Metrics

All CSAC metrics are available as Prometheus metrics using the ADP PM Server API.

Alternatively, CSAC metrics can be accessed directly from a CSAC service or pod using either the `/actuator/metrics` end point or `/actuator/prometheus` end point.  For example,

```bash
# retrieve the list of all available metrics
$ curl -X GET 'http://localhost:8080/actuator/metrics'
{"names":["csac.application.ready.time", ...]}

# retrieve a single metric with its value
$ curl -X GET 'http://localhost:8080/actuator/metrics/csac.application.ready.time'
{"name":"csac.application.ready.time","description":"Time taken for the application to be ready to service requests","baseUnit":"seconds","measurements":[{"statistic":"VALUE","value":43.179}],"availableTags":[{"tag":"main.application.class","values":["com.ericsson.oss.air.CoreApplication"]}]}
```

See also [Accessing CSAC Metrics](#accessing-csac-metrics).

#### Logging

If the log streaming method is set to "direct" or "dual", the logs will be stored by the Log Transformer service. 
By default, the Log Transformer service is configured to store log events in the Search Engine.
In the Search Engine, the logs will be indexed in an index named adp-app-logs-YYYY.MM.DD.hh according to the UTC time zone. 
See [Log Transformer service](https://adp.ericsson.se/marketplace/log-transformer) for more information.

Alternatively, if the log streaming method is either "dual" or "indirect", CSAC logs can be accessed from a single CSAC pod using the `kubectl logs` command.  For example,

```bash
# dump the logs for a CSAC pod.  Requires getting the pod Id via 'kubectl get pods'.
$ kubectl -n namespace logs eric-oss-core-slice-assurance-cfg-85cc4966d-vq68n
```

#### Change Log Level

Log level can be set dynamically during run-time by modifying a configuration file made available to the service by a Kubernetes ConfigMap. The json file contains the name of the container and the desired log level which can be given as lower case. The log level can be set to debug as follows:

`kubectl get configmaps eric-oss-core-slice-assurance-cfg-log-config -n <namespace> -o yaml | sed -E 's/severity": .*/severity": "debug"/' | kubectl replace -f -`

Only enable debug logging in order to troubleshoot a problem that can be reproduced. Debug logging may impact performance.

<!--
### Log Categories

Log Categories are used to support automatic filtering which enable a
possibility to support AI and machine learning. In the table below the log
categories provided by the service are listed.

| Category Name     | Security Log | Description              |
| ----------------- | -------------| ------------------------ |
| <*category_name*> | <*yes_no*>   | <*category_description*> |
| ... | ... | ... |

<*Instructions:   The Category Name must follow the [Log General Design
Rules](https://confluence.lmera.ericsson.se/display/AA/LOG+General+Design+Rules)
and therefore include the short name of the functional area and nature of the
logs in the category. The Functional Area short name to be used are the
Functional Area Acronyms listed in the [ADP FA
Invetory](https://confluence.lmera.ericsson.se/display/AA/FA+Inventory). Each
microservices log categories includes The combination of the FA short name and
the nature of the log category shall be separated by dash.   Example of category
names: IAM-token-generation, KM-genkey-issuecert.*>
-->

### Troubleshooting Procedures

#### CSAC Fails To Start

If CSAC does not install properly and fails to start on deployment, collect the logs and restart the deployment.

1. Check the deployment status as described in [CSAC Pod Information](#csac-pod-information).
1. Collect the logs by following the steps in the [Troubleshooting Procedures: Logging](#logging) section.

__Corrective Action__

When the root cause of the deployment failure has been diagnosed and corrected, restart the deployment as follows:

```bash
$ kubectl -n <namespace> rollout restart deployment <deployment_name> 
```

Result: The deployment is restarted and functions properly.

If the CSAC still fails to start, follow the steps in the [Bug Reporting and
Additional Support](#bug-reporting-and-additional-support) section.

#### CSAC Provisioning Fails

After successful deployment, CSAC provisioning may fail due to configuration or connection-related problems with any of its external dependencies. Provisioning failures may result from

1. Connectivity issues with external dependencies
1. CSAC service configuration
1. CSAC resource configuration

If provisioning fails, the CSAC log will indicate a fatal error with a brief description of the failure.  If more logging detail is needed, the CSAC log level can be lowered to `DEBUG` as described in [Change Log Level](#change-log-level) and CSAC restarted.

##### Connectivity Problem - TLS Handshake Failure

Connectivity problems may result from missing, invalid, or expired TLS certificates.  The following examples show log messages related to TLS connection problems:

- CSAC failed to load certificates and/or properly set up its SSL context:

```text
Fatal error. Service restart may be required. I/O error on PUT request for "<PMSC URL>/son-om/kpi/v1/kpis/definitions": PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target; nested exception is javax.net.ssl.SSLHandshakeException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```

- CSAC failed to add certificates to its KeyStore:

```text
Fatal error. Service restart may be required. I/O error on PUT request for "<PMSC URL>/son-om/kpi/v1/kpis/definitions": Received fatal alert: bad_certificate; nested exception is javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate
```

- CSAC failed to add certificaters to its TrustStore:

```text
Fatal error. Service restart may be required. I/O error on PUT request for "<PMSC URL>/son-om/kpi/v1/kpis/definitions": Unexpected error: java.security.InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty; nested exception is javax.net.ssl.SSLException: Unexpected error: java.security.InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty
```

__Corrective Action__

- Check the health of SIP-TLS and its dependent services. If any of the services are unhealthy, consult their documentation.

##### Connectivity Problem - Service Configuration

Connection-related configuration properties are defined in the CSAC service configuration.  To access the configuration, retrieve the CSAC configuration ConfigMap using `kubectl -n <namespace> get configmap <configmap name>`.  For example,

```bash
$ kubectl -n namespace get configmap eric-oss-core-slice-assurance-cfg-application-config -o yaml
```

__Corrective Action__

1. Locate the target service configuration in the CSAC application config.
1. Ensure that the target service is running and healthy.
1. Ensure that the CSAC configuration for the target service matches the target service deploymment.
1. Make any changes necessary to the CSAC configuration and redeploy/restart CSAC.

#### Resource Configuration Problem - Resources Fail To Load Or Are Invalid

Resource configuration problems should be reported immediately if provisioning fails and the CSAC log indicates that resource configuration could not be loaded or was determined to be invalid during provisioning.  See [Bug Reporting And Additional Support](#bug-reporting-and-additional-support).

#### Resource Configuration Problem - Partial or Inconsistent Configuration

CSAC provisions all target services in a single operation.  It is possible that provisioning for one of the target services did not complete successfully because of a connectivity or database-related problem.  For example, augmentation and KPI provisioning succeeded but AIS provisioning failed because of an internal issue in CSAC or a problem with AIS.

__Corrective Action__

The CSAC log will indicate the nature of the error.  If the error can be corrected without redeployment, e.g. connectivity problem with the CSAC database or the affected target service, the following steps may result in successful provisioning:

1. Find the root cause of the failure in the CSAC log.
1. Correct the condition that caused the failure.
1. Reset the Assurance configuration using the [CSAC resource actuator](#csac-resource-actuator---reset).
1. Reload the Assurance configuration using the [CSAC resource actuator](#csac-resource-actuator---reload).

##### CSAC Resource Actuator - Reset

The resource actuator will reset all Assurance configuration in CSAC and all target services.  To succeed, the CSAC database must be accessible and healthy, and all target services must be accessible and healthy.  To reset Assurance configuration, invoke the actuator as follows:

```bash
$ curl -X DELETE 'http://localhost:8080/actuator/resource/reset'
```

> NOTE: the reset operation is idempotent and re-entrant. If a failure occurs, the operation can be repeated once the underlying condition is corrected.  Errors are indicated in the CSAC log.

##### CSAC Resource Actuator - Reload

The resource actuator will reload all Assurance resources and perform all provisioning operations.  To reload the Assurance resources, invoke the actuator as follows:

```bash
$ curl -X POST 'http://localhost:8080/actuator/resource/reload'
```

> NOTE: reloading resources performs the same operations as done during initial CSAC deployment. Reload should only be attempted after a successful reset.  See [CSAC Resource Actuator - Reset](#csac-resource-actuator---reset).

### Bug Reporting and Additional Support

Issues can be handled in different ways, as listed below:

- For questions, support or hot requesting, see [Additional Support](#additional-support).

- For reporting of faults, see [Bug Reporting](#bug-reporting).

#### Additional Support

If there are CSAC support issues, use the [Team Swordform page](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/AAP/Team+Swordform) and follow the [Assurance Bugs Handling page](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?spaceKey=AAP&title=Assurance+Bugs+Handling) except use 'Support' for the Issue Type field.

#### Bug Reporting

If there is a suspected fault, report a bug. The bug report must
contain specific CSAC Service information and all
applicable troubleshooting information highlighted in the
[Troubleshooting](#troubleshooting), and [Data Collection](#data-collection).

Indicate if the suspected fault can be resolved by restarting the pod and if any of CSAC's dependent services are unhealthy.

Use the [Team Swordform page](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/AAP/Team+Swordform) and follow the [Assurance Bugs Handling page](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?spaceKey=AAP&title=Assurance+Bugs+Handling)  to report bugs.


<!--
### Recovery Procedure

This section describes how to recover the service in case of malfunction.

<*Create a subsection for any possible recovery procedure that can be applied
to recover the service. Restarting the pod and Data recovery reported below
are some common scenarios, that might not be applicable for all services*>

#### Restarting the Pod

<*Describe how to restart pods if that could resolve problem.*>

#### Data Recovery

<*Describe the recovery procedure. For example, how to recover a data
service using a restore procedure.*>

### KPIs Troubleshooting

<*This section is only applicable for stateful services.*>

#### <*Title of the problem*>

##### Description

<*Description of the problem.*>

##### Procedures for Possible Fault Reasons and Solutions

###### <*Title of handled fault reason \#1*>

Prerequisites

- <*Prerequisite for starting the procedure*>

Steps

1. <*First step of action*>
2. <*Second step of action*>
3. ...

###### <*Title of handled fault reason \#2*>

Prerequisites

- <*Prerequisite for starting the procedure*>

Steps

1. <*First step of action*>
2. <*Second step of action*>
3. ...

...

### Alarm Handling

<*Provide a list of the possible alarms the service can raise and refer to the
dedicated OPI for problem resolution.<br/>
Omit this section if the service doesn't raise any alarm.*>

### Known Issues

<*When applicable, this section shall list the most common problems that
can occur and the instructions to avoid them*>
-->

## References


- [ESOA JIRA](https://eteamproject.internal.ericsson.com/projects/ESOA)
- [Service Identity Provider TLS User Guide](https://adp.ericsson.se/marketplace/service-identity-provider-tls/documentation)
- [DMM repository](https://gerrit-gamma.gic.ericsson.se/#/admin/projects/OSS/com.ericsson.oss.dmi/eric-oss-dmm)
- [Data Catalog User Guide](https://adp.ericsson.se/marketplace/data-catalog/documentation)
- [Schema Registry User Guide](https://adp.ericsson.se/marketplace/schema-registry-sr/documentation)
- [Performance Management (PM) Server User Guide](https://adp.ericsson.se/marketplace/pm-server/documentation)
- [Log Transformer User Guide](https://adp.ericsson.se/marketplace/log-transformer/documentation)
- [Search Engine](https://adp.ericsson.se/marketplace/search-engine/documentation/development/dpi/service-user-guide)
- [Logging General Design Rules](https://eteamspace.internal.ericsson.com/display/AA/LOG+general+design+rules#LOGgeneraldesignrules-DR-D470222-010)
- [Section 4.1.1. Identifiers and Key Words in Postgresql Documentation](https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS)
- [PM Stats Calculator User Guide](https://adp.ericsson.se/marketplace/pm-stats-calculator/documentation)
- [Assurance Augmentation Service User Guide](https://adp.ericsson.se/marketplace/assurance-augmentation/documentation)
- [Core Analytics Reporting Dimensioning Query User Guide](https://adp.ericsson.se/marketplace/core-analytics-reporting-dimensioning-qu/documentation)
- [Assurance Indexer User Guide](https://adp.ericsson.se/marketplace/assurance-indexer/documentation)

# Core Slice Assurance Configurator (CSAC)

The Core Slice Assurance Configurator's (CSAC) primary purpose is to provision all down stream systems for instantiating, calculating and visualizing Key Performance Indicators (KPIs).

It is a Java Spring Boot application created from the Microservice Chassis.

## Contact Information

#### Team Members

##### CSAC

[Team Swordform](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/Team+Swordform) is currently the acting development team working on CSAC.
For support, please contact Team Swordform <a href="mailto:PDLPDLSWOR@pdl.internal.ericsson.com">PDLPDLSWOR@pdl.internal.ericsson.com</a>.

##### CI Pipeline

The CI Pipeline aspect of this Microservice is now owned, developed and maintained by [Team Hummingbirds](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/ACE/Hummingbirds+Home) in the DE (Development Environment) department of PDU OSS.

#### Email

Guardians for this project can be reached at Team Swordform <a href="mailto:PDLPDLSWOR@pdl.internal.ericsson.com">PDLPDLSWOR@pdl.internal.ericsson.com</a>.

## Maven Dependencies

This microservice has the following Maven dependencies:

- Spring Boot Start Parent version 2.5.12.
- Spring Boot Starter Web.
- Spring Boot Actuator.
- Spring Cloud Sleuth.
- Spring Boot Starter Test.
- JaCoCo Code Coverage Plugin.
- Sonar Maven Plugin.
- Spotify Dockerfile Maven Plugin.
- Common Logging utility for logback created by Vortex team.
- Flyway core
- PostgreSQL jdbc driver
- Properties for spring cloud version and java are as follows.

```
<version.spring-cloud>2020.0.3</version.spring-cloud>
```

## Build and run CSAC locally

### Local set up instructions

1. Follow the instructions listed on https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/IDUN/IDE+setup+guide+for+ADP-compliant+code+style and
   https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?spaceKey=DGBase&title=Common+OSS+Artifactory+Repositories to set up your local development environment.
2. Ensure that Java 11 is installed.

### Run CSAC via Command-line

1. In your terminal, switch to the root directory of where you cloned CSAC, e.g. `~/gerrit/eric-oss-core-slice-assurance-cfg`
2. Run `mvn clean install -DskipTests=true` to build CSAC.
3. Render chart template locally and display the output for _application-prod.yaml_ with command `helm template charts/eric-oss-core-slice-assurance-cfg/`
4. Copy and paste the output of _application-prod.yaml_ into a local config directory, e.g. `cp ./charts/eric-oss-core-slice-assurance-cfg/config/application-prod.yaml ~/config/csac`
5. Modify your version of application-prod.yaml as needed, e.g. csac.resource's value from `/config/csac-oob-kpi-defs.json` to `/Users/<SIGNUM>/config/csac/csac-oob-kpi-defs.json`
6. Run docker-compose file in `./dev` folder. Follow the instructions in `./dev/README.md`
7. Run `java -jar target/eric-oss-core-slice-assurance-cfg-<BUILT-VERSION-NUMBER>-SNAPSHOT.jar --spring.config.additional-location=file:///<YOUR-PATH-TO-THE-CONFIG>/application-prod.yaml --spring.profiles.active=prod` to start CSAC. e.g. `java -jar target/eric-oss-core-slice-assurance-cfg-1.6.0-SNAPSHOT.jar --spring.config.additional-location=file:///Users/<SIGNUM>/config/csac/application-prod.yaml --spring.profiles.active=prod`

### Run CSAC via Eclipse

1. Import CSAC as an existing Maven project into Eclipse.
2. Install Lombok in your Eclipse by first navigating to `~/.m2/repository/org/projectlombok/lombok/<VERSION>` in your terminal.
3. Execute `java -jar lombok-<VERSION>.jar`
4. Follow the instructions on the pop-up window and restart Eclipse.
5. Right-click on com.ericsson.oss.air.CoreApplication.java and select 'Run As' > 'Run Configurations'.
6. In the `Run Configurations` pop-up, switch to the Arguments tab. Input `--spring.config.additional-location=file:///<YOUR-PATH-TO-THE-CONFIG>/application-prod.yaml --spring.profiles.active=prod` into the 'Program Arguments' text box.
7. Click 'Apply' and then 'Run'

### Run CSAC via IntelliJ

To be written

## Build related artifacts

The main build tool is BOB provided by ADP. For convenience, maven wrapper is provided to allow the developer to build in an isolated workstation that does not have access to ADP.

- [ruleset2.0.yaml](ruleset2.0.yaml) - for more details on BOB please see [Bob 2.0 User Guide](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md).
  You can also see an example of Bob usage in a Maven project in [BOB](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Adopting+BOB+Into+the+MVP+Project).
- [precoderview.Jenkinsfile](precodereview.Jenkinsfile) - for pre code review Jenkins pipeline that runs when patch set is pushed.
- [publish.Jenkinsfile](publish.Jenkinsfile) - for publish Jenkins pipeline that runs after patch set is merged to master.
- [.bob.env](.bob.env) - if you are running Bob for the first time this file will not be available on your machine.
  For more details on how to set it up please see [Bob 2.0 User Guide](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md).

If the developer wishes to manually build the application in the local workstation, the `bob clean init-dev build image package-local` command can be used once BOB is configured in the workstation.
Note: The `mvn clean install` command will be required before running the bob command above.
See the "Containerization and Deployment to Kubernetes cluster" section for more details on deploying the built application.

Stub jar files are necessary to allow contract tests to run. The stub jars are stored in JFrog (Artifactory).
To allow the contract test to access and retrieve the stub jars, the .bob.env file must be configured as follows.

```
SELI_ARTIFACTORY_REPO_USER=<LAN user id>
SELI_ARTIFACTORY_REPO_PASS=<JFrog encripted LAN PWD or API key>
HOME=<path containing .m2, e.g. /c/Users/<user>/>
```

To retrieve an encrypted LAN password or API key, login to [JFrog](https://arm.seli.gic.ericsson.se) and select "Edit Profile".
For info in setting the .bob.env file see [Bob 2.0 User Guide](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md).

## Containerization and Deployment to Kubernetes cluster.

Following artifacts contains information related to building a container and enabling deployment to a Kubernetes cluster:

- [charts](charts/) folder - used by BOB to lint, package and upload helm chart to helm repository.
  - Once the project is built in the local workstation using the `bob clean init-dev build image package-local` command, a packaged helm chart is available in the folder `.bob/eric-oss-core-slice-assurance-cfg-internal/` folder.
    This chart can be manually installed in Kubernetes using `helm install` command. [P.S. required only for Manual deployment from local workstation]
- [Dockerfile](Dockerfile) - used by Spotify dockerfile maven plugin to build docker image.
  - The base image for the chassis application is `sles-jdk8` available in `armdocker.rnd.ericsson.se`.

### Deploy CSAC in local k8s cluster

1. Download and install [Rancher Desktop](https://docs.rancherdesktop.io/getting-started/installation/) application.
2. Make sure you select `containerd` option in `General > Settings > Container Engine` tab.
3. Open a new terminal and set the Kubernetes context to `rancher-desktop`
   ```
   kubectl config use-context rancher-desktop
   ```
4. Create namespace
   ```
   kubectl create ns test
   ```
5. Login to ARM docker registry. Use your ECN/local machine password for login.
   ```
   nerdctl login armdocker.rnd.ericsson.se --username <SIGNUM>
   ```
6. Create a docker-registry secret that will be used for pulling the images.
   ```
   kubectl create secret docker-registry k8s-registry --docker-server=armdocker.rnd.ericsson.se --docker-username=<SIGNUM> --docker-password=<PASSWORD> --docker-email=<XXXXXXX@ericsson.com> --namespace=test
   ```
7. Install ADP document Database(DDB).

   ```shell
   #Add helm repository to pull Document Database chart.
   helm repo add adp https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm --username <SIGNUM>
   helm repo update

   #Create postgresql user credential secret.
   kubectl create secret generic eric-oss-core-slice-assurance-cfg-db-secret --from-literal=pguserid=csac --from-literal=pgpasswd=custompwd --from-literal=super-pwd=superpwd --from-literal=super-user=postgres --from-literal=metrics-pwd=metricspwd --from-literal=replica-user=replicauser --from-literal=replica-pwd=replicapwd --namespace=test

   #Install Document Database.
   helm install csac-ddb adp/eric-data-document-database-pg --version 8.8.0+31 --namespace test --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-core-slice-assurance-cfg-db-secret,postgresDatabase=csacdb,nameOverride=eric-oss-core-slice-assurance-cfg-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd,credentials.keyForSuperPw=super-pwd
   
   # DDB can be installed with the Backup and Restore Orchestrator (BRO) enabled. This requires setting the `brAgent.enabled` parameter to `true` (the default is `false`). Below are examples of installing DDB with BRO enabled.
   # option 1. To install application level BRO that only our agents can connect to (own BRO)
   helm install csac-ddb adp/eric-data-document-database-pg --version <DDB_VERSION> --namespace <DDB_NAMESPACE> --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=csacdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd,brAgent.enabled=true,security.tls.brAgent.enabled=false,global.adpBR.brLabelKey=<GLOBAL-BR-LABEL-KEY>,brAgent.brLabelValue=<BR_AGENT_LABEL>
   
   # option 2. To install application level BRO that only our agents can connect to (own BRO)
   helm install csac-ddb adp/eric-data-document-database-pg --version <DDB_VERSION> --namespace <DDB_NAMESPACE> --set global.pullSecret=k8s-registry,global.security.tls.enabled=false,credentials.kubernetesSecretName=eric-oss-assurance-augmentation-db-secret,postgresDatabase=csacdb,nameOverride=eric-oss-assurance-augmentation-db,credentials.keyForUserId=pguserid,credentials.keyForUserPw=pgpasswd,brAgent.enabled=true,security.tls.brAgent.enabled=false

   ```
   For information related to DDB installation,
   see [Document Database PG Service User Guide.](https://adp.ericsson.se/marketplace/document-database-pg/documentation/8.8.0/dpi/service-user-guide)

   For information related to Backup and Restore Orchestrator (BRO) installation
   see [Backup and Restore Orchestrator Service Deployment Guide](https://adp.ericsson.se/marketplace/backup-and-restore-orchestrator/documentation/development/dpi/service-user-guide#deployment)

   > If you want to install the released chart of CSAC, please run the following command. Alternatively, you can follow steps 8 to 10 to build CSAC using the bob command provided below.

   ```shell
   helm repo add drop https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm --username <SIGNUM>
   helm repo update

   # Install CSAC using version 1.236.0-1. If you remove the --version flag, it will install the latest build.
   helm install csac drop/eric-oss-core-slice-assurance-cfg --version 1.236.0-1 --devel --namespace test --set appArmorProfile.type=unconfined,log.streamingMethod=indirect,global.pullSecret=k8s-registry --set global.security.tls.enabled=false

   ```

8. Build the CSAC project in bob vm.

   ```
   cd ~/gerrit/eric-oss-core-slice-assurance-cfg

   bob clean init-dev build image package-local
   ```

   To build an aarch64 image for Apple Silicon:

   ```
   cd ~/gerrit/eric-oss-core-slice-assurance-cfg

   bob clean init-dev build image-arm64 package-local
   ```

9. Copy the CSAC docker image built from bob vm to local environment.

   ```
   #Save docker image as tar file. This command needs to be run inside bob vm.
   docker save armdocker.rnd.ericsson.se/proj-eric-oss-dev/eric-oss-core-slice-assurance-cfg > csac-image.tar

   #Load the saved docker image to local environment. This command needs to be run outside bob vm(in local env).
   nerdctl --namespace=k8s.io load --input csac-image.tar
   ```

10. Install CSAC.

    ```
    # Untar the packaged helm chart in your project's .bob/eric-oss-core-slice-assurance-cfg-internal/ folder.
    tar -xvf eric-oss-core-slice-assurance-cfg.tar
    cd eric-oss-core-slice-assurance-cfg

    # Helm install with default logging
    helm install csac -n test --set appArmorProfile.type=unconfined,global.pullSecret=k8s-registry .

    # Helm install with indrect (stdout) logging
    helm install csac -n test --set appArmorProfile.type=unconfined,log.streamingMethod=indirect,global.pullSecret=k8s-registry --set global.security.tls.enabled=false .


    # Helm install with alternative data base vender (for example EDB installed to a separate namespace 'test-edb-install')
    helm install csac -n test --set appArmorProfile.type=unconfined,log.streamingMethod=indirect,global.pullSecret=k8s-registry --set global.security.tls.enabled=false --set database.service=edb.test-edb-install.svc.cluster.local,database.port=5444 .

    ```

## Source

The [src](src/) folder of the java project contains the CSAC spring boot application source code including associated java unit tests.

## Setting up CI Pipeline

- Docker Registry is used to store and pull Docker images. At Ericsson official chart repository is maintained at the org-level JFrog Artifactory.
  Follow the link to set up a [Docker registry](https://confluence.lmera.ericsson.se/pages/viewpage.action?spaceKey=ACD&title=How+to+create+new+docker+repository+in+ARM+artifactory).
- Helm repo is a location where packaged charts can be stored and shared. The official chart repository is maintained at the org-level JFrog Artifactory.
  Follow the link to set up a [Helm repo](https://confluence.lmera.ericsson.se/display/ACD/How+to+setup+Helm+repositories+for+ADP+e2e+CICD).
- Follow instructions at [Jenkins Pipeline setup](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-JenkinsPipelinesetup)
  to use out-of-box Jenkinsfiles which comes along with eric-oss-core-slice-assurance-cfg.
- Jenkins Setup involves master and agent machines. If there is not any Jenkins master setup, follow instructions at [Jenkins Master](<https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-JenkinsMaster-2.89.2(FEMJenkins)>) - 2.89.2 (FEM Jenkins).
- Request a node from the GIC (Note: RHEL 7 GridEngine Nodes have been successfully tested).
  [Request Node](https://estart.internal.ericsson.com/).
- To setup [Jenkins Agent](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-Prerequisites)
  for Jenkins, jobs execution follow the instructions at Jenkins Agent Setup.
- The provided ruleset is designed to work in standard environments, but in case you need, you can fine tune the automatically generated ruleset to adapt to your project needs.
  Take a look at [Bob 2.0 User Guide](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md) for details about ruleset configuration.

  [Gerrit Repos](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Design+and+Development+Environment)
  [BOB](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Adopting+BOB+Into+the+MVP+Project)
  [Bob 2.0 User Guide](https://gerrit-gamma.gic.ericsson.se/plugins/gitiles/adp-cicd/bob/+/refs/heads/master/USER_GUIDE_2.0.md)
  [Docker registry](https://confluence.lmera.ericsson.se/pages/viewpage.action?spaceKey=ACD&title=How+to+create+new+docker+repository+in+ARM+artifactory)
  [Helm repo](https://confluence.lmera.ericsson.se/display/ACD/How+to+setup+Helm+repositories+for+ADP+e2e+CICD)
  [Jenkins Master](<https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-JenkinsMaster-2.89.2(FEMJenkins)>)
  [Jenkins Agent](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-Prerequisites)
  [Jenkins Pipeline setup](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-JenkinsPipelinesetup)
  [EO Common Logging](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/ESO/EO+Common+Logging+Library)
  [SLF4J](https://logging.apache.org/log4j/2.x/log4j-slf4j-impl/index.html)
  [JFrog](https://arm.seli.gic.ericsson.se)
  [Request Node](https://estart.internal.ericsson.com/)

## Using the Helm Repo API Token

The Helm Repo API Token is usually set using credentials on a given Jenkins FEM.
If the project you are developing is part of IDUN/Aeonic this will be pre-configured for you.
However, if you are developing an independent project please refer to the 'Helm Repo' section:
[Microservice Chassis xxxx CI Pipeline Guide](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/PCNCG/Microservice+Chassis+CI+Pipeline+Start+Guide#MicroserviceChassisCIPipelineStartGuide-HelmRepo)

Once the Helm Repo API Token is made available via the Jenkins job credentials the precodereview and publish Jenkins jobs will accept the credentials (ex. HELM_SELI_REPO_API_TOKEN' or 'HELM_SERO_REPO_API_TOKEN) and create a variable HELM_REPO_API_TOKEN which is then used by the other files.

Credentials refers to a user or a functional user. This user may have access to multiple Helm repos.
In the event where you want to change to a different Helm repo, that requires a different access rights, you will need to update the set credentials.

## Artifactory Set-up Explanation

The Microservice Chassis xxxx Artifactory repos (dev, ci-internal and drop) are set up following the ADP principles: [ADP Repository Principles](https://confluence.lmera.ericsson.se/pages/viewpage.action?spaceKey=AA&title=2+Repositories)

The commands: "bob init-dev build image package" will ensure that you are pushing a Docker image to:
[Docker registry - Dev](https://arm.seli.gic.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-dev/)

The Precodereview Jenkins job pushes a Docker image to:
[Docker registry - CI Internal](https://arm.seli.gic.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-ci-internal/)

This is intended behaviour which mimics the behavior of the Publish Jenkins job.
This job presents what will happen when the real microservice image is being pushed to the drop repository.
Furthermore, the 'Helm Install' stage needs a Docker image which has been previously uploaded to a remote repository, hence why making a push to the CI Internal is necessary.

The Publish job also pushes to the CI-Internal repository, however the Publish stage promotes the Docker image and Helm chart to the drop repo:
[Docker registry - Drop](https://arm.seli.gic.ericsson.se/artifactory/docker-v2-global-local/proj-eric-oss-drop/)

Similarly, the Helm chart is being pushed to three separate repositories:
[Helm registry - Dev](https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-dev-helm/)

The Precodereview Jenkins job pushes the Helm chart to:
[Helm registry - CI Internal](https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-ci-internal-helm/)

This is intended behaviour which mimics the behavior of the Publish Jenkins job.
This job presents what will happen when the real Helm chart is being pushed to the drop repository.
The Publish Jenkins job pushes the Helm chart to:
[Helm registry - Drop](https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm/)

## Custom CSAC Metrics

CSAC exposes the following custom metrics via prometheus:

- csac_pm_defs_dict_int_total : A count of the PM Definitions persisted in the internal CSAC store
- csac_kpi_defs_dict_int_total : A count of the KPI Definitions persisted in the internal CSAC store
- csac_deployed_profile_defs_int_total: A count of the deployed profile definitions in the runtime data resource
- csac_deployed_kpi_instances_int_total: A count of the deployed KPI definitions in the runtime data resource
- csac_provisioning_pmsc_time_seconds: Time taken for CSAC to complete provisioning of the PMSC
- csac_provisioning_total_time_seconds: Time taken for CSAC to complete provisioning of all target services
- csac_runtime_kpi_instance_errors_total: A count of the total number of runtime KPI definitions that failed to be provisioned in PMSC (Currently, we
  only support 0 for no error and 1 for submission failure)
- csac_file_load_errors_total: A count of OOB resource JSON files that failed to be loaded (Currently, we only support 0 for no error and 1 for file
  loading errors)
- csac_dictionary_pm_definition_errors_total: A count of the dictionary PM definition validation errors (Currently, we only support 0 for no error and
  1 for PM definition validation errors)
- csac_dictionary_kpi_definition_errors_total: A count of the dictionary KPI definition validation errors (Currently, we only support 0 for no error
  and 1 for KPI definition validation errors )

To access these metrics, run the CSAC application and the metrics should be available
at [http://localhost:<port-number>/actuator/prometheus](http://localhost:8080/actuator/prometheus)

## Logging

To secure consistency and simplify handling of log messages, ADP defines a set of Log Design Rules that CSAC must follow. For more information, refer
to [this resource](https://adp.ericsson.se/workinginadpframework/tutorials/logging-in-adp/introduction-to-logging-architecture).

Therefore, CSAC logs are available in 2 formats:

- Default: ADP logging style (JSON)
  - `{"timestamp":"2022-12-01T10:40:59.012-08:00","version":"0.3.0","message":"HV000001: Hibernate Validator 6.2.3.Final","logger":"org.hibernate.validator.internal.util.Version","thread":"background-preinit","service_id":"unknown","severity":"info"}`
- Plain text
  - `2022-12-01 15:47:01.225 [][][] [main] INFO  o.h.validator.internal.util.Version - HV000001: Hibernate Validator 6.2.3.Final`

### For generating plain text logs

- During local developer testing:
  - Use `--logging.config=classpath:logback-plain-text.xml` as part of the `java -jar` command or set it through the run configuration in IntelliJ/Eclipse

### To use a custom log formatter

- During local developer testing:
  - Use `--logging.config=<path_to_the_custom_xml_logback_file>` as part of the `java -jar` command or set it through the run configuration in IntelliJ/Eclipse

### To enable debug logs

- During kubernetes deployment:
  - Run the following command for an active pod: `kubectl get configmaps eric-oss-core-slice-assurance-cfg-log-config -n <namespace> -o yaml | sed -E 's/severity": .*/severity": "debug"/' | kubectl replace -f -`
- During local developer testing:
  - Set `<root level="DEBUG">` in the specific logback file of your choice

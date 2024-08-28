# Dev Environment

## Install and setup podman

### Install [Podman](https://podman.io/getting-started/installation) if docker is not available for you

Install podman using brew if you are using MacOS or following
this [instruction](https://github.com/containers/podman/blob/main/docs/tutorials/podman-for-windows.md) if you are using windows

```
brew install podman
```

Next, create and start your first Podman machine:

```
podman machine init, e.g. podman machine init --cpus=4 --disk-size=60 --memory=6096 -v $HOME:$HOME
podman machine start
```

### Install [podman-compose](https://github.com/containers/podman-compose) if docker-compose is not available for you

podman-compose is an implementation of [Compose Spec](https://compose-spec.io/) with [Podman](https://podman.io/) backend. Install the latest stable
version from PyPI:

```
pip3 install podman-compose
```

### Start dependencies

#### Without mTLS

```
docker compose up
# or
podman-compose up
```
#### With mTLS

The keystore and the truststore for the Wiremock server must be present under /dev/certs as keystore.jks and truststore.jks, respectively.

Using self-signed certificates, you can use the openssl and keytool utilities to create these files. For example,

```
cd <path-to-csac-repo>/dev/certs

openssl pkcs12 -export -in <path-to-generated-cert-files>/wiremock.pem -inkey <path-to-generated-cert-files>/wiremockRSA.pem -out keystore.p12 -password pass:password

keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype JKS -srcstorepass password -deststorepass password

keytool -importcert -alias rootca -file <path-to-generated-cert-files>/rootCA.pem  -keystore truststore.jks -storepass password -storetype JKS
```

based on the generated keys and certificates described on [[IDUN-62678] Knowledge Sharing](https://confluence-oss.seli.wh.rnd.internal.ericsson.com/display/AAP/%5BIDUN-62678%5D+Knowledge+Sharing).

Consequently, run:

```
docker compose -f docker-compose.yml -f docker-compose.tls.yml up
# or
podman-compose -f docker-compose.yml -f docker-compose.tls.yml up
```

The mocked APIs are simultaneously available over https at the port 8443 for mTLS and over http at the port 8080.

### Start CSAC application
1. Either copy **the application-prod.yaml** file under the **dev** folder to your local config directory or use it directly
2. To enable the PMSC provisioning, overwrite `provisioning.pmsc.enabled` to **true**
3. Run the *CoreApplication* from the IDE or jar file via command line

#### Windows users

Common issues and their solutions during the `docker compose up` step

- `Error: short-name resolution enforced but cannot prompt without a TTY
  exit code: 125`
    - Solution: Prepend the image names in the `docker-compose.yml` file with `docker.io` like this: `image: docker.io/wiremock/wiremock`
- `Error: initializing source docker://wiremock/wiremock:latest: pinging container registry registry-1.docker.io: Get "https://registry-1.docker.io/v2/": dial tcp: lookup registry-1.docker.io: Temporary failure in name resolution
  exit code: 125`
    - Solution: Podman uses a WSL instance that is not able to resolve the DNS servers for any requests. Edit the resolve.conf and wsl.conf files as
      mentioned
      here: https://confluence-oss.seli.wh.rnd.internal.ericsson.com/pages/viewpage.action?spaceKey=IDUN&title=Environment+set-up+for+microservice+development+using+WSL2+and+Docker+Desktop
        - ```
          # NOTE: This will delete existing config
          sudo rm -rf /etc/resolv.conf && echo 'nameserver 193.181.14.10
          nameserver 193.181.14.11
          nameserver 8.8.8.8' | sudo tee /etc/resolv.conf
          ```
        - ```
          # NOTE: This will delete existing config
          sudo rm -rf /etc/wsl.conf && echo '[network]
          generateResolvConf = false' | sudo tee /etc/wsl.conf
          ```

### Stop dependencies

```
docker compose down
# or
podman-compose down
```

## Install and import postman collection

### Install [Postman](https://www.postman.com/downloads/)

Install postman using brew if you are using MacOS

```
brew install --cask postman

```

### Import postman collection

#### Generic instructions on how to import postman collection

[Importing and exporting data](https://learning.postman.com/docs/getting-started/importing-and-exporting-data/)

#### Import Wiremock collection for Data Catalog, Schema Registry and PMSC

1. Open Postman
2. Click _File_ -> _Import_ -> _Upload Files_, then choose the collection file: **_WiremockTests.postman_collection.json_**

```
-rw-r--r--  1 <USERNAME>  staff   1602 10 Nov 16:19 README.md
-rw-r--r--@ 1 <USERNAME>  staff  10830 10 Nov 16:18 WiremockTests.postman_collection.json
-rw-r--r--  1 <USERNAME>  staff    741 28 Oct 17:28 docker-compose.yml
drwxr-xr-x  5 <USERNAME>  staff    160 10 Nov 15:56 mocker

```

3. Locate folder: _WiremockTests_
4. Select the query and execute it

## Install TLS-enabled Wiremock Helm chart

1. Create keystore and truststore certs for wiremock as described in this [section](#with-mtls)
2. Create K8S secret using these certs.
    ```
    kubectl create secret generic keystore-cert --from-file=<path-to-csac-repo>/dev/certs/keystore.jks

    kubectl create secret generic truststore-cert --from-file=<path-to-csac-repo>/dev/certs/truststore.jks
    ```
3. Install wiremock helm chart.
    ```
    cd <path-to-csac-repo>/dev/helmCharts/wiremock

    #install wiremock with tls enabled
    helm install wiremock .

    #install wiremock with tls disabled
    helm install wiremock --set tls.enabled=false .
    ```

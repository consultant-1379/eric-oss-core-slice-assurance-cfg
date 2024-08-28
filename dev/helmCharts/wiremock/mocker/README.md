# Mock Data Catalog, Schema Registry and PMSC

The mocked data is stored under directory `data` and Mocker can be started in following way.

## Using Wiremock standalone

1. Download [Wiremock Standalone JAR](https://wiremock.org/docs/running-standalone/).

2. Copy the wiremock jar into **_mocker_** directory:

   ```
   -rw-r--r--  1 <USERNAME>  staff      1081 26 Feb 11:30 README.md
   drwxr-xr-x  4 <USERNAME>  staff       128 14 Feb 15:37 data
   -rw-r--r--@ 1 <USERNAME>  staff  17864803 27 Feb 09:39 wiremock-standalone-3.4.1.jar

   ```

3. Start Wiremock server in **_mocker_** directory.
   ```
   java -jar wiremock-standalone-3.4.1.jar --root-dir ./data

   ```

   Or you can leave the wiremock jar in other directory and run the jar as below:

   ```
   java -jar <wiremock jar path> ./data

   ```

## Using Wiremock in Docker or Podman:

```
docker run -it --rm -p 8080:8080 --name wiremock -v $PWD/data:/home/wiremock wiremock/wiremock:3.4.1

podman run -it --rm -p 8080:8080 --name wiremock -v $PWD/data:/home/wiremock wiremock/wiremock:3.4.1
```

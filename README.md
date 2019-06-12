# Quick start

A enhanced enterprise-level project based on springcloud-gateway

## Building

```bash
mvn clean package -DskipTests -Dmaven.test.skip=true -T 2C -U -Pbuild:image -f springcloud-gateway
```

## Deploy on Docker

```bash
export appName=springcloudgateway
export appActive=pro
export appPort=8080
export appDataDir="/mnt/disk1/${appName}"
export appLogDir="/mnt/disk1/log/${appName}"
#export javaToolOptions='-javaagent:/opt/javaagent/jmx_prometheus_javaagent-0.16.1.jar=10105:/opt/javaagent/jmx-metrics.yml'
#export javaOpts=""
#export javaHeapOpts='-Xms4G -Xmx4G -XX:MaxDirectMemorySize=4G'
#export javaDumpOpts='-XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -XX:HeapDumpPath=${appLogDir}/jvm_dump.hprof'
#export javaGcOpts='-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -Xloggc:${appLogDir}/${appName}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M'
docker run -d \
  -e APP_NOHUP=y \
  -e APP_DEBUG=y \
  -e APP_ACTIVE=${appActive} \
  -e APP_PORT=${appPort} \
  -e APP_DATA=${appDataDir} \
  -e APP_LOG=${appLogDir} \
  -e APP_OPTS=${appOpts} \
  -e JAVA_OPTS=${javaOpts} \
  -p ${appPort}:${appPort} \
  -v ${appDataDir}:${appDataDir} \
  -v ${appLogDir}:${appLogDir} \
  --name=${appName} ${appName}:latest
```

## Deploy on Kubernetes

```bash
kubectl apply -f https://raw.githubusercontent.com/xbcnmbxgw01/springcloud-gateway/main/kubenetes-repo-example/deployement-all-in-one.yml
```

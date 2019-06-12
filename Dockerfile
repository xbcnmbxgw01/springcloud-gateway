# Copyright 2017 ~ 2025 the original author or authors.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
FROM openjdk:8u212-jre-alpine3.9 AS springboot_base
LABEL maintainer="jacks01 <jacks01@gmail.com>"

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
&& echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
&& apk update upgrade \
&& apk add --no-cache procps unzip curl bash tini tzdata \
&& ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

FROM springboot_base
LABEL maintainer="jacks01 <jacks01@gmail.com>"

ARG APP_NAME
ARG APP_VERSION
ARG APP_MAINCLASS
ARG APP_EXT_CLASSPATH # Optional
ENV APP_NAME "${APP_NAME}"
ENV APP_VERSION "${APP_VERSION}"
ENV APP_MAINCLASS "${APP_MAINCLASS}"
ENV APP_EXT_CLASSPATH "${APP_EXT_CLASSPATH}"
ENV APP_BIN_NAME "${APP_NAME}-${APP_VERSION}-bin"
ENV APP_HOME_PARENT "/opt/apps/ecm/${APP_NAME}-package"
ENV APP_HOME "${APP_HOME_PARENT}/${APP_BIN_NAME}"
ENV APP_HOME_LINK "${APP_HOME_PARENT}/${APP_NAME}-current"
ENV APP_DATA "${APP_HOME}/data"
ENV APP_LOG "${APP_HOME}/log"
COPY target/${APP_BIN_NAME}.tar /tmp/${APP_BIN_NAME}.tar

RUN mkdir -p ${APP_HOME} ${APP_DATA} ${APP_LOG} && ln -snf ${APP_HOME} ${APP_HOME_LINK} \
&& tar -xf /tmp/${APP_BIN_NAME}.tar --strip-components=1 -C ${APP_HOME} \
&& rm -rf /tmp/${APP_BIN_NAME}.tar \
&& touch /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh \
&& echo -e "#!/bin/bash" >>/docker-entrypoint.sh \
&& echo -e "\
export APP_NOHUP=\$(echo \$APP_NOHUP|tr '[a-z]' '[A-Z]')\n\
export APP_DEBUG=\$(echo \$APP_DEBUG|tr '[a-z]' '[A-Z]')\n\
export APP_NAME=\${APP_NAME:-${APP_NAME}}\n\
export APP_PORT=\${APP_PORT:-'<default>'}\n\
export APP_ACTIVE=\${APP_ACTIVE:-pro}\n\
export APP_OPTS=\${APP_OPTS:-}\n\
export JAVA_OPTS=\${JAVA_OPTS:-}\n\
export DEFAULT_JAVA_HEAP_OPTS=\"-XX:InitialRAMPercentage=80.0 -XX:MinRAMPercentage=80.0 -XX:MaxRAMPercentage=80.0\" \n\
export JAVA_HEAP_OPTS=\${JAVA_HEAP_OPTS:-\${DEFAULT_JAVA_HEAP_OPTS}}\n\
export DEFAULT_JAVA_DUMP_OPTS=\"-XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -XX:HeapDumpPath=\${APP_LOG}/jvm_dump.hprof\" \n\
export JAVA_DUMP_OPTS=\${JAVA_DUMP_OPTS:-\${DEFAULT_JAVA_DUMP_OPTS}}\n\
export DEFAULT_JAVA_GC_OPTS=\"-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+DisableExplicitGC -Xloggc:\${APP_LOG}/\${APP_NAME}-gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M\" \n\
export JAVA_GC_OPTS=\${JAVA_GC_OPTS:-\${DEFAULT_JAVA_GC_OPTS}}\n\
export JAVA_TOOL_OPTIONS=\${JAVA_TOOL_OPTIONS:-}\n\
[ -n \"\$APP_NOHUP\" ] && export CMD_NOHUP='nohup' \n\
[ -n \"\$APP_DEBUG\" ] && export JVM_DEBUG_OPTS='-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n' \n\
[[ \$APP_PORT != '' && \$APP_PORT != '<default>' ]] && export APP_PORT_OPTS='--server.port=\${APP_PORT}'\n\
export CMD_EXEC=\"\${CMD_NOHUP} java -server \${JAVA_OPTS} \${JAVA_HEAP_OPTS} \${JAVA_DUMP_OPTS} \${JAVA_GC_OPTS} \${JVM_DEBUG_OPTS} \
-Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -cp \
.:\${APP_HOME_LINK}/conf:\${APP_EXT_CLASSPATH}:\${APP_HOME_LINK}/ext-lib/*:\${APP_HOME_LINK}/lib/* \${APP_MAINCLASS} \
--spring.application.name=\${APP_NAME} \
--spring.profiles.active=\${APP_ACTIVE} \
\${APP_PORT_OPTS} \
--server.tomcat.basedir=\${APP_DATA} \
--logging.file.name=\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.log \${APP_OPTS} \
1>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stdout \
2>\${APP_LOG}/\${APP_NAME}_\${APP_ACTIVE}.stderr\"\n\
[ -n \"\$APP_DEBUG\" ] && echo \$CMD_EXEC\n\
echo \"Starting \${APP_NAME}:\${APP_VERSION}(\${APP_ACTIVE}) on \${APP_PORT} ...\"\n\
exec \${CMD_EXEC}\n\
echo \"Started \${APP_NAME}:\${APP_VERSION}(\${APP_ACTIVE}) on \${APP_PORT}\"\n" >>/docker-entrypoint.sh
RUN adduser ${APP_NAME} --disabled-password \
&& chown -R ${APP_NAME}:${APP_NAME} ${APP_HOME_PARENT} ${APP_DATA} ${APP_LOG} /docker-entrypoint.sh \
&& chmod -R 755 ${APP_HOME_PARENT} /docker-entrypoint.sh
USER ${APP_NAME}
ENTRYPOINT ["/sbin/tini", "-s", "-g", "--", "/docker-entrypoint.sh"]
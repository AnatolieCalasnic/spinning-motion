FROM gradle:8.5.0-jdk21
WORKDIR /opt/app
COPY ./build/libs/SpinningMotion-0.0.1-SNAPSHOT.jar ./

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar SpinningMotion-0.0.1-SNAPSHOT.jar"]
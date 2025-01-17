FROM gradle:8.5.0-jdk21
WORKDIR /opt/app
COPY ./build/libs/SpinningMotion-0.0.1-SNAPSHOT.jar ./
ENV SPRING_PROFILES_ACTIVE=staging
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql-container:3306/spinningmotiondb
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=fontys2024
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar SpinningMotion-0.0.1-SNAPSHOT.jar"]

## 使用官方的Maven镜像作为构建环境
#FROM maven:3.8.4-openjdk-17-slim AS build
#WORKDIR /app
#
## 添加自定义settings.xml配置（国内镜像源）
#COPY maven/settings.xml /usr/share/maven/ref/
#
#COPY pom.xml .
#RUN mvn dependency:go-offline
#COPY src ./src
#RUN mvn package -DskipTests

# 使用官方的OpenJDK镜像作为运行环境
FROM openjdk:17-slim
WORKDIR /app
#COPY --from=build /app/target/*.jar app.jar
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
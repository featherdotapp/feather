FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app
COPY . /app

FROM tomcat:latest
COPY --from=builder /app/target/feather-api.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
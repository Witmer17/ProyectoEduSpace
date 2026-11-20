FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S eduspace && adduser -S eduspace -G eduspace
COPY --chown=eduspace:eduspace target/*.jar app.jar
USER eduspace
ENV TZ=America/Lima
EXPOSE 18080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:18080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]

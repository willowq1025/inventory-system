# syntax=docker/dockerfile:1.7
# ==============================================================================
# 后端镜像：Maven 多模块构建 -> Spring Boot 可执行 jar -> 精简 JRE 运行时
# 构建上下文为仓库根目录（docker compose 自动传入）
# 依赖下载通过 BuildKit cache mount 跨构建复用；.dockerignore 已排除 target/、web/node_modules 等
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B -ntp clean package -DskipTests

# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime
LABEL org.opencontainers.image.title="inventory-system-backend" \
      org.opencontainers.image.description="Enterprise Inventory Management System (Spring Boot 3 / Java 21)"

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    LOG_HOME=/app/logs \
    SERVER_PORT=8080

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r app && useradd -r -g app -u 10001 app \
    && mkdir -p /app/logs && chown -R app:app /app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/inventory-admin/target/inventory-admin.jar ./app.jar

USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=70.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Duser.timezone=Asia/Shanghai", \
  "-Dfile.encoding=UTF-8", \
  "-jar", "/app/app.jar"]

# syntax=docker/dockerfile:1.7
# ==============================================================================
# 前端镜像：Node 构建 Vue3 静态产物 -> Nginx 托管 + 反向代理 /api
# 构建上下文为仓库根目录（与后端 Dockerfile 共用），因此路径以 web/ 开头
# ==============================================================================
FROM node:22-alpine AS build
WORKDIR /web
COPY web/package.json web/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY web/ ./
RUN npm run build

# ------------------------------------------------------------------------------
FROM nginx:1.27-alpine AS runtime
LABEL org.opencontainers.image.title="inventory-system-web" \
      org.opencontainers.image.description="Vue3 SPA + Nginx reverse proxy"

ENV TZ=Asia/Shanghai \
    BACKEND_SCHEME=http \
    BACKEND_HOSTPORT=backend:8080 \
    BACKEND_SSL_NAME=backend \
    BACKEND_HOST=backend
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

# 官方镜像启动时对 templates/*.template 执行 envsubst（只替换已定义的环境变量，$host 等 nginx 变量不受影响）
COPY web/nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /web/dist /usr/share/nginx/html

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1

CMD ["nginx", "-g", "daemon off;"]

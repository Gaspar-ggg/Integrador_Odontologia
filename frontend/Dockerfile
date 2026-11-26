# Etapa 1: Compilación de TypeScript
FROM node:20-alpine AS builder
WORKDIR /app

COPY frontend/package*.json frontend/tsconfig.json ./
RUN npm install

COPY frontend/src src
RUN npm run build

# Etapa 2: Servidor Web Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Configuración personalizada de Nginx con proxy inverso
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Copiar sistema de diseño compartido
COPY design-system ./design-system

# Copiar archivos públicos y compilados
COPY frontend/public ./frontend/public
COPY --from=builder /app/dist ./frontend/dist

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

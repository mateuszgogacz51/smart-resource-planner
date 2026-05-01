# ETAP 1: Budowanie Angulara (Node.js)
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --configuration=production

# ETAP 2: Serwowanie plików (Nginx)
FROM nginx:alpine
# Kopiujemy zbudowane pliki Angulara do Nginxa
COPY --from=build /app/dist/planner-ui/browser /usr/share/nginx/html

# Magiczna konfiguracja Nginxa - Proxy do Backendu!
RUN echo 'server { \
    listen 80; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
    location /api/ { \
        proxy_pass http://planner-backend:8080/api/; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

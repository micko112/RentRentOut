# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Kopiramo package fajlove prvo - npm install se kešira ako se deps nisu promenile
COPY package*.json .
RUN npm ci --quiet

# Kopiramo source i buildujemo
COPY . .
RUN npm run build

# ─── Stage 2: Serve ───────────────────────────────────────────────────────────
FROM nginx:alpine

COPY --from=build /app/dist/rent-rent-out-front/browser /usr/share/nginx/html

# nginx config za Angular SPA (history API fallback)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

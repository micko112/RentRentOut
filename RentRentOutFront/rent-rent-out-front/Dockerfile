# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Kopiramo package fajlove prvo - npm install se kešira ako se deps nisu promenile
COPY package*.json .
RUN npm ci --quiet --legacy-peer-deps

# Kopiramo source i buildujemo (SSR)
COPY . .
RUN npm run build

# ─── Stage 2: SSR server ──────────────────────────────────────────────────────
FROM node:20-alpine

WORKDIR /app

# Kopiramo samo dist folder (browser + server)
COPY --from=build /app/dist/rent-rent-out-front ./dist/rent-rent-out-front

EXPOSE 4000

CMD ["node", "dist/rent-rent-out-front/server/server.mjs"]

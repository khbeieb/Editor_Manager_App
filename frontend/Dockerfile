# Stage 1: Build the Angular app
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build --prod

# Stage 2: Serve the app with Nginx
FROM nginx:alpine
COPY --from=builder /app/dist/editor-manager-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

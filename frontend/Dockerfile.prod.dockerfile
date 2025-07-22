# Stage 1: Build the Angular app
FROM node:20-alpine AS builder

WORKDIR /app

COPY . .

RUN npm install
RUN npm run build -- --configuration=production
RUN ls -l dist/ && ls -l dist/editor-manager-frontend/

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*

# Copy Angular dist to Nginx
COPY --from=builder /app/dist/editor-manager-frontend/browser /usr/share/nginx/html

# Copy custom Nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

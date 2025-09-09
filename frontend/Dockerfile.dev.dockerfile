# Use Node.js base image
FROM node:20-alpine

# Set working directory inside container
WORKDIR /app

# Copy package.json and lock file first for dependency caching
COPY package*.json yarn.lock* ./

# Clear yarn cache and install dependencies
RUN yarn cache clean && yarn install --frozen-lockfile

# Copy the rest of your app's code
COPY . .

# Expose Angular's dev server port
EXPOSE 4200

# Start Angular in dev mode
CMD ["yarn", "start"]

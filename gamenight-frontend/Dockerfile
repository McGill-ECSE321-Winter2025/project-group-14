# Use Node.js to build the React app
FROM node:18 AS build

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package.json package-lock.json /app/
RUN npm install

# Copy source code and build the React app
COPY src /app/src
COPY public /app/public
RUN npm run build

# Use Nginx to serve the build output
FROM nginx:alpine

# Copy the build output to Nginx's public folder
COPY --from=build /app/build /usr/share/nginx/html

# Expose the port the app will run on
EXPOSE 80

# Run Nginx to serve the React app
CMD ["nginx", "-g", "daemon off;"]

FROM node:16.14-alpine AS builder

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install

COPY . .

ARG GAME_URL
ENV REACT_APP_GAME_URL=$GAME_URL
ARG GAME_SOCKET_URL
ENV REACT_APP_GAME_SOCKET_URL=$GAME_SOCKET_URL

RUN ["npm", "run", "build"]

FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d

COPY --from=builder /usr/src/app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
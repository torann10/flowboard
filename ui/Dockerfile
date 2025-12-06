FROM nginx:latest

RUN apt-get update
RUN rm /usr/share/nginx/html/*
RUN rm /etc/nginx/conf.d/default.conf
RUN mkdir /usr/share/nginx/html/app

ADD ./dist/flowboard/browser /usr/share/nginx/html/app
ADD ./default.conf.template /etc/nginx/templates/default.conf.template

COPY entrypoint.sh /

RUN ["chmod", "+x", "/entrypoint.sh"]
ENTRYPOINT ["/entrypoint.sh"]
CMD ["nginx","-g","daemon off;"]

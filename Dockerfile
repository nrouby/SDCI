FROM ubuntu:trusty

WORKDIR /code
COPY init.py ./

RUN chmod -R 777 /code

RUN apt-get update -y
RUN apt-get install -y \
	    net-tools \
	    iputils-ping \
	    iproute \
            nodejs \
            npm \
            wget \
            curl


RUN npm config set strict-ssl false
RUN npm cache clean -f
RUN npm install -g n
RUN n 14
RUN npm install express yargs systeminformation request

RUN apt install python3-pip -y

CMD /bin/bash

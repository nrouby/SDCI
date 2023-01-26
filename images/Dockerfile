FROM python:3.7-slim-buster

WORKDIR /code
ADD . /code

EXPOSE 5001

RUN apt-get update -y
RUN apt-get install nano -y
RUN apt-get install netcat -y
RUN apt-get install net-tools -y
RUN apt-get install iputils-ping -y
RUN apt-get install iproute2 -y

RUN python -m pip install -r requirements.txt

COPY . .

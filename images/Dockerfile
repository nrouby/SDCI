FROM python:3.7-slim-buster

WORKDIR /code
ADD . /code

RUN apt-get update -y
RUN apt-get install nano -y

RUN python -m pip install -r requirements.txt

COPY . .

CMD ["python", "app.py"]

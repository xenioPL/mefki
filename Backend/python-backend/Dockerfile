FROM ubuntu:xenial

RUN apt update && apt install -y python3 python3-pip
RUN pip3 install flask
RUN pip3 install requests
COPY server.py server.py

EXPOSE 80
ENTRYPOINT python3 server.py

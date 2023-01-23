#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Dec 16 11:20:23 2022

@author: mocquais
"""

from flask import Flask, request
import json
import requests

app = Flask(__name__)
devcount = {"10.0.0.1":0,"10.0.0.2":0,"10.0.0.3":0}

@app.route('/device/<dev>/data', methods=['POST'])
def count(dev):
    # Get the JSON data from the request body
    data = request.get_json()
    headers = {'Content-type': 'application/json'}
    response = requests.post('http://10.0.0.4:8181/', data=data, headers=headers)
    if not request.headers.getlist("X-Forwarded-For"):
        ip = request.remote_addr
    else:
        ip = request.headers.getlist("X-Forwarded-For")[0]
    devcount[ip] += 1 

    return response

@app.route('/health')
def health():

    result = devcount.copy()
    return json.dumps(result)


if __name__ == '__main__':
    app.run(host="0.0.0.0",port="8181")

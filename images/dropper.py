#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Dec 16 11:20:23 2022

@author: mocquais
"""

from flask import Flask
import json

app = Flask(__name__)

@app.route('/device/<dev>/data', methods=['POST'])
def dump(dev):
    return json.dumps({'success':True}), 200, {'ContentType':'application/json'} 


if __name__ == '__main__':
    app.run(host="0.0.0.0",port="8181")
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Dec 16 11:20:23 2022

@author: mocquais
"""

from flask import Flask, request, jsonify
import json

app = Flask(__name__)

@app.route('/<filename>')
def read_json(filename):
    print("supertest!")
    # Open the JSON file
    with open(filename+".json") as f:
        # Read the contents of the file
        data = f.read()

    # Parse the JSON data
    json_data = json.loads(data)

    # Return the parsed JSON data as a JSON response
    return jsonify(json_data)

@app.route('/write/<filename>', methods=['POST'])
def write_json(filename):
    # Get the JSON data from the request body
    data = request.get_json()

    # Write the JSON data to the file
    with open(filename+'.json', 'w') as f:
        json.dump(data, f)

    return 'JSON data written to file'


if __name__ == '__main__':
    app.run(host="0.0.0.0")

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Dec 16 11:20:23 2022

@author: mocquais
"""

import requests
import os
import subprocess
import sys

def get_setup(data):
    if data['type'] == 'gateway':
        # Build the command for gateway configuration
        command = 'node gateway.js --local_ip "{}" --local_port {} --local_name "{}" --remote_ip "{}" --remote_port {} --remote_name "{}"'.format(data["local_ip"],data["local_port"],data["local_name"],data["remote_ip"],data["remote_port"],data["remote_name"])
        # Save the link to the script used for gateway
        code_url = 'http://homepages.laas.fr/smedjiah/tmp/mw/gateway.js'
    if data['type'] == 'dev':
        # Build the command for dev configuration
        command = 'node device.js --local_ip "{}" --local_port {} --local_name "{}" --remote_ip "{}" --remote_port {} --remote_name "{}" --send_period {}'.format(data["local_ip"],data["local_port"],data["local_name"],data["remote_ip"],data["remote_port"],data["remote_name"],data["send_period"])
        # Save the link to the script used for dev
        code_url = 'http://homepages.laas.fr/smedjiah/tmp/mw/device.js'
    if data['type'] == 'server':
        # Build the command for server configuration
        command = 'node server.js --local_ip "{}" --local_port {} --local_name "{}"'.format(data["local_ip"],data["local_port"],data["local_name"])
        # Save the link to the script used for server
        code_url = 'http://homepages.laas.fr/smedjiah/tmp/mw/server.js'
    if data['type'] == 'app':
        # Build the command for app configuration
        command = 'node application.js --remote_ip "{}" --remote_port {} --device_name "{}" --send_period {}'.format(data["remote_ip"],data["remote_port"],data["remote_name"],data["send_period"])
        # Save the link to the script used for app
        code_url = 'http://homepages.laas.fr/smedjiah/tmp/mw/application.js'
    return command,code_url
 
if len(sys.argv) <= 1 :
    # Get the container_nameeter from the environment variable
    param = os.environ['CONTAINER_NAME']
else:
    param = sys.argv[1] 

# Send the request to localhost:5000/<container_name>
response = requests.get("http://172.17.0.2:5000/{}".format(param))

# Store the JSON response in a dictionary
data = response.json()

command, code_url = get_setup(data)

# Download the script file from `code_url`
file = requests.get(code_url)

# Save the file
open(data['type']+'.js', 'wb').write(file.content)

# Run the command and capture the output
subprocess.call(command, shell=True)

# Run the next script
if data['tailing'] != 'None' :
    output = subprocess.call("python init.py " + data['tailing'] , shell=True)

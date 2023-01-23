#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri Dec 16 11:20:23 2022

@author: mocquais
"""

import requests

# Ask the user for the filename
filename = input("Enter the JSON file to create: ")

# Set the URL using the filename
url = 'http://localhost:5000/write/' + filename

# Ask the user for the different parameters
type_container = input("Enter the container type: ")
local_ip = input("Enter the local IP address: ")
local_port = input("Enter the local port: ")
local_name = input("Enter the local name: ")
remote_ip = input("Enter the remote IP address: ")
remote_port = input("Enter the remote port: ")
remote_name = input("Enter the remote name: ")
period = input("Enter the send_period: ")
delai = input("Enter the delay: ")
tailing = input("Enter the tailing target: ")

# Set the data to be sent in the request
data = {
    "type" : type_container,
    "local_ip": local_ip,
    "local_port": local_port,
    "local_name": local_name,
    "remote_ip": remote_ip,
    "remote_port": remote_port,
    "remote_name": remote_name,
    "send_period": period,
    "delai": delai,
    "tailing": tailing
}

# Make the POST request with the data
response = requests.post(url, json=data)

# Print the response from the server
print(response.text)
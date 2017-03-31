#!/bin/bash
set -e

source init.sh

# Start a glassfish server
cd $QS_DIR

pwd

#asadmin start-domain --debug domain1 
asadmin start-domain domain1
asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.port=7080

# or for websphere:
#/home/mmusgrov/products/ibm/was/WLP/wlp


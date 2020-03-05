#!/bin/bash
source init.sh
set -ex

# Start a glassfish server
cd $QS_DIR

export PATH=$GLASSFISH/bin:$PATH

#asadmin start-domain --debug --verbose domain1 
asadmin start-domain domain1
asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.port=7080


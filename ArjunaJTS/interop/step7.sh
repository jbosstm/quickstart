#!/bin/bash
set -e

source init.sh

asadmin stop-domain domain1

[ -d "$JBOSS_HOME"  ] || fatal "file not found: $JBOSS_HOME"
$JBOSS_HOME/bin/jboss-cli.sh --connect shutdown

#!/bin/bash
source init.sh
set -ex

# start jboss and put it into JTS mode
[ -d "$JBOSS_HOME"  ] || fatal "file not found: $JBOSS_HOME"
cd $JBOSS_HOME
./bin/standalone.sh -c standalone-full.xml -Djboss.tx.node.id=1 -Dcom.arjuna.ats.jts.transactionServiceId=0 &
sleep `timeout_adjust 10 2>/dev/null || echo 10`
./bin/jboss-cli.sh --connect --file=$QS_DIR/configure-jts-transactions.cli

# shut it down using the script step7.sh

#!/bin/bash
source init.sh
set -ex

# start jboss and put it into JTS mode
[ -d "$JBOSS_HOME"  ] || fatal "file not found: $JBOSS_HOME"
cd $JBOSS_HOME
./bin/standalone.sh -c standalone-full.xml -Djboss.tx.node.id=1 -Dcom.arjuna.ats.jts.transactionServiceId=0 &
executeJBossCli --file=$QS_DIR/configure-jts-transactions.cli
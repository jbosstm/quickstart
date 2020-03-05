#!/bin/bash
source init.sh
set -ex

cd $QS_DIR
# deploy ejbs to glassfish
./ejb_operations.sh -a gf1 -f "${QS_DIR}/../test-ejbs/target/ejbtest.war"

# deploy ejbs to WildFly
./ejb_operations.sh -a wf1 -f "${QS_DIR}/../test-ejbs/target/ejbtest.war"

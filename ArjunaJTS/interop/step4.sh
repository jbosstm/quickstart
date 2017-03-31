#!/bin/bash
set -e

source init.sh

cd $QS_DIR
# deploy ejbs to glassfish
./d.sh -a gf1 -f src/interop/target/ejbtest.war

# deploy ejbs to WildFly
./d.sh -a wf1 -f src/interop/target/ejbtest.war

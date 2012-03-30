# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running integration 1 XATMI"

# RUN THE INTEGRATION 1 EXAMPLE
cd $BLACKTIE_HOME/quickstarts/integration1/xatmi_service/
generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes="CreditService.c,DebitService.c" -Dserver.name=integra
if [ "$?" != "0" ]; then
        exit -1
fi
btadmin startup
if [ "$?" != "0" ]; then
        exit -1
fi
cd $BLACKTIE_HOME/quickstarts/integration1/client/
generate_client -Dclient.includes=client.c 
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/quickstarts/integration1/xatmi_service/
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

#rem "Build Converted XATMI service"
(cd $BLACKTIE_HOME/quickstarts/integration1/ejb && mvn install)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/integration1/ejb/ear/ && mvn install jboss-as:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/integration1/xatmi_adapter/ && mvn install)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/integration1/xatmi_adapter/ear/ && mvn install jboss-as:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi

#rem "Run Converted XATMI service"
(cd $BLACKTIE_HOME/quickstarts/integration1/client/ && generate_client -Dclient.includes=client.c)
if [ "$?" != "0" ]; then
	exit -1
fi
cd $BLACKTIE_HOME/quickstarts/integration1/client/
sleep 5
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/integration1/xatmi_adapter/ear/ && mvn jboss-as:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd $BLACKTIE_HOME/quickstarts/integration1/ejb/ear/ && mvn jboss-as:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi

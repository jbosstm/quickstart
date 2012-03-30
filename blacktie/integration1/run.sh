# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running integration 1 XATMI"

# RUN THE INTEGRATION 1 EXAMPLE
cd xatmi_service/
generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes="CreditService.c,DebitService.c" -Dserver.name=integra
if [ "$?" != "0" ]; then
        exit -1
fi
btadmin startup
if [ "$?" != "0" ]; then
        exit -1
fi
cd ../client/
generate_client -Dclient.includes=client.c 
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
cd ../xatmi_service/
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

cd ..

#rem "Build Converted XATMI service"
(cd ejb && mvn install)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ejb/ear/ && mvn install jboss-as:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd xatmi_adapter/ && mvn install)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd xatmi_adapter/ear/ && mvn install jboss-as:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi

#rem "Run Converted XATMI service"
cd client
generate_client -Dclient.includes=client.c
if [ "$?" != "0" ]; then
	exit -1
fi
sleep 5
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ../xatmi_adapter/ear/ && mvn jboss-as:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ../ejb/ear/ && mvn jboss-as:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi

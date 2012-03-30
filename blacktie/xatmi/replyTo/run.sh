# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running quickstart to show reply to"

# GENERATE A SERVER
generate_server -Dserver.includes=BarService.c -Dserver.name=server
if [ "$?" != "0" ]; then
    exit -1
fi

# RUN THE SERVER - MUST PROVIDE BLACKTIE_CONFIGURATION
# AS THIS EXAMPLE USES IT TO FIND THE CORRECT LIBRARY_NAME
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi

# BUILD THE C CLIENT
generate_client -Dclient.includes=client.c

# RUN THE C clientA
echo '0
0' | ./client clientA CLIENTRESPONSEHANDLER_1 hello1
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

# RUN THE C clientB
echo '0
0' | ./client clientB CLIENTRESPONSEHANDLER_2 hello2
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi


# SHUTDOWN THE SERVER
BLACKTIE_SERVER=server btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

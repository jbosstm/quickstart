# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running XATMI admin quickstart"

# RUN THE FOOAPP SERVER
cd ../../xatmi/fooapp/
generate_server -Dservice.names=FOOAPP -Dserver.includes=BarService.c -Dserver.name=fooapp
if [ "$?" != "0" ]; then
	exit -1
fi
export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

cd ../../blacktie-admin-services/xatmi

# SHUTDOWN THE SERVER RUNNING THE XATMI ADMIN CLIENT
generate_client -Dclient.includes=client.c
echo '0
0
0
0
1' | ./client
if [ "$?" != "0" ]; then
    killall -9 server
    killall -9 client
	exit -1
fi
# SHUTDOWN THE SERVER RUNNING THE XATMI ADMIN CLIENT
generate_client -Dclient.includes=client.c
echo '0
0
0
0
2' | ./client
if [ "$?" != "0" ]; then
    killall -9 server
    killall -9 client
	exit -1
fi
# PICK UP THE CLOSING SERVER
sleep 3

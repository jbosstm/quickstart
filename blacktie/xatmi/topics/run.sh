# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running Topics"

# CLEAN LOG
rm -f blacktie*.log

# RUN THE SERVER
generate_server -Dserver.includes=BarService.c -Dserver.name=myserv
if [ "$?" != "0" ]; then
	exit -1
fi

btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

# RUN THE C CLIENT
generate_client -Dclient.includes=client.c
./client
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

barcalled=`grep "bar called" blacktie.log|wc -l`
if [ "$barcalled" != "2" ]; then
	echo "every server should been bursted with the messages"
	exit -1
fi

# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running externally managed queue quickstart"

# RUN THE QUEUEING EXAMPLE
generate_client -Dclient.includes=queues.c
./client put 10
if [ "$?" != "0" ]; then
    echo Unable to queue all messages
    exit -1
fi
export BLACKTIE_SERVER_ID=1
./client get 5
if [ "$?" != "0" ]; then
    echo Unable to retrieve first 5 queued messages
    exit -1
fi
./client get 5
if [ "$?" != "0" ]; then
    echo Unable to retrieve last 5 queued messages
    exit -1
fi
unset BLACKTIE_SERVER_ID

# RUN THE TXSENDER EXAMPLE
echo "Quickstart: Running transactional queue quickstart"
generate_client -Dclient.includes=txsender.c -Dclient.output.file=txsender
if [ "$?" != "0" ]; then
	exit -1
fi
generate_client -Dclient.includes=queues.c
if [ "$?" != "0" ]; then
	exit -1
fi
echo '1
' | ./txsender
if [ "$?" != "0" ]; then
    echo Unable to queue all messages
    exit -1
fi
export BLACKTIE_SERVER_ID=1
./client get 2
if [ "$?" != "0" ]; then
    echo Unable to retrieve the queued messages
    exit -1
fi
unset BLACKTIE_SERVER_ID

# RUN THE PROPAGATED TRANSACTION EXAMPLE
echo "Quickstart: Running propagated transaction queue quickstart"
generate_client -Dclient.includes=queues.c -Dclient.output.file=client
if [ "$?" != "0" ]; then
	exit -1
fi
generate_server -Dserver.includes=BarService.c  -Dservice.names=QUEUES -Dserver.name=queues
if [ "$?" != "0" ]; then
	exit -1
fi
generate_client -Dclient.includes=client.c -Dclient.output.file=clientSender
if [ "$?" != "0" ]; then
	exit -1
fi

btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
echo '1
' | ./clientSender
if [ "$?" != "0" ]; then
    echo Unable to invoke queue proxy
    exit -1
fi
export BLACKTIE_SERVER=myserv
export BLACKTIE_SERVER_ID=1
./client get 1
if [ "$?" != "0" ]; then
    echo Unable to retrieve the queued message
    exit -1
fi
unset BLACKTIE_SERVER_ID
unset BLACKTIE_SERVER
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

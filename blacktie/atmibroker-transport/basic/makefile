all: 
	$($(shell generate_client -Dclient.includes=client.c))

test:
	g++ client.c -pthread -ldl -I../../../target/classes/include -L../../../target/debug/shared -latmibroker-transport -o w

clean:
	rm -f client

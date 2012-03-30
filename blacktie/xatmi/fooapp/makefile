all: server client

server: 
	$($(shell generate_server -Dservice.names=FOOAPP -Dserver.includes=BarService.c))

client: 	
	$($(shell generate_client -Dclient.includes=client.c))

clean:
	rm -f server client

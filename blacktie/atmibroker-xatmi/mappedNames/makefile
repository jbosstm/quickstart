all: hiprio loprio client

hiprio: 
	$($(shell generate_server -Dservice.names=ONE,TWO -Dserver.output.file=hiprio  -Dserver.includes=BarService.c -Dserver.name=hiprio))

loprio:
	$($(shell generate_server -Dservice.names=THREE,FOUR -Dserver.output.file=loprio  -Dserver.includes=BarService.c -Dserver.name=loprio))

client: 	
	$($(shell generate_client -Dclient.includes=client.c))

clean:
	rm -f hiprop loprio client

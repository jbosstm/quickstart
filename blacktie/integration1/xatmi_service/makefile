all: server 

server: 
	$($(shell generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes=CreditService.c,DebitService.c))

clean:
	rm -f server 

CC=gcc 
CFLAGS=-Wall

IDIR=$(BLACKTIE_HOME)/include
CFLAGS=-I$(IDIR)
ifneq ($(DEBUG),y)  
   CFLAGS += -g3
else
   CLFAGS += -O2
endif

LDIR=$(BLACKTIE_HOME)/lib/cxx
LDFLAGS=-L$(LDIR) -lblacktie-xatmi -lblacktie-core -blacktie-tx

all: client txsender log4cxx.properties

log4cxx.properties:
	jar xf $(BLACKTIE_HOME)/lib/java/codeGeneration-5.0.0.Final-SNAPSHOT-SNAPSHOT.jar log4cxx.properties

client: queues.c 
	gcc -o $@ $^ $(CFLAGS) $(LDFLAGS)

txsender: txsender.c
	gcc -o $@ $^ $(CFLAGS) $(LDFLAGS)

clean:
	rm -f client txsender log4cxx.properties

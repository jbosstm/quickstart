/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
#include <stdio.h>
#include <string.h>

#include "xatmi.h"
#include "tx.h"

#include "btlogger.h"
#include "malloc.h"
#include <stdlib.h>

char prompt(char* prompt) {
	btlogger("Please press return after you: %s...", prompt);
	return getchar();
}

void output(char* operationName, char* listIn) {
	char* list;
	char* nextToken;
	int i = 0;
	btlogger("Output from %s: ", operationName);
	list = (char*) malloc(strlen(listIn) + 1);
	list[strlen(listIn)] = '\0';
	list = (char*) memcpy(list, listIn, strlen(listIn));
	nextToken = strtok(list, ",");
	while (nextToken != NULL) {
		btlogger((char*) "Element: %d Value: %s", i, nextToken);
		nextToken = strtok(NULL, " ");
		i++;
	}
}

char* itemAt(char* list, int index) {
	char* nextToken = strtok(list, ",");
	int i = 0;
	while (index < i) {
		nextToken = strtok(NULL, " ");
		i++;
	}
	return nextToken;
}

int main(int argc, char **argv) {
	int tpstatus;
	char *retbuf;
	long retbufsize;
	char *sbuf;
	long sbufsize;
	long callflags;
	char* list;
	char* serverName;
	int id;

	callflags = 0;
	retbufsize = 1;
	retbuf = tpalloc("X_OCTET", 0, retbufsize);

	prompt("Start JBoss Application Server");
	prompt("Start an XATMI server");

	// listRunningServers
	sbufsize = strlen("listRunningServers,") + 1;
	sbuf = tpalloc("X_OCTET", 0, sbufsize);
	memset(sbuf, 0, sbufsize);
	strcpy(sbuf, "listRunningServers,");
	tpstatus = tpcall("BTDomainAdmin", sbuf, sbufsize, (char **) &retbuf,
			&retbufsize, callflags);
	list = (char*) malloc(retbufsize);
	strncpy(list, retbuf, retbufsize - 1);
	list[retbufsize - 1] = '\0';
	output((char*) "listRunningServers", list);

	if (strlen(list) != 0) {
		char response = prompt(
				"Enter the id of a server to get the instance numbers of");
		int index = atoi(&response);
		serverName = itemAt(list, index);

		// listRunningInstanceIds
		sbufsize = strlen("listRunningInstanceIds,,") + strlen(serverName) + 1;
		sbuf = tprealloc(sbuf, sbufsize);
		memset(sbuf, 0, sbufsize);
		sprintf(sbuf, "listRunningInstanceIds,%s,", serverName);
		tpstatus = tpcall("BTDomainAdmin", sbuf, sbufsize, (char **) &retbuf,
				&retbufsize, callflags);
		list = (char*) malloc(retbufsize);
		strncpy(list, retbuf, retbufsize - 1);
		list[retbufsize - 1] = '\0';
		output((char*) "listRunningInstanceIds", list);

		prompt("Start a second instance of the same server");
		getchar();

		// listRunningInstanceIds
		sbufsize = strlen("listRunningInstanceIds,,") + strlen(serverName) + 1;
		sbuf = tprealloc(sbuf, sbufsize);
		memset(sbuf, 0, sbufsize);
		sprintf(sbuf, "listRunningInstanceIds,%s,", serverName);
		tpstatus = tpcall("BTDomainAdmin", sbuf, sbufsize, (char **) &retbuf,
				&retbufsize, callflags);
		list = (char*) malloc(retbufsize);
		strncpy(list, retbuf, retbufsize - 1);
		list[retbufsize - 1] = '\0';
		output((char*) "listRunningInstanceIds", list);

		response = prompt(
				"Enter the instance id of the server you wish to shutdown");
		id = atoi(&response);

		// shutdown
		sbufsize = strlen("shutdown,,,,") + strlen(serverName) + 1 + 1;
		sbuf = tprealloc(sbuf, sbufsize);
		memset(sbuf, 0, sbufsize);
		sprintf(sbuf, "shutdown,%s,%d,", serverName, id);
		btlogger((char*) "Shutting down: %s (%d)", serverName, id);
		tpstatus = tpcall("BTDomainAdmin", sbuf, sbufsize, (char **) &retbuf,
				&retbufsize, callflags);
	} else {
		btlogger((char*) "ERROR: There were no running servers detected");
	}

	//tpfree(sbuf);
	tpfree(retbuf);
	return 0;
}

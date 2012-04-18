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
#include "btnbf.h"

#include "btlogger.h"

int main(int argc, char **argv) {
	char* sendbuf = NULL;
	char* rcvbuf = NULL;
	char name[16];
	int toReturn = 0;
	long id = 1001;
	int rc;
	long rcvlen;
	int len;

	sendbuf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	if(sendbuf == NULL) {
		fprintf(stderr, "tpalloc BT_NBF failed\n");
		return -1;
	}
	
	strcpy(name, "zhfeng");
	btaddattribute(&sendbuf, (char*)"name", (char*)name, 6);
	btlogger((char*)"add name value is %s", name);

	btaddattribute(&sendbuf, (char*)"id", (char*)&id, sizeof(id));
	btlogger((char*)"add id value is %d", id);

	rcvbuf = tpalloc((char*)"BT_NBF", (char*)"employee", 0);
	rcvlen = strlen(rcvbuf);

	rc = tpcall((char*)"NBFEXAMPLE", (char*)sendbuf, strlen(sendbuf), (char**)&rcvbuf, &rcvlen, (long)0);

	if(rc == 0 && tperrno == 0) {
		btlogger((char*) "call NBF service ok");
		len = 16;
		rc = btgetattribute(rcvbuf, (char*)"name", 0, name, &len);
		if(rc == 0) {
			btlogger((char*)"recv name value is %s", name);
		    toReturn = -1;
		} else {
			btlogger((char*)"name removed");
		}

		rc = btgetattribute(rcvbuf, (char*)"id", 0 , (char*)&id, &len);
		if(rc == 0) {
			btlogger((char*)"recv id value is %d", id);
		} else {
			btlogger((char*)"id removed");
		    toReturn = -1;
		}
	} else {
		btlogger((char*) "call failed with rc = %d, tperrno = %d", rc, tperrno);
		toReturn = -1;
	}

	tpfree(sendbuf);
	tpfree(rcvbuf);

	return toReturn;
}

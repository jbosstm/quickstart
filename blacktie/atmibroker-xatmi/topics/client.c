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

#include "btlogger.h"

int main(int argc, char **argv) {
	int tpstatus;
	char *retbuf;
	char type[20];
	char subtype[20];
	long retbufsize;
	char *sbuf;
	long sbufsize;
	long callflags;

	callflags = 0L;
	sbufsize = 29;
	sbuf = tpalloc("X_OCTET", 0, sbufsize);
	memset(sbuf, 0, sbufsize);
	strcpy(sbuf, "THIS IS YOUR CLIENT SPEAKING");
	retbufsize = 15;
	retbuf = tpalloc("X_OCTET", 0, retbufsize);
	memset(retbuf, 0, retbufsize);

	// tptypes
	tptypes(sbuf, type, subtype);

	// tpcall
	btlogger((char*) "Calling tpcall with input: %s", sbuf);
	tpstatus = tpcall("JAVA_Topic", sbuf, sbufsize, (char **) &retbuf,
			&retbufsize, callflags);

	if (tpstatus == -1 && tperrno != TPEINVAL) {
		btlogger((char*) "tpcall TOPIC failed with %d", tperrno);
	}

	tpstatus = tpacall("JAVA_Topic", sbuf, sbufsize, TPNOREPLY);
	if (tpstatus == -1) {
		btlogger((char*) "tpacall TOPIC with NOREPLY flag failed with %d", tperrno);
	}

	tpfree(sbuf);
	tpfree(retbuf);
	return 0;
}

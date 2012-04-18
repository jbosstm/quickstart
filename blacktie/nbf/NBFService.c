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
#include <stdlib.h>

#include "xatmi.h"
#include "btnbf.h"
#include "btlogger.h"
#include "string.h"

void NBFEXAMPLE(TPSVCINFO * svcinfo) {
	char* buf = svcinfo->data;
	int rc = 0;

	char name[16];
	long id;
	int  len = 16;

	rc = btgetattribute(buf, (char*)"name", 0, (char*) name, &len);
	if(rc == 0) {
		btlogger((char*) "get name value is %s", name);
	}

	len = 0;
	rc = btgetattribute(buf, (char*)"id", 0, (char*)&id, &len);
	if(rc == 0) {
		btlogger((char*) "get id value is %d", id);
	}

	btlogger((char*)"remove attr");
	rc = btdelattribute(buf, (char*)"name", 0);
	if(rc == 0) {
		btlogger((char*) "remove name");
	}

	id = 1234;
	rc = btsetattribute(&buf, (char*)"id", 0, (char*)&id, sizeof(id));
	if(rc == 0) {
		btlogger((char*) "set id value to 1234");
	}

	tpreturn(TPSUCCESS, 0, buf, strlen(buf), 0);
}

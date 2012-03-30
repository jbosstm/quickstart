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
#include "btlogger.h"
#include "string.h"
#include "btservice.h"

#ifdef __cplusplus
extern "C" {
#endif
EXPORT_SERVICE void myserverdone(void) {
	btlogger((char*) "user server done");
}

EXPORT_SERVICE int myserverinit(int argc, char** argv) {
	int i;
	btlogger((char*) "server initialized with %d arguments", argc);
	for (i = 0; i < argc; i++) {
		btlogger((char*) "Argument: %d Value: %s", i, argv[i]);
	}
	return 0;
}
#ifdef __cplusplus
}
#endif

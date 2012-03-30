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
#include "message.h"
#include "btservice.h"

#ifdef __cplusplus
extern "C" {
#endif
EXPORT_SERVICE void BAR(TPSVCINFO * svcinfo) {
	char* buffer;
	int sendlen;

    MESSAGE* message = (MESSAGE*) svcinfo->data;

	btlogger((char*) "bar called response expected by: %s data %s", message->reply_to, message->data);

	sendlen = 15;
	buffer = tpalloc("X_OCTET", 0, sendlen);
	strcat(buffer, "PROC:");
	strcat(buffer, message->data);

    tpacall(message->reply_to, buffer, sendlen, TPNOREPLY);
}
#ifdef __cplusplus
}
#endif

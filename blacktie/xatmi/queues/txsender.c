/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and others contributors as indicated
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
#include <stdlib.h>

#include "xatmi.h"
#include "tx.h"
#include "btqueue.h"

#include "btlogger.h"

char prompt(char* prompt) {
	btlogger("Please press return after you: %s...", prompt);
	return getchar();
}

static int send_one(msg_opts_t* mopts, const char* data) {
	int rc = -1;
	int len = strlen(data) + 1;
	char *buf = tpalloc((char*) "X_OCTET", NULL, len);

	if (tperrno == 0) {
		rc = btenqueue((char*) "TestOne", mopts, strcpy(buf, (char*) data), len, 0);

		if (tperrno != 0 || rc != 0)
			btlogger((char*) "tpacall error: %d %d", rc, tperrno);
		else
			btlogger((char*) "Sent message %s", data);

		if (tperrno != 0)
			rc = tperrno;

		tpfree(buf);

		return rc;
	} else {
		btlogger((char*) "tpalloc error: %d", tperrno);
		return tperrno;
	}
}

/*
 * An quickstart showing how to decouple sender and receiver using queues with optional priorities.
 */
int main(int argc, char **argv) {
	msg_opts_t mopts;
	char response;
	int index;

	int rc = -1;

	rc = tx_open();
	if (rc != 0) {
		btlogger((char*) "open error: %d", rc);
	} else {
		rc = tx_begin();
		if (rc != 0) {
			btlogger((char*) "begin error: %d", rc);
		} else {
			mopts.priority = 0;

			if (argc > 1)
				while (--argc != 0)
					send_one(&mopts, argv[argc]);
			else if (send_one(&mopts, "1") == 0)
				send_one(&mopts, "2");
#if 0
			len = 2;
			buf = tpalloc((char*) "X_OCTET", NULL, len);

			if (tperrno != 0) {
				btlogger((char*) "tpalloc error: %d", tperrno);
			} else {
				(void) strcpy(buf, (char*) "1");
				rc = btenqueue((char*) "TestOne", &mopts, buf, len, 0);

				if (tperrno != 0 || rc != 0) {
					btlogger((char*) "tpacall error: %d %d", rc, tperrno);
				} else {
					btlogger((char*) "Sent message 1");
					(void) strcpy(buf, (char*) "2");
					rc = btenqueue((char*) "TestOne", &mopts, buf, len, 0);

					if (tperrno != 0 || rc != 0) {
						btlogger((char*) "tpacall error: %d %d", rc, tperrno);
					} else {
						btlogger((char*) "Sent message 2");
					}
				}

				tpfree(buf);
			}
#endif
			response = prompt("Press 1 to commit or 2 to rollback");
			index = atoi(&response);

			if (index == 1) {
				rc = tx_commit();
				if (rc != 0) {
					btlogger((char*) "commit error: %d", rc);
				}
			} else {
				rc = tx_rollback();
				if (rc != 0) {
					btlogger((char*) "rollback error: %d", rc);
				}
			}
		}
	}

	rc = tx_close();
	if (rc != 0) {
		btlogger((char*) "close error: %d", rc);
	}
	return rc;
}

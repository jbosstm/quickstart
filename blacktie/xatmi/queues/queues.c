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

#include "tx.h"
#include "xatmi.h"
#include "btqueue.h"

#include "btlogger.h"

extern BLACKTIE_XATMI_DLL int serverdone();
static int txopen = 0;

#define TEST_ERROR(msg, err)	\
	if (err != 0) {	\
		btlogger((char*) "%s error %d", msg, err);	\
		if (txopen) tx_close();	\
		return -1;	\
	} //else {btlogger((char*) "%s ok", msg);}

/**
 * Send cnt messages begining with message id msgid with the requested priority.
 */
static int put_messages(unsigned int cnt, unsigned int msgid, unsigned int pri) {
	int id;
	int err;
	msg_opts_t mopts;
	char msg[16];
	char* buf;
	long len = 2;

	for (id = msgid; id < msgid + cnt; id++) {
		err = -1;

		mopts.priority = pri;
		mopts.ttl = 0;

		(void) sprintf(msg, (char*) "%d", id);
		len = strlen(msg) + 1;

		buf = tpalloc((char*) "X_OCTET", NULL, len);

		if (tperrno != 0) {
			btlogger((char*) "tpalloc error: %d", tperrno);
		} else {
			(void) strcpy(buf, msg);
			err = btenqueue((char*) "TestOne", &mopts, buf, len, 0);

			if (tperrno != 0 || err != 0)
				btlogger((char*) "tpacall error: %d %d", err, tperrno);
			else
				btlogger((char*) "Sent a message");

			tpfree(buf);
		}

		if (err != 0) {
			return err;
		}
	}

	return 0;
}

/**
 * Take the next cnt messages off the service queue.
 */
static int get_messages(unsigned int cnt, int txmode, long millis) {
	int msgCnt = 0;
	msg_opts_t mopts = {0, 0L};

	mopts.ttl = millis;
	btlogger((char*) "Preparing to dequeue the messages: %d", cnt);

	if (txmode) {
		TEST_ERROR("open", tx_open());
		txopen = 1;
		TEST_ERROR("begin", tx_begin());
	}

	/*
	 * Register a service listener for the queue. If the env variable BLACKTIE_SERVER_ID
	 * is set then the framework will automatically register a server listener for the
	 * queue during tpadvertise. Otherwise do it manually using serverinit(argc, argv)
	 * where argv includes, for quickstart, server -c linux -i 1
	 */

	// wait for the service routine, qservice, to consume the required number of messages (msgCnt)
	while (msgCnt < cnt) {
		long olen = 10;
		char* odata = (char*) tpalloc((char*) "X_OCTET", NULL, olen);
		long flags = 0;
		int err = btdequeue((char*) "TestOne", &mopts, &odata, &olen, flags);
		if (err != -1 && tperrno == 0) {
			msgCnt++;
			btlogger("Received %s which was %d long", odata, olen);
		} else {
			if (tperrno == TPETIME)
				btlogger("timed out waiting for messages");
			else
				btlogger("Got an error reading from the queue. tperrno: %d result %d", tperrno, err);
			break;
		}
	}

	if (txmode) {
		TEST_ERROR("end", (txmode == 1 ? tx_commit() : tx_rollback()));
	}

	// Manually shutdown the server
	serverdone();

	return 0;
}

/*
 * An quickstart showing how to decouple sender and receiver using queues with optional priorities.
 */
int main(int argc, char **argv) {
	int rc = -1;
	int arg2 = (argc > 2 ? atoi(argv[2]) : 0);
	int arg3 = (argc > 3 ? atoi(argv[3]) : 0);

	if (argc < 3) {
		btlogger((char *) "usage: %s <put cnt [msgid priority] | get cnt [timeout (ms)] " \
			"| getcommit cnt [timeout (ms)] | getabort cnt [timeout (ms)]>",
				argv[0]);
	} else if (strcmp(argv[1], "put") == 0) {
		rc = put_messages(arg2, arg3, argc > 4 ? atoi(argv[4]) : 0);
	} else if (strcmp(argv[1], "get") == 0) {
		rc = get_messages(arg2, 0, (long) arg3);
	} else if (strcmp(argv[1], "getcommit") == 0) {
		rc = get_messages(arg2, 1, (long) arg3);
	} else if (strcmp(argv[1], "getabort") == 0) {
		rc = get_messages(arg2, 2, (long) arg3);
	} else {
		btlogger((char *) "usage: %s <put cnt [priority] | get cnt>", argv[0]);
	}

	return rc;
}

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

#include "debit.h"
#include "credit.h"

int main(int argc, char **argv) {
	int status;
	DEBIT_T* debitBuf;
	CREDIT_T* creditBuf;
	char *retbuf;
	long retbufsize;
	char* debitted;
	char* creditted;
	long callflags;

	callflags = 0L;
	debitBuf = (DEBIT_T*) tpalloc("X_COMMON", "debit", 0);
	creditBuf = (CREDIT_T*) tpalloc("X_COMMON", "credit", 0);
	retbuf = tpalloc("X_OCTET", NULL, 9);

	debitBuf->acct_no = 1L;
	debitBuf->amount = 10;

	creditBuf->acct_no = 2L;
	creditBuf->amount = 10;

	// Open the connection to the transaction manager
	btlogger((char*) "Starting transaction");
	status = tx_open();
	if (status == 0) {

		// Start a transaction
		btlogger((char*) "Starting transaction");
		status = tx_begin();
		if (status == 0) {

			// Debit the account
			btlogger((char*) "Reallocate response buffer");
			retbuf = tprealloc(retbuf, 9);
			if (tperrno == 0 && tptypes(retbuf, NULL, NULL) == 9) {
				memset(retbuf, 0, 9);

				btlogger((char*) "Calling debit");
				status = tpcall("DEBITEXAMPLE", (char*) debitBuf, 0,
						(char **) &retbuf, &retbufsize, callflags);
				if (status == 0) {
					status = (tperrno != 0);
					if (status == 0) {
						debitted = (char*) "DEBITTED";
						btlogger((char*) "Checking output");
						status = (strcmp(retbuf, debitted) != 0);
						if (status == 0) {

							// Credit the account
							btlogger((char*) "Reallocate response buffer");
							retbuf = tprealloc(retbuf, 10);

							if (tperrno == 0 && tptypes(retbuf, NULL, NULL)
									== 10) {
								memset(retbuf, 0, 10);
								btlogger((char*) "Calling credit");
								status = tpcall("CREDITEXAMPLE", (char*) creditBuf, 0,
										(char **) &retbuf, &retbufsize,
										callflags);
								if (status == 0) {
									status = (tperrno != 0);
									if (status == 0) {
										creditted = (char*) "CREDITTED";
										btlogger((char*) "Checking output");
										status = (strcmp(retbuf, creditted)
												!= 0);
										if (status == 0) {
											// Commit the transaction
											btlogger(
													(char*) "Committing transaction");
											status = tx_commit();
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	btlogger((char*) "Transfer Status: tperrno: %d, status: %d", tperrno,
			status);

	tpfree((char*) debitBuf);
	tpfree((char*) creditBuf);
	tpfree(retbuf);
	return status;
}

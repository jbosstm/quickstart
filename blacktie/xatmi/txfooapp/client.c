/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
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
#ifdef UNITTEST
#include "TestTxRMTPCall.h"
extern "C" {
#include "tx/request.h"
}
#else
#include "tx/request.h"
#endif

extern int run_tests(product_t *prod_array);

static product_t *get_product(const char *pid) {
    int id = atoi((char *) pid);
    product_t *p;

    for (p = products; p->id != -1; p++)
        if (p->id == id)
            return p;

    return 0;
}

int main(int argc, char **argv)
{
    product_t prods[8];
    int rv = -1;
    int i;
    int rmCnt = 0;
    btlogger_debug( "TxLog %s:%d", __FUNCTION__, __LINE__);

    if (argc > 1) {
        for (i = 1; i < argc; i++) {
            product_t *p = get_product(argv[i]);

            if (p == NULL) {
                btlogger_debug("TxLog product id %s not found", argv[i]);
                for (p = products; p->id != -1; p++)
                    btlogger_debug("TxLog product id %d", p->id);
                //return fatal("Requested db is not supported\n");
            } else {
                prods[rmCnt++] = *p;
            }
        }

        prods[rmCnt].id = -1;
        prods[rmCnt].access = 0;
        prods[rmCnt].xaflags = 0;
    } else {
        for (i = 0; products[i].id != -1; i++)
            prods[i] = products[i];

        prods[i] = products[i];
    }

    for (i = 0; prods[i].id != -1; i++) {
        btlogger_debug("TxLog INFO: %s (%s) id=%d flags=0x%x",
            prods[i].pname, prods[i].dbname, prods[i].id, prods[i].xaflags());
    }

    rv = run_tests(prods);

    btlogger("TxLog Test %s (%d)\n", (rv ? "failed" : "passed"), rv);

    return rv;
}

#include <stdio.h>
#include <string.h>

#include <XARecoveryLog.h>

#define IOR(rr) ((char *) (rr + 1))

/*
 * This quickstart shows how to dump the transaction recovery log. Compile as follows:
 *
 * g++ -I$BLACKTIE_HOME/include -L$BLACKTIE_HOME/lib/cxx -latmibroker-tx rclog.cxx
 *
 * There is also an interactive mode for deleting records. WARNING deleting a
 * recovery record means that its corresponding transaction branch will have
 * to be recovered manually.
 */

int
main(int argc, char* argv[])
{
    if (argc <= 2) {
        fprintf(stderr, "syntax %s <-i|-v> <recovery log file path>\n", argv[0]);
        return -1;
    }

    XARecoveryLog log(argv[2]);
	bool prompt = (strcmp(argv[1], "-i") == 0 ? true : false);

   	for (rrec_t* rr = log.find_next(0); rr; rr = log.find_next(rr)) {
		XID &xid = rr->xid;

       	fprintf(stdout, "XID=%ld:%ld:%ld:%s IOR=%s\n", xid.formatID, xid.gtrid_length, xid.bqual_length,
			(char *) (xid.data + xid.gtrid_length), IOR(rr));

		if (prompt) {
			char ans[64];

			fprintf(stdout, "Do you wish to delete this record [y/n]? ");

			if (fgets(ans, sizeof (ans), stdin) != NULL && ans[0] == 'y') {
				log.del_rec(rr->xid);
			}
		}
	}

    return 0;
}

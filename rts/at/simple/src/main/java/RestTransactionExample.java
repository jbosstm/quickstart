import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.TxStatusMediaType;

public class RestTransactionExample {
    public static void main(String[] args) throws Exception {
        String coordinatorUrl = "http://localhost:8080/rest-at-coordinator/tx/transaction-manager";

        if (args.length > 0 && args[0].startsWith("coordinator="))
            coordinatorUrl = args[0].substring("coordinator=".length());

        // create a helper with thin(e desired transaction manager resource endpoint
        TxSupport txn = new TxSupport(coordinatorUrl);

        // start a transaction
        txn.startTx();

        // verify that there is an active transaction
        if (!txn.txStatus().equals(TxStatusMediaType.TX_ACTIVE))
            throw new RuntimeException("A transaction should be active: " + txn.txStatus());

        System.out.println("transaction running: " + txn.txStatus());

        // see how many RESTful transactions are running (there should be at least one)
        int txnCount = txn.txCount();

        if (txn.txCount() == 0)
            throw new RuntimeException("The transaction did not start");

        // end the transaction
        txn.commitTx();

        // there should now be one fewer transactions
        if (txn.txCount() >= txnCount)
            throw new RuntimeException("The transaction did not complete");

        System.out.println("Success");
    }
}
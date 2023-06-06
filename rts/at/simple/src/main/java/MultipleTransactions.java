import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.TxStatusMediaType;

public class MultipleTransactions {
    public static void main(String[] args) throws Exception {
        String coordinatorUrl = "http://localhost:8080/rest-at-coordinator/tx/transaction-manager";

        if (args.length > 0 && args[0].startsWith("coordinator="))
            coordinatorUrl = args[0].substring("coordinator=".length());

        // create a helper with the desired transaction manager resource endpoint
        TxSupport[] txns = { new TxSupport(coordinatorUrl), new TxSupport(coordinatorUrl)};

        // start transactions
        for (TxSupport txn: txns)
            txn.startTx();

        // verify that all the transactions are active
        for (TxSupport txn: txns)
            if (!txn.txStatus().equals(TxStatusMediaType.TX_ACTIVE))
                throw new RuntimeException("A transaction should be active: " + txn.txStatus());

        // see how many RESTful transactions are running (there should be at least one)
        int txnCount = txns[0].txCount();

        if (txns[0].txCount() != 2)
            throw new RuntimeException("At least one transaction did not start");

        // end the transaction
        for (TxSupport txn: txns)
            txn.commitTx();

        // there should now be one fewer transactions
        if (txns[0].txCount() >= txnCount)
            throw new RuntimeException("At least one transaction did not complete");

        System.out.println("Success");
    }
}
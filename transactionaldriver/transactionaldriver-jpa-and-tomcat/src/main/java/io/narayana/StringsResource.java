package io.narayana;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.TransactionManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path("/")
public class StringsResource {

    private final StringDao stringDao;

    private final TransactionManager transactionManager;

    public StringsResource() throws NamingException, SQLException {
        stringDao = new StringDao();
        transactionManager = InitialContext.doLookup("java:comp/env/TransactionManager");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getStrings() throws Exception {
        System.out.println(this.getClass().getSimpleName() + " GET");
        transactionManager.begin();
        try {
            List<String> all = stringDao.getAll();
            transactionManager.commit();
            return all;
        } catch (SQLException e) {
            transactionManager.rollback();
            throw e;
        }
    }

    @POST
    public void saveString(String string) throws Exception {
        System.out.println(this.getClass().getSimpleName() + " POST");
        System.out.println(this.getClass().getSimpleName() + " begin transaction");
        transactionManager.begin();
        System.out.println(this.getClass().getSimpleName() + " save string");
        try {
            stringDao.save(string);
            System.out.println(this.getClass().getSimpleName() + " commit transaction");
            transactionManager.commit();
            System.out.println(this.getClass().getSimpleName() + " transaction committed successfully");
        } catch (SQLException e) {
            System.out.println(this.getClass().getSimpleName() + " rollback transaction");
            transactionManager.rollback();
            System.out.println(this.getClass().getSimpleName() + " transaction rolled back");
            throw e;
        }
    }

}
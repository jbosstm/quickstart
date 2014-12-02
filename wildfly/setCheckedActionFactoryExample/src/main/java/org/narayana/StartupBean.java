package org.narayana;

import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.narayana.handler.MyEJB;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;

@Singleton
@javax.ejb.Startup
@TransactionAttribute(TransactionAttributeType.NEVER)
@AccessTimeout(-1)
public class StartupBean {

	@EJB
	private MyEJB myEJB;

	@PostConstruct
	private void test() {
		System.out.println("StartupBean call");

		arjPropertyManager.getCoordinatorEnvironmentBean()
				.setCheckedActionFactory(new CheckedActionFactory() {

					@Override
					public CheckedAction getCheckedAction(Uid txId,
							String actionType) {
						System.out.println("MyEJB::getCheckedAction called");
						return new CheckedAction() {
							@Override
							public void check(boolean isCommit, Uid actUid,
									Hashtable list) {
								System.out.println("MyEJB::check called");
							}
						};
					}

				});
		myEJB.call();
		
	}

}

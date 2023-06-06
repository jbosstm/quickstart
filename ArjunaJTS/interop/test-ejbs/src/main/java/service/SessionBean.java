package service;

import service.remote.ISessionHome;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.RemoteHome;

import jakarta.ejb.Stateless;

import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;

import java.util.concurrent.atomic.AtomicInteger;

@Stateless
@RemoteHome(ISessionHome.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBean {
	static AtomicInteger counter = new AtomicInteger(0);
	boolean isWF;

	@PostConstruct
	public void init() {
		isWF = System.getProperty("jboss.node.name") != null;

		counter.set(isWF ? 8000 : 7000);
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public String getNext() {
		return getNext(null);
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public String getNext(String failureType) {
		try {
			TxnHelper.addResources(isWF, failureType);

			System.out.printf("%s returning next counter%n", this);
			return String.valueOf(counter.getAndIncrement());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
package org.narayana;

import java.io.Serializable;

import jakarta.ejb.EJB;
import jakarta.faces.bean.ApplicationScoped;
import jakarta.inject.Named;

import org.narayana.handler.MyEJB;

@Named("CheckedActionFactoryExample")
@ApplicationScoped
public class CheckedActionFactoryExample implements Serializable {

	@EJB
	private MyEJB myEJB;

	public void call() {
		System.out.println("CheckedActionFactoryExample called");
		myEJB.call();
	}
}
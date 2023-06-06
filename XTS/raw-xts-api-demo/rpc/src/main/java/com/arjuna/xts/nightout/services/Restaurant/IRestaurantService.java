package com.arjuna.xts.nightout.services.Restaurant;

public interface IRestaurantService
{
     /**
     * Book a number of seats in the restaurant
     * Enrols a Participant if necessary, then passes
     * the call through to the business logic.
     *
     * @param how_many The number of seats to book
     */
    public void bookSeats(int how_many);

}
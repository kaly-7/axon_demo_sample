package com.kaly7dev.axon_sample.coreapi.exceptions;

public class OrderAlreadyConfirmedException extends IllegalStateException{
    public OrderAlreadyConfirmedException(String orderId) {
        super("Cannot perform operation because order [" + orderId + "] is already confirmed.");
    }
}

package com.kaly7dev.axon_sample.commandmodel;

import com.kaly7dev.axon_sample.commandmodel.order.OrderAggregate;
import com.kaly7dev.axon_sample.coreapi.commands.*;
import com.kaly7dev.axon_sample.coreapi.events.*;
import com.kaly7dev.axon_sample.coreapi.exceptions.DuplicateOrderLineException;
import com.kaly7dev.axon_sample.coreapi.exceptions.OrderAlreadyConfirmedException;
import com.kaly7dev.axon_sample.coreapi.exceptions.UnconfirmedOrderException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class OrderAggregateUnitTest {
    private static final String ORDER_ID = UUID.randomUUID().toString();
    private static final String PRODUCT_ID = UUID.randomUUID().toString();

    private FixtureConfiguration<OrderAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(OrderAggregate.class);
    }

    @Test
    void giveNoPriorActivity_whenCreateOrderCommand_thenShouldPublishOrderCreatedEvent() {
        fixture.givenNoPriorActivity()
                .when(new CreateOrderCommand(ORDER_ID))
                .expectEvents(new OrderCreatedEvent(ORDER_ID));
    }

    @Test
    void givenOrderCreatedEvent_whenAddProductCommand_thenShouldPublishProductAddedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID))
                .when(new AddProductCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductAddedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEventAndProductAddedEvent_whenAddProductCommandForSameProductId_thenShouldThrowDuplicateOrderLineException() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new ProductAddedEvent(ORDER_ID, PRODUCT_ID))
                .when(new AddProductCommand(ORDER_ID, PRODUCT_ID))
                .expectException(DuplicateOrderLineException.class)
                .expectExceptionMessage(Matchers.predicate(message -> ((String) message).contains(PRODUCT_ID)));
    }

    @Test
    void givenOrderCreatedEventAndProductAddedEvent_whenIncrementProductCountCommand_thenShouldPublishProductCountIncrementedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new ProductAddedEvent(ORDER_ID, PRODUCT_ID))
                .when(new IncrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductCountIncrementedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEventProductAddedEventAndProductCountIncrementedEvent_whenDecrementProductCountCommand_thenShouldPublishProductCountDecrementedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID),
                        new ProductAddedEvent(ORDER_ID, PRODUCT_ID),
                        new ProductCountIncrementedEvent(ORDER_ID, PRODUCT_ID))
                .when(new DecrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductCountDecrementedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEventAndProductAddedEvent_whenDecrementProductCountCommand_thenShouldPublishProductRemovedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new ProductAddedEvent(ORDER_ID, PRODUCT_ID))
                .when(new DecrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectEvents(new ProductRemovedEvent(ORDER_ID, PRODUCT_ID));
    }

    @Test
    void givenOrderCreatedEvent_whenConfirmOrderCommand_thenShouldPublishOrderConfirmedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID))
                .when(new ConfirmOrderCommand(ORDER_ID))
                .expectEvents(new OrderConfirmedEvent(ORDER_ID));
    }

    @Test
    void givenOrderCreatedEventAndOrderConfirmedEvent_whenConfirmOrderCommand_thenExpectNoEvents() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new OrderConfirmedEvent(ORDER_ID))
                .when(new ConfirmOrderCommand(ORDER_ID))
                .expectNoEvents();
    }

    @Test
    void givenOrderCreatedEvent_whenShipOrderCommand_thenShouldThrowUnconfirmedOrderException() {
        fixture.given(new OrderCreatedEvent(ORDER_ID))
                .when(new ShipOrderCommand(ORDER_ID))
                .expectException(UnconfirmedOrderException.class);
    }

    @Test
    void givenOrderCreatedEventAndOrderConfirmedEvent_whenShipOrderCommand_thenShouldPublishOrderShippedEvent() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new OrderConfirmedEvent(ORDER_ID))
                .when(new ShipOrderCommand(ORDER_ID))
                .expectEvents(new OrderShippedEvent(ORDER_ID));
    }

    @Test
    void givenOrderCreatedEventProductAndOrderConfirmedEvent_whenAddProductCommand_thenShouldThrowOrderAlreadyConfirmedException() {
        fixture.given(new OrderCreatedEvent(ORDER_ID), new OrderConfirmedEvent(ORDER_ID))
                .when(new AddProductCommand(ORDER_ID, PRODUCT_ID))
                .expectException(OrderAlreadyConfirmedException.class)
                .expectExceptionMessage(Matchers.predicate(message -> ((String) message).contains(ORDER_ID)));
    }

    @Test
    void givenOrderCreatedEventProductAddedEventAndOrderConfirmedEvent_whenIncrementProductCountCommand_thenShouldThrowOrderAlreadyConfirmedException() {
        fixture.given(new OrderCreatedEvent(ORDER_ID),
                        new ProductAddedEvent(ORDER_ID, PRODUCT_ID),
                        new OrderConfirmedEvent(ORDER_ID))
                .when(new IncrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectException(OrderAlreadyConfirmedException.class)
                .expectExceptionMessage(Matchers.predicate(message -> ((String) message).contains(ORDER_ID)));
    }

    @Test
    void givenOrderCreatedEventProductAddedEventAndOrderConfirmedEvent_whenDecrementProductCountCommand_thenShouldThrowOrderAlreadyConfirmedException() {
        fixture.given(new OrderCreatedEvent(ORDER_ID),
                        new ProductAddedEvent(ORDER_ID, PRODUCT_ID),
                        new OrderConfirmedEvent(ORDER_ID))
                .when(new DecrementProductCountCommand(ORDER_ID, PRODUCT_ID))
                .expectException(OrderAlreadyConfirmedException.class)
                .expectExceptionMessage(Matchers.predicate(message -> ((String) message).contains(ORDER_ID)));
    }
}

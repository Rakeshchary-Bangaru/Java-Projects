package com.ecommerce.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailNotificationObserverTest {

    @Test
    void shouldRejectNullOrder() {

        EmailNotificationObserver observer =
                new EmailNotificationObserver();

        assertThrows(
                IllegalArgumentException.class,
                () -> observer.update(null)
        );
    }
}
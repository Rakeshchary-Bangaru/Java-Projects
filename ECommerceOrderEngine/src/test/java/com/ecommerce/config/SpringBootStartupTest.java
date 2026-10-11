package com.ecommerce.config;

import com.ecommerce.EcommerceApplication;
import com.ecommerce.checkout.HibernateCheckoutService;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

class SpringBootStartupTest {

    @Test
    void shouldStartSpringBootAndDiscoverBeans() {

        try (ConfigurableApplicationContext context =
                     SpringApplication.run(
                             EcommerceApplication.class,
                             "--spring.main.web-application-type=none"
                     )) {

            ProductRepository productRepository =
                    context.getBean(
                            ProductRepository.class
                    );

            ProductService productService =
                    context.getBean(
                            ProductService.class
                    );

            HibernateCheckoutService checkoutService =
                    context.getBean(
                            HibernateCheckoutService.class
                    );

            Environment environment =
                    context.getEnvironment();


            assertNotNull(productRepository);
            assertNotNull(productService);
            assertNotNull(checkoutService);


            assertEquals(
                    "ecommerce-order-engine",
                    environment.getProperty(
                            "spring.application.name"
                    )
            );

            assertEquals(
                    "6",
                    environment.getProperty(
                            "ecommerce.version"
                    )
            );

            assertEquals(
                    "hibernate",
                    environment.getProperty(
                            "ecommerce.persistence"
                    )
            );
        }
    }
}
package com.ecommerce.config;

import com.ecommerce.checkout.HibernateCheckoutService;
import com.ecommerce.repository.HibernateProductRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class SpringCoreConfigTest {

    @Test
    void shouldCreateAndInjectSpringBeans() {

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             AppConfig.class
                     )) {

            ProductRepository repository =
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

            assertNotNull(checkoutService);
            assertNotNull(repository);

            assertInstanceOf(
                    HibernateProductRepository.class,
                    repository
            );

            assertNotNull(productService);


            ProductRepository repositoryAgain =
                    context.getBean(
                            ProductRepository.class
                    );

            assertSame(
                    repository,
                    repositoryAgain
            );
        }
    }
}
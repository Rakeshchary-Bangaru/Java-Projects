package com.ecommerce.config;

import com.ecommerce.EcommerceApplication;
import com.ecommerce.checkout.HibernateCheckoutService;
import com.ecommerce.repository.HibernateProductRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class SpringCoreConfigTest {

    @Test
    void shouldCreateAndInjectSpringBeans() {

        SpringApplication application =
                new SpringApplication(
                        EcommerceApplication.class
                );

        /*
         * We only want to test Spring bean creation here.
         * No embedded Tomcat needs to start.
         */
        application.setWebApplicationType(
                WebApplicationType.NONE
        );

        try (ConfigurableApplicationContext context =
                     application.run()) {

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
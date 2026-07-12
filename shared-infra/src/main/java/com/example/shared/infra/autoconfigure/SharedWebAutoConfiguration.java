package com.example.shared.infra.autoconfigure;

import com.example.shared.infra.tracing.RequestTracingFilter;
import com.example.shared.infra.web.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SharedWebAutoConfiguration {

    @Bean
    public RequestTracingFilter requestTracingFilter() {
        return new RequestTracingFilter();
    }

    // Referencia excepciones de JPA/spring-orm: solo se registra si están en el classpath.
    @Bean
    @ConditionalOnClass(name = {
            "jakarta.persistence.EntityNotFoundException",
            "org.springframework.orm.ObjectOptimisticLockingFailureException"})
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}

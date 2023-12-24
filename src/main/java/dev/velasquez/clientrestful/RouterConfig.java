package dev.velasquez.clientrestful;

import dev.velasquez.clientrestful.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> rutas(ProductoHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/client"), handler::findAll)
                .andRoute(RequestPredicates.GET("/api/client/{id}"), handler::findById)
                .andRoute(RequestPredicates.POST("/api/client"), handler::save)
                .andRoute(RequestPredicates.PUT("/api/client/{id}"), handler::update)
                .andRoute(RequestPredicates.DELETE("/api/client/{id}"), handler::delete)
                .andRoute(RequestPredicates.POST("/api/client/upload/{id}"), handler::upload);
    }
}

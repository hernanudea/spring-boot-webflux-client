package dev.velasquez.clientrestful.handler;

import dev.velasquez.clientrestful.models.Producto;
import dev.velasquez.clientrestful.services.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class ProductoHandler {

    private ProductoService service;

    public ProductoHandler(ProductoService service) {
        this.service = service;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().contentType(APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                service.findbyId(id).flatMap(p -> ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .syncBody(p))
                        .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.findbyId(id)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .syncBody(p))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if (errorResponse.getStatusCode().is4xxClientError()) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("error", "No existe el producto. ".concat(errorResponse.getMessage()));
                        body.put("timestamp", new Date());
                        body.put("status", errorResponse.getStatusCode().value());
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .syncBody(body);
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);

        return producto.flatMap(p -> {
                    if (p.getCreateAt() == null) {
                        p.setCreateAt(new Date());
                    }
                    return service.save(p);
                }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(APPLICATION_JSON)
                        .syncBody(p))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest()
                                .contentType(APPLICATION_JSON)
                                .syncBody(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        return errorHandler(
                producto
                        .flatMap(p -> service.update(p, id))
                        .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                                .contentType(APPLICATION_JSON)
                                .syncBody(p))
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                service.delete(id).then(ServerResponse.noContent().build())
        );
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return errorHandler(
                request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                        .cast(FilePart.class)
                        .flatMap(file -> service.upload(file, id))
                        .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                                .contentType(APPLICATION_JSON)
                                .syncBody(p))
        );
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
        return response.onErrorResume(error -> {
            WebClientResponseException errorResponse = (WebClientResponseException) error;
            if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
                body.put("timestamp", new Date());
                body.put("status", errorResponse.getStatusCode().value());
                return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(body);
            }
            return Mono.error(errorResponse);
        });
    }
}
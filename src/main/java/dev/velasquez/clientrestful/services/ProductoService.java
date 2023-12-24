package dev.velasquez.clientrestful.services;

import dev.velasquez.clientrestful.models.Producto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

    Flux<Producto> findAll();

    Mono<Producto> findbyId(String id);

    Mono<Producto> save(Producto producto);

    Mono<Producto> update(Producto producto, String id);

    Mono<Void> delete(String id);

    public Mono<Producto> upload(FilePart file, String id);



}

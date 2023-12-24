package dev.velasquez.clientrestful.services;

import dev.velasquez.clientrestful.models.Producto;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
public class ProductoServiceImpl implements ProductoService {

    private static final String ID_PARAM = "/{id}";
    private final WebClient webClient;

    public ProductoServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Flux<Producto> findAll() {
        return webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Producto.class);
    }

    @Override
    public Mono<Producto> findbyId(String id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        return webClient.get().uri(ID_PARAM, params)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return webClient.post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
//                .body(BodyInserters.fromObject(producto))
                .syncBody(producto)
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {

        return webClient.put()
                .uri(ID_PARAM, Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(producto)
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return webClient.put()
                .uri(ID_PARAM, Collections.singletonMap("id", id))
                .exchange()
                .then();
    }

//    @Override
//    public Mono<Producto> upload(FilePart file, String id) {
//        MultipartBodyBuilder parts = new MultipartBodyBuilder();
//        parts.asyncPart("file", file, DatabaseDriver.class)
//                .header(h -> {
//                    h.setContentDispositionFormData("file", file.filename());
//                });
//
//        return webClient.post()
//                .uri("/upload".concat(ID_PARAM), Collections.singletonMap("id", id))
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .syncBody(parts.build())
//                .retrieve()
//                .bodyToMono(Producto.class);
//    }
    @Override
    public Mono<Producto> upload(FilePart file, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts.asyncPart("file", file.content(), DataBuffer.class).headers(h -> {
            h.setContentDispositionFormData("file", file.filename());
        });

        return webClient.post()
                .uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MULTIPART_FORM_DATA)
                .syncBody(parts.build())
                .retrieve()
                .bodyToMono(Producto.class);
    }

}

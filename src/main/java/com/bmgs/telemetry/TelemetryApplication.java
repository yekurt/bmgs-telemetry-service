package com.bmgs.telemetry;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
@EnableEurekaClient
@RestController
@RequestMapping("/telemetry")
public class TelemetryApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelemetryApplication.class, args);
	}

	@Bean
	ApplicationRunner init(TelemetryRepository tr)
	{
		return args -> tr.deleteAll()
				.thenMany(
						Flux.just("temperature","pressure").map(l -> new TelemetryData(null,l)).flatMap(tr::save)
				)
				.thenMany(tr.findAll())
				.subscribe(System.out::println);
	}

	@Bean
	RouterFunction<?> routes(TelemetryRepository tr)
	{
		return
				route(GET("/telemetry"), r -> ok().body(tr.findAll(), TelemetryData.class))
						.andRoute(GET("/telemetry/{id}"), r -> ok().body(tr.findById(r.pathVariable("id")), TelemetryData.class))
				.andRoute(GET("/delay"), r -> ok().body(Flux.just("Hello World").delayElements(Duration.ofSeconds(10)), String.class));

	}
}

interface TelemetryRepository extends ReactiveMongoRepository<TelemetryData, String>
{

}

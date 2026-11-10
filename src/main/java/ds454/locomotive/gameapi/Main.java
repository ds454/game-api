package ds454.locomotive.gameapi;

import ds454.locomotive.gameapi.grpc.HeaderInterceptor;
import ds454.locomotive.gameapi.model.gameApiClient;
import ds454.locomotive.gameapi.repository.GameRepository;
import ds454.locomotive.gameapi.repository.UserGameRepository;
import ds454.locomotive.gameapi.service.GameGrpcService;
import ds454.locomotive.gameapi.spring.BeanProvider;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static String env(String name, String fallback){
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }
    public static void main(String[] args) throws Exception {
        log.info("Starting Game API Application...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("ds454.locomotive.gameapi");
        BeanProvider.setContext(context);


       

        int grpcPort = Integer.parseInt(env("GRPC_PORT", "50051"));
        String steamHost = env("STEAM_SERVICE_HOST", "steam-service");
        int steamPort = Integer.parseInt(env("STEAM_SERVICE_PORT", "50051"));

        log.info("Starting gRPC server on port " + grpcPort);

        gameApiClient.init(steamHost, steamPort, context.getBean(GameRepository.class), 
        context.getBean(UserGameRepository.class));
        Server s;
        s = ServerBuilder.forPort(grpcPort)
                // This sends the request to the HeaderInterceptor, which gets the user's UUID and
                // steamid from the request.
                .addService(ServerInterceptors.intercept(new GameGrpcService(), new HeaderInterceptor()))
                .build()
                .start();
        log.info("gRPC server started.");

        s.awaitTermination();
    }
}
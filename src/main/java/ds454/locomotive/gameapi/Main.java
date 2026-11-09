package ds454.locomotive.gameapi;

import ds454.locomotive.gameapi.grpc.HeaderInterceptor;
import ds454.locomotive.gameapi.grpc.HelloService;
import ds454.locomotive.gameapi.spring.BeanProvider;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    // Should be set with env variable GRPC_PORT
    private static int grpcPort = 8082;

    public static void main(String[] args) throws Exception {
        log.info("Starting User API Application...");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("ds454.locomotive.gameapi");
        BeanProvider.setContext(context);

        log.info("Starting gRPC server on port " + Main.grpcPort);
        Server s;
        s = ServerBuilder.forPort(Main.grpcPort)
                // This sends the request to the HeaderInterceptor, which gets the user's UUID and
                // steamid from the request.
                .addService(ServerInterceptors.intercept(new HelloService(), new HeaderInterceptor()))
                .build()
                .start();
        log.info("gRPC server started.");

        s.awaitTermination();
    }
}
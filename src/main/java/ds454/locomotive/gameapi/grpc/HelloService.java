package ds454.locomotive.gameapi.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.GreetRequest;
import ds454.locomotive.generated.grpc.GreetResponse;
import ds454.locomotive.generated.grpc.HasMetRequest;
import ds454.locomotive.generated.grpc.HasMetResponse;
import ds454.locomotive.generated.grpc.HelloServiceGrpc.HelloServiceImplBase;
import ds454.locomotive.gameapi.model.User;
import ds454.locomotive.gameapi.repository.UserRepository;
import ds454.locomotive.gameapi.spring.BeanProvider;
import io.grpc.stub.StreamObserver;

public class HelloService extends HelloServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(HelloService.class);

    UserRepository userRepo = BeanProvider.getBean(UserRepository.class);

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        log.info("Received greet request!");

        var response = GreetResponse.newBuilder();
        String name = request.getName();
        User u = new User();

        try {
            u.setUsername(name);
            userRepo.save(u);
            response.setGreeting("Hello, " + name + "! Nice to meet you!.");
        }
        catch (Exception e) {
            response.setGreeting("Nice to see you again " + name + "!");
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void hasMet(HasMetRequest request, StreamObserver<HasMetResponse> responseObserver) {
        // This is how we get the requester's uuid and steam id. To keep things clean and testable,
        // you should get these values in the gRPC handler, then pass them to other functions.
        var requestersUuid = HeaderInterceptor.UserUuid.get();
        var requestersSteamid = HeaderInterceptor.SteamId.get();

        var response = HasMetResponse.newBuilder();

        var user = userRepo.findByUsername(request.getName());

        if (user.isPresent()) {
            response.setHasMet(true);
        }
        else {
            response.setHasMet(false);
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}

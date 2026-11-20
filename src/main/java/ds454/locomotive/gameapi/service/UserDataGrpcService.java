package ds454.locomotive.gameapi.service;

import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ds454.locomotive.generated.grpc.DeleteUserDataRequest;
import ds454.locomotive.generated.grpc.DeleteUserDataResponse;
import ds454.locomotive.generated.grpc.UserDataServiceGrpc;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.gameapi.model.gameApiClient;
import ds454.locomotive.gameapi.util.Result;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;
import ds454.locomotive.generated.grpc.problem.ProblemsList;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class UserDataGrpcService extends UserDataServiceGrpc.UserDataServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(GameGrpcService.class);

    @Override 
    public void deleteUserData(DeleteUserDataRequest request, StreamObserver<DeleteUserDataResponse> out){
        DeleteUserDataResponse.Builder reply = DeleteUserDataResponse.newBuilder();
        try{
            UUID userId;
                userId = UUID.fromString(request.getUserId());
                switch(gameApiClient.get().deleteUserData(userId)){
                    case Result.Success<Boolean, Problem> s -> {}
                    case Result.Failure<Boolean, Problem> f -> reply.setProblem(f.error());
                }
        } catch (IllegalArgumentException e){
                reply.setProblem(Problem.newBuilder().setProblem(ProblemType.ServerError).setMessage("invalid user id").build());
        } catch (Exception e){
                log.error("deleteUserData failed", e);
                reply.setProblem(Problem.newBuilder().setProblem(ProblemType.ServerError).setMessage("deleteUserdata Failed").build());
        }
        out.onNext(reply.build());
        out.onCompleted();
    }
        

    
}

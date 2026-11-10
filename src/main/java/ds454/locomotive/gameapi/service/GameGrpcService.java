package ds454.locomotive.gameapi.service;

import java.util.UUID;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import ds454.locomotive.gameapi.grpc.HeaderInterceptor;
import ds454.locomotive.gameapi.model.Game;
import ds454.locomotive.gameapi.util.Result;
import ds454.locomotive.gameapi.model.gameApiClient;
import ds454.locomotive.generated.grpc.GameList;
import ds454.locomotive.generated.grpc.GameServiceGrpc;
import ds454.locomotive.generated.grpc.GetGamesRequest;
import ds454.locomotive.generated.grpc.GetGamesResponse;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;
import ds454.locomotive.generated.grpc.problem.ProblemsList;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class GameGrpcService extends GameServiceGrpc.GameServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(GameGrpcService.class);

    @Override 
    public void getGames(GetGamesRequest request, StreamObserver<GetGamesResponse> out){
        try{
            String uuidString = HeaderInterceptor.UserUuid.get();
            String steamId = HeaderInterceptor.SteamId.get();

            UUID userId;
            try{
                userId = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e){
                out.onError(Status.UNAUTHENTICATED.withDescription("bad users id").asRuntimeException());
                return;
            }

            GetGamesResponse.Builder reply = GetGamesResponse.newBuilder();

            switch (gameApiClient.get().getGames(userId, steamId)){
                case Result.Success<List<Game>, Problem> s -> {
                    GameList.Builder list = GameList.newBuilder();
                    for (Game g : s.value()){
                        list.addGames(toProto(g));
                    } 
                    reply.setGames(list);
                }
                case Result.Failure<List<Game>, Problem> f -> reply.setProblem(f.error());
            }

            out.onNext(reply.build());
            out.onCompleted();


        } catch (Exception e){
            log.error("getGames Failed :(", 0 );
            out.onError(Status.INTERNAL.withDescription(String.valueOf(e.getMessage())).asRuntimeException());
        }
    }

    private static ds454.locomotive.generated.grpc.Game toProto(Game g){
        return ds454.locomotive.generated.grpc.Game.newBuilder()
            .setAppid(Long.toString(g.getAppId()))
            .setDisplayName(g.getTitle() == null ? "" : g.getTitle())
            .setDescription(g.getDescription() == null ? "" : g.getDescription())
            .setReleaseYear(g.getReleaseYear())
            .build();
    }
    
}

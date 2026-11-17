package ds454.locomotive.gameapi.service;

import java.util.List;
import java.util.UUID;

import ds454.locomotive.gameapi.grpc.HeaderInterceptor;
import ds454.locomotive.gameapi.model.Achievement;
import ds454.locomotive.gameapi.model.UserAchievement;
import ds454.locomotive.gameapi.model.achievementApiClient;
import ds454.locomotive.generated.grpc.GameList;
import ds454.locomotive.generated.grpc.GameServiceGrpc;
import ds454.locomotive.generated.grpc.GetAchievementsRequest;
import ds454.locomotive.generated.grpc.GetAchievementsResponse;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsResponse;
import ds454.locomotive.generated.grpc.UserGameAcheivement;
import ds454.locomotive.generated.grpc.UserGameAcheivementList;
import ds454.locomotive.generated.grpc.AchievementsServiceGrpc;
import ds454.locomotive.generated.grpc.GameAchievement;
import ds454.locomotive.generated.grpc.AchievementsServiceGrpc.AchievementsServiceImplBase;
import ds454.locomotive.generated.grpc.GetGamesResponse;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.gameapi.util.Result;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class AchievementGrpcService extends AchievementsServiceGrpc.AchievementsServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(AchievementGrpcService.class);

    @Override 
    public void getAchievements(GetAchievementsRequest request, StreamObserver<GetAchievementsResponse> out){
        try{
            UUID userId;
            userId = UUID.fromString(HeaderInterceptor.UserUuid.get());
            String steamId = HeaderInterceptor.SteamId.get();
            GetAchievementsResponse.Builder reply = GetAchievementsResponse.newBuilder();

            switch (achievementApiClient.get().getAchievements(userId, steamId, request.getAppId())){
                case Result.Success<List<UserAchievement>, Problem> s -> {
                    UserGameAcheivementList.Builder list = UserGameAcheivementList.newBuilder();
                    for (UserAchievement a : s.value()){
                        var achieve = a.getAchievement();
                        GameAchievement.Builder information = GameAchievement.newBuilder()
                            .setName(achieve.getApiName())
                            .setDisplayName(achieve.getDisplayName() == null ? "" : achieve.getDisplayName())
                            .setDescription(achieve.getDescription() == null ? "" : achieve.getDescription())
                            .setIsCompleted(a.isCompleted());
                        if(a.getTimeCompleted() != null){
                            information.setTimeCompleted(a.getTimeCompleted());
                        }
                        list.addAcheivements(UserGameAcheivement.newBuilder()
                            .setAchievement(information)
                            .setStatus(a.isCompleted())
                            .build()); 
                    } 
                    reply.setAchievements(list);
                }
                case Result.Failure<List<UserAchievement>, Problem> f -> reply.setProblem(f.error());
            }

            out.onNext(reply.build());
            out.onCompleted();

        }catch (Exception e){
            log.error("getAchievements FAILED", 0);
            out.onError(Status.INTERNAL.withDescription(String.valueOf(e.getMessage())).asRuntimeException());
        }
    }


}

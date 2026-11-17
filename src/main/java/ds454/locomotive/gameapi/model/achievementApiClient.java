package ds454.locomotive.gameapi.model;

import ds454.locomotive.gameapi.repository.AchievementRepository;
import ds454.locomotive.gameapi.repository.UserAchievementRepository;
import ds454.locomotive.gameapi.repository.GameRepository;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;
import ds454.locomotive.generated.grpc.UserAcheivements;
import ds454.locomotive.generated.grpc.UserGameAcheivement;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import ds454.locomotive.gameapi.util.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import ds454.locomotive.generated.grpc.problem.Problem;
import ds454.locomotive.generated.grpc.problem.ProblemType;
import ds454.locomotive.generated.grpc.problem.ProblemsList;
import io.grpc.StatusRuntimeException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ds454.locomotive.generated.grpc.AcheivementsGrpc;
import ds454.locomotive.generated.grpc.GameAchievement;
import ds454.locomotive.generated.grpc.GetAllGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetAllGameAcheivementsResponse;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsRequest;
import ds454.locomotive.generated.grpc.GetUserGameAcheivementsResponse;


public class achievementApiClient {
    private static final Logger log = LoggerFactory.getLogger(achievementApiClient.class);

    private final AcheivementsGrpc.AcheivementsBlockingStub steam;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final GameRepository gameRepository;

    private static achievementApiClient instance;
    
    public static void init(String host, int port, GameRepository gameRepository, AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository){
        achievementApiClient.instance = new achievementApiClient(host, port, gameRepository, achievementRepository, userAchievementRepository);
    }

    public static achievementApiClient get(){
        if(instance == null){
            log.error("achievementApiClient not initialized");
            throw new IllegalStateException();
        }
        return instance;
    }

    private achievementApiClient(String host, int port, GameRepository gameRepository, AchievementRepository achievementRepository, UserAchievementRepository userAchievementRepository ){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.steam = AcheivementsGrpc.newBlockingStub(channel);
        this.gameRepository = gameRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    public Result<List<UserAchievement>, Problem> getAchievements(UUID userId, String steamId, long appId){

        //Is the game in gameRepo??
        Optional<Game> found = gameRepository.findByAppId(appId);
        if(found.isEmpty()){
            return new Result.Failure<>(serverError("Game not found. Call get_Games"));
        }

        Game game = found.get();

        //the common achievements
        List<Achievement> common = achievementRepository.findByGameId(game.getId());
        if(common.isEmpty()){
            switch(fetchGameAchievements(game)){
                case Result.Success<List<Achievement>, Problem> s -> common = s.value();
                case Result.Failure<List<Achievement>, Problem> f -> {
                    return new Result.Failure<>(f.error());
                }
            }
        }

        //what has this user unlocked on steam for this game??
        GetUserGameAcheivementsRequest request = GetUserGameAcheivementsRequest.newBuilder().setSteamid(steamId).setSteamappid(Long.toString(appId)).build();
        GetUserGameAcheivementsResponse response;
        try{
            response = steam.getUserGameAcheivements(request);
        }catch(StatusRuntimeException e){
            log.error("Failed to get response from steam-service", e);
            return new Result.Failure<>(
                serverError(e.getStatus().getDescription()));
        }

        //problem?
        if(response.hasProblems()){
            log.warn("Stream problem for user achievements", response.getProblems());
            return new Result.Failure<>(theProblem(response.getProblems()));

        }

        //find it and avoid looping 
        //hashmap will make for quicker lookups
        Map<String, Achievement> byName = new HashMap<>();
        for (Achievement achieve : common){
            byName.put(achieve.getApiName(), achieve);
        }


        //update the row
        Map<Long, UserAchievement> existing = new HashMap<>();
        for(UserAchievement userAch : userAchievementRepository.findByUserIdAndAchievementGameId(userId, game.getId())){
            existing.put(userAch.getAchievement().getId(), userAch);
        }

        List<UserAchievement> toStore = new ArrayList<>();
        for( UserGameAcheivement bySteam : response.getAcheivements().getAcheivementsList()){
            GameAchievement details = bySteam.getAchievement();
            Achievement achievement = byName.get(details.getName());
            //not in the commonplace
            if(achievement == null) continue; 

            UserAchievement row = existing.get(achievement.getId());
            if(row == null){
                row = new UserAchievement(userId, achievement);
            }

            row.setCompleted(bySteam.getStatus());

            if(details.hasTimeCompleted()){
                row.setTimeCompleted(details.getTimeCompleted());
            }else{
                row.setTimeCompleted(null);
            }

            toStore.add(row);

        }
        
        return new Result.Success<List<UserAchievement>,Problem>(userAchievementRepository.saveAll(toStore));
    }

    //gets the game achievements from steam and put them in the database
    private Result<List<Achievement>, Problem> fetchGameAchievements(Game game){

        GetAllGameAcheivementsRequest request = GetAllGameAcheivementsRequest.newBuilder().setAppid((int) game.getAppId()).build();
        GetAllGameAcheivementsResponse response;
        try{
            response = steam.getAllGameAcheivements(request);
        }catch(StatusRuntimeException e){
            log.error("Failed to get response from steam-service", e);
            return new Result.Failure<>(
                serverError(e.getStatus().getDescription()));
        }

        //problem occurs
        if(response.hasProblems()){
            log.warn("steam problem with app", game.getAppId(), response.getProblems());
            return new Result.Failure<>(theProblem(response.getProblems()));

        }

        //store them

        List<Achievement> toStore = response.getAchievements().getAchievementsList().stream()
        .map(achieve -> new Achievement(game, achieve.getName(), achieve.getDisplayName(), achieve.getDescription())).toList();
        return new Result.Success<>(achievementRepository.saveAll(toStore));
        
    }

    //proto return a list so give just the first
    private static Problem theProblem(ProblemsList list){
        if(list.getProblemsCount() > 0){
            return list.getProblems(0);
        }
        //just in case
        return serverError(null);
    }

    //manage the errors
    private static Problem serverError(String message){
        Problem.Builder built =  Problem.newBuilder().setProblem(ProblemType.ServerError);
        if(message != null){
            built.setMessage(message);
        }
        return built.build();
    }
    
}

package ds454.locomotive.gameapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;
import ds454.locomotive.generated.grpc.GameFromSteam;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsResponse;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;

import ds454.locomotive.gameapi.repository.GameRepository;
import ds454.locomotive.gameapi.repository.UserGameRepository;
import ds454.locomotive.gameapi.util.Result;

//gRpc classes
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;



public class gameApiClient {

    private static final Logger log = LoggerFactory.getLogger(gameApiClient.class);

    //for requests with steamService
    private final SteamServiceGrpc.SteamServiceBlockingStub steamService;
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;

    private static gameApiClient instance;

    public static void init(String host, int port, GameRepository gameRepository, UserGameRepository userGameRepository ){
        gameApiClient.instance = new gameApiClient(host, port, gameRepository, userGameRepository);
    }

    //is was gameapi initialized???
    public static gameApiClient get(){
        if(instance == null){
            log.error("gameApiClient not initialized");
            throw new IllegalStateException();
        }
        return instance;
    }

    //connection with steamApi
    private gameApiClient(String host, int port, GameRepository gameRepository, UserGameRepository userGameRepository){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.steamService = SteamServiceGrpc.newBlockingStub(channel);
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
    }

    /*
     * get all games
     * checks database vs the return from steam
     * updates database if the list of games don't match
     * takes in steamId can be changed to uid instead if necessary
     */

    public Result<List<Game>, Problem> getGames(UUID userId, String steamId){

        GetOwnedGameAppIdsRequest request = GetOwnedGameAppIdsRequest.newBuilder().setSteamId(steamId).build();
        GetOwnedGameAppIdsResponse response;

        try{
            response = steamService.getOwnedGameAppIds(request);
        }catch(StatusRuntimeException e){
            log.error("Failed to get response from steam-service", e);
            return new Result.Failure<>(
                Problem.newBuilder().setMessage(e.getStatus().getDescription()).build());
        }

        if(response.hasProblem()){
            return new Result.Failure<>(response.getProblem());
        }

        List<Long> appIds = response.getAppIds().getAppIdsList().stream().map(Integer::longValue).toList();

        //the games that the user is already linked to
        List<Game> userGames = userGameRepository.findByUserId(userId).stream().map(UserGame::getGame).collect(Collectors.toCollection(ArrayList::new));
        Set<Long> linkedApps = userGames.stream().map(Game::getAppId).collect(Collectors.toSet());

        //in steam but not link to the user
        List<Long> unlinkedApps = appIds.stream().filter(id -> !linkedApps.contains(id)).toList();
        if(unlinkedApps.isEmpty()){
            return new Result.Success<>(userGames);
        }
        

        //only link
        List<Game> inDb = gameRepository.findByAppIdIn(unlinkedApps);
        linkGames(userId, inDb);
        userGames.addAll(inDb);

        Set<Long> dbAppids = inDb.stream().map(Game::getAppId).collect(Collectors.toSet());

        //not in the db
        List<Long> toFetchAppList = unlinkedApps.stream().filter(id -> !dbAppids.contains(id)).toList();
        if(toFetchAppList.isEmpty()){
            return new Result.Success<>(userGames); 
        }
        
        GetGamesFromGameAppIdsRequest gameRequest = GetGamesFromGameAppIdsRequest.newBuilder().addAllAppIds(toFetchAppList).build();
        GetGamesFromGameAppIdsResponse gamesResponse;
        try{
            gamesResponse = steamService.getGamesFromGameAppIds(gameRequest);
        }catch(StatusRuntimeException e){
            log.error("Failed to get response from steam-service", e);
            return new Result.Failure<>(
                Problem.newBuilder().setMessage(e.getStatus().getDescription()).build());
        }


        List<Game> newGames = gamesResponse.getGamesList().stream().map(gameApiClient::toGame).toList();
        List<Game> savedGames = gameRepository.saveAll(newGames);
        linkGames(userId, savedGames);
        userGames.addAll(savedGames);
        
        
        return new Result.Success<>(userGames);
    }

    private void linkGames(UUID userId, List<Game> games){
        if(games.isEmpty()) return;
        List<UserGame> links = games.stream().map(game-> new UserGame(userId, game)).toList();
        userGameRepository.saveAll(links);
    }

    private static Game toGame (GameFromSteam g){
        return new Game(g.getAppId(), g.getName(), g.getDescription(), g.getReleaseYear());
    }
}



package ds454.locomotive.gameapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ds454.locomotive.generated.grpc.Problem;
import ds454.locomotive.generated.grpc.ProblemType;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetGamesFromGameAppIdsResponse;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsRequest;
import ds454.locomotive.generated.grpc.GetOwnedGameAppIdsResponse;
import ds454.locomotive.generated.grpc.SteamServiceGrpc;

import ds454.locomotive.gameapi.util.Result;

//gRpc classes?
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;



public class gameApiClient {

    private static final Logger log = LoggerFactory.getLogger(gameApiClient.class)

    //for requests with steamService
    private final SteamServiceGrpc.SteamServiceBlockingStub steamService;

    private static gameApiClient instance;

    public static void init(String host, int port){
        gameApiClient.instance = new gameApiClient(host, port);
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
    private gameApiClient(String host, int port){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        //stub
        this.steamService = SteamServiceGrpc.newBlockingStub(channel);
    }

    /*
     * get all games
     * checks database vs the return from steam
     * updates database if the list of games don't match
     * takes in steamId can be changed to uid instead if necessary
     */


    //TODO: FINISH THIS BUT I HAVE QUESTIONS SO I STOPPED
    public Result<List<Game>, Problem> getGames(String steamId){
        //get the games from steam
        GetOwnedGameAppIdsRequest ownedGamesRequest =
            GetOwnedGameAppIdsRequest.newBuilder().setSteamId(steamId).build();
        GetOwnedGameAppIdsResponse ownedGameResponse; 
        try{
            ownedGameResponse = steamService.getOwnedGameAppIds(ownedGamesRequest);
            
        }catch(StatusRuntimeException e){
            log.error("Failed to pull games from steamApi", e);
            return new Result.Failure<>(Problem.newBuilder().setMessage(e.getStatus().getDescription()).build());
        }

        if(ownedGameResponse.hasProblem()){
            return new Result.Failure<>(ownedGameResponse.getProblem());
        }

        //get the list
        List<Integer> ownedGamesIds = new ArrayList<>(ownedGameResponse.getAppIds().getAppIdsList());
        
     

    }




    
}


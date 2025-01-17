package edu.usfca.cs.dfs.storageNode;

import com.google.protobuf.ByteString;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.dfs.storageNode.data.ChunkFileMeta;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StorageStorageMessagesHelper {
//
////    private String nodeType;
////    private String address;
////    private int port;
////    private String connectingAddress;
////    private int connectingPort;
//
//
//    public StorageStorageMessagesHelper() {
//    }
//
////    public StorageClientHelper(String nodeType, String address, String port) {
////        this.nodeType = nodeType;
////        this.address = address;
////        this.port = port;
////    }

    public static StorageMessages.StorageMessageWrapper prepareHeartBeat
            (String address, int port, long spaceRemaining, long requestProcessed, long retrievalProcessed) {
        StorageMessages.HeartBeat heartBeat = StorageMessages.HeartBeat.newBuilder() // building heartbeat
                .setIpAddress(address)
                .setPort(String.valueOf(port))
                .setSpaceRemaining(spaceRemaining)  // new File("\").getSpace
                .setRequestProcessed(requestProcessed)
                .setRetrievalProcessed(retrievalProcessed)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setHeartBeatMsg(heartBeat)
                        .build();

        return msgWrapper;
    }


    public static StorageMessages.StorageMessageWrapper prepareStoreChunkMsgForReplica(StorageMessages.StorageMessageWrapper msg, int toReplicaNo) {

        StorageMessages.StoreChunk recvStoreChunk = msg.getStoreChunkMsg();

        StorageNodeDS.getInstance().logger.log(Level.INFO,"Size of StorageNodeIds : "+ recvStoreChunk.getStorageNodeIdsList().size());
        StorageMessages.StoreChunk storeChunkMsg
                = StorageMessages.StoreChunk.newBuilder()
                .setFileName(recvStoreChunk.getFileName())
                .setChunkId(recvStoreChunk.getChunkId())
                .setChunkSize(recvStoreChunk.getChunkSize())
                .setTotalChunks(recvStoreChunk.getTotalChunks())
                .addAllStorageNodeIds(recvStoreChunk.getStorageNodeIdsList())
                .setData(recvStoreChunk.getData())
                .setToStorageNodeId(recvStoreChunk.getStorageNodeIdsList().get(toReplicaNo))
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setStoreChunkMsg(storeChunkMsg)
                        .build();

        return msgWrapper;
    }

    public static StorageMessages.StorageMessageWrapper prepareChunkMetaInfo(ChunkFileMeta chunkFileMeta){
        StorageMessages.StorageChunkMeta chunkMetaInfo = StorageMessages.StorageChunkMeta.newBuilder()
                .setFileName(chunkFileMeta.getFileName())
                .setChunkId(chunkFileMeta.getChunkId())
                .setTotalChunks(chunkFileMeta.getTotalChunks()).build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setStorageChunkMetaMsg(chunkMetaInfo)
                        .build();

        return msgWrapper;
    }

    public static StorageMessages.StorageMessageWrapper prepareChunkMsg(String fileChunkId, ByteBuffer buff){
        StorageMessages.Chunk chunk = StorageMessages.Chunk.newBuilder()
                .setFound(true)
                .setFileChunkId(fileChunkId)
                .setData(ByteString.copyFrom(buff))
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setChunkMsg(chunk)
                        .build();

        return msgWrapper;
    }


    public static StorageMessages.StorageMessageWrapper prepareChunkForBadChunkMsg(String fileChunkId, ByteBuffer buff, String primaryId){
        StorageMessages.ChunkForBadChunk chunkForBadChunk = StorageMessages.ChunkForBadChunk.newBuilder()
                .setFound(true)
                .setFileChunkId(fileChunkId)
                .setData(ByteString.copyFrom(buff))
                .setPrimaryIdForChunk(primaryId)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setChunkForBadChunkMsg(chunkForBadChunk)
                        .build();

        return msgWrapper;
    }

    public static StorageMessages.StorageMessageWrapper prepareChunkNotFoundMsg(String fileChunkId, List<String> storageIds){
        StorageNodeDS.getInstance().logger.log(Level.INFO,"Sending updated storage node ids in prepareChunkNotFoundMsg : - > ");
        for(int i = 0; i < storageIds.size(); i++) {
            System.out.println(storageIds.get(i));
        }


        StorageMessages.Chunk chunk = StorageMessages.Chunk.newBuilder()
                .setFound(false)
                .setFileChunkId(fileChunkId)
                .addAllStorageNodeIds(storageIds)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setChunkMsg(chunk)
                        .build();

        return msgWrapper;
    }


    public static StorageMessages.StorageMessageWrapper buildNewPrimaryAlert(String forAddress, String forPort){

        StorageMessages.NewPrimaryAlert newPrimaryAlertMsg = StorageMessages.NewPrimaryAlert.newBuilder()
                .setForIpAddress(forAddress)
                .setForPort(forPort)
                .setNewIpAddress(StorageNodeDS.getInstance().getIpAddress())
                .setNewPort(String.valueOf(StorageNodeDS.getInstance().getPort()))
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setNewPrimaryAlertMsg(newPrimaryAlertMsg)
                        .build();

        return msgWrapper;
    }


    public static StorageMessages.StorageMessageWrapper prepareStoreChunkMsg(ChunkFileMeta cmMsg, ByteBuffer buffer, String newReplicaId) {
    //todo


        List<String> storageNodeIds = new ArrayList<>();
        storageNodeIds.add(cmMsg.getStorageNodeIds().get(0));
        storageNodeIds.add(newReplicaId);
        StorageNodeDS.getInstance().logger.log(Level.INFO,"Size of StorageNodeIds : "+ storageNodeIds.size());


        StorageMessages.StoreChunk storeChunkMsg
                = StorageMessages.StoreChunk.newBuilder()
                .setFileName(cmMsg.getFileName())
                .setChunkId(cmMsg.getChunkId())
                .setChunkSize(cmMsg.getChunkSize())
                .setTotalChunks(cmMsg.getTotalChunks())
                .addAllStorageNodeIds(storageNodeIds) // new storage id at 0
                .setData(ByteString.copyFrom(buffer))
                .setToStorageNodeId(cmMsg.getStorageNodeIds().get(1))
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setStoreChunkMsg(storeChunkMsg)
                        .build();

        return msgWrapper;
    }

    public static StorageMessages.StorageMessageWrapper prepareStoreChunkMsg(String nodeId, List<String> storageNodes,ChunkFileMeta chunkFileMeta, ByteBuffer buff){
        StorageMessages.StoreChunk  storeChunk = StorageMessages.StoreChunk.newBuilder()
                .setFileName(chunkFileMeta.getFileName())
                .setChunkId(chunkFileMeta.getChunkId())
                .setChunkSize(chunkFileMeta.getChunkSize())
                .setTotalChunks(chunkFileMeta.getTotalChunks())
                .addAllStorageNodeIds(storageNodes)
                .setData(ByteString.copyFrom(buff))
                .setToStorageNodeId(nodeId)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.newBuilder()
                .setStoreChunkMsg(storeChunk)
                .build();

        return msgWrapper;
    }

    public static StorageMessages.StorageMessageWrapper prepareBadChunkFoundMsg(String nodeId, String fileChunkId, String primaryNode){
        StorageMessages.BadChunkFound  badChunkFound = StorageMessages.BadChunkFound.newBuilder()
                .setSelfId(nodeId)
                .setFileChunkId(fileChunkId)
                .setPrimaryIdForChunk(primaryNode)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.newBuilder()
                .setBadChunkFoundMsg(badChunkFound)
                .build();

        return msgWrapper;
    }


    public static StorageMessages.StorageMessageWrapper prepareRetrieveChunkForBadChunk(String fileChunkId, List<String> storageIds, String primaryId){

        StorageMessages.RetrieveChunkForBadChunk retrieveChunkForBadChunk = StorageMessages.RetrieveChunkForBadChunk.newBuilder()
                .setFileChunkId(fileChunkId)
                .addAllStorageNodeIds(storageIds)
                .setPrimaryNode(primaryId)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.newBuilder()
                .setRetrieveChunkForBadChunk(retrieveChunkForBadChunk)
                .build();

        return msgWrapper;
    }


    public static StorageMessages.StorageMessageWrapper prepareChunkStoredMsg(String fileChunkId){

        StorageMessages.ChunkStored chunkStored = StorageMessages.ChunkStored.newBuilder()
                .setFileChunkId(fileChunkId)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.newBuilder()
                .setChunkStoredMsg(chunkStored)
                .build();

        return msgWrapper;
    }


}

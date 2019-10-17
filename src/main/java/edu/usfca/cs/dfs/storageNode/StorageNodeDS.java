package edu.usfca.cs.dfs.storageNode;

import edu.usfca.cs.dfs.data.NodeId;
import edu.usfca.cs.dfs.fileUtil.Fileify;
import edu.usfca.cs.dfs.init.ConfigSystemParam;
import edu.usfca.cs.dfs.storageNode.data.ChunkFileMeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;


public class StorageNodeDS {

    private static StorageNodeDS storageNodeDS = null;

    private String nodeId;
    private String ipAddress;
    private int port;
    private String controllerIpAddress;
    private int controllerPort;
    private String basePath;

    private long spaceRemaining;
    private LongAdder requestProcessed;
    private LongAdder retrievalProcessed;

    //private int healed;
    private Map<String, ChunkFileMeta> chunksMetaInfo;// = new HashMap<>();
    private Map<String, Integer> healingMap;
    private StorageNodeDS(){
    }

    private void setNodeParam(ConfigSystemParam nodeParam) {
        this.ipAddress = nodeParam.getAddress();
        this.port = nodeParam.getPort();
        this.nodeId = NodeId.getId(this.ipAddress, this.port);
        this.controllerIpAddress = nodeParam.getControllerAddress();
        this.controllerPort = nodeParam.getControllerPort();

        this.basePath = System.getProperty("user.home")+"/sfdfs_"+ NodeId.getId(this.ipAddress, this.port)+"/";
        Fileify.deleteDirectory(basePath);
        Fileify.createDirectory(basePath);
        //this.healed = 1;
        storageNodeDS.chunksMetaInfo = new ConcurrentHashMap<>();
        storageNodeDS.healingMap = new ConcurrentHashMap<>();

    }

//    public int getHealed() {
//        return healed;
//    }
//
//    public void setHealed(int healed) {
//        this.healed = healed;
//    }

    public int getHealed(String fileChunkId) {
        return healingMap.get(fileChunkId);
    }

    public void setHealed(String fileChunkId, int isHealed) {
        healingMap.put(fileChunkId, isHealed);
    }


    public static void setInstance(ConfigSystemParam nodeParam) {
        if(storageNodeDS == null){
            storageNodeDS =  new StorageNodeDS();
            storageNodeDS.setNodeParam(nodeParam);
        }
    }

    public static StorageNodeDS getInstance(){
        if(storageNodeDS == null){
            storageNodeDS =  new StorageNodeDS();
        }
        return storageNodeDS;
    }


    public String getIpAddress() {
        return storageNodeDS.ipAddress;
    }


    public String getNodeId() {
        return storageNodeDS.nodeId;
    }

    public int getPort() {
        return storageNodeDS.port;
    }

    public String getControllerIpAddress() {
        return storageNodeDS.controllerIpAddress;
    }

    public int getControllerPort() {
        return storageNodeDS.controllerPort;
    }

    public String getBasePath() {
        return storageNodeDS.basePath;
    }

    public long getRequestProcessed() {
        return storageNodeDS.requestProcessed.sum();
    }

    public void addToRequestProcessed() {
        storageNodeDS.requestProcessed.add(1);
    }

    public long getRetrievalProcessed() {
        return storageNodeDS.retrievalProcessed.sum();
    }

    public void addToRetrievalProcessed() {
        storageNodeDS.retrievalProcessed.add(1);
    }

    public long getSpaceRemaining() {
        Path path = Paths.get(storageNodeDS.basePath);
        try {
            storageNodeDS.spaceRemaining = Files.getFileStore(path).getUsableSpace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return storageNodeDS.spaceRemaining;
    }

    //Map<NameOfDirectoriesHavingChunks, Map<ChunkFileName,ChunkMetadata>
    public Map<String,ChunkFileMeta> getChunksMetaInfo(){
        return storageNodeDS.chunksMetaInfo;
    }

    public ChunkFileMeta getChunkMetaInfo(String chunkFileName){
        ChunkFileMeta chunkFileMeta = null;
            if (storageNodeDS.chunksMetaInfo.containsKey(chunkFileName)) {
                chunkFileMeta = storageNodeDS.chunksMetaInfo.get(chunkFileName);
            }
        return chunkFileMeta;
    }

    ////

    public void keepSendingHeartBeat() {
        Timer timer = new Timer();
        timer.schedule(
                new HeartBeatSender(
                        StorageNodeDS.getInstance().getControllerIpAddress(),
                        StorageNodeDS.getInstance().getControllerPort(),
                        StorageStorageMessagesHelper.prepareHeartBeat(
                                StorageNodeDS.getInstance().getIpAddress(),
                                StorageNodeDS.getInstance().getPort(),
                                StorageNodeDS.getInstance().getSpaceRemaining(),
                                StorageNodeDS.getInstance().getRequestProcessed(),
                                StorageNodeDS.getInstance().getRetrievalProcessed()


                        )
                ),
                0,
                5000);
    }




}

package org.xtreemfs.mrc.operations;

import org.xtreemfs.foundation.logging.Logging;
import org.xtreemfs.mrc.MRCRequest;
import org.xtreemfs.mrc.MRCRequestDispatcher;
import org.xtreemfs.mrc.database.StorageManager;
import org.xtreemfs.mrc.database.VolumeManager;
import org.xtreemfs.mrc.metadata.XLoc;
import org.xtreemfs.mrc.utils.MRCHelper;
import org.xtreemfs.osd.storage.FileMetadata;
import org.xtreemfs.pbrpc.generatedinterfaces.MRC
        .xtreemfs_replica_mark_completeRequest;

import org.xtreemfs.pbrpc.generatedinterfaces.Common.emptyResponse;

public class MarkReplicaCompleteOperation extends MRCOperation {


    public MarkReplicaCompleteOperation(MRCRequestDispatcher master) {
        super(master);
    }

    @Override
    public void startRequest(MRCRequest rq) throws Throwable {
        final xtreemfs_replica_mark_completeRequest rqArgs =
                (xtreemfs_replica_mark_completeRequest) rq.getRequestArgs();

        if (Logging.isDebug()) {
            Logging.logMessage(Logging.LEVEL_DEBUG, this,
                               "Replica on OSD %s of file with id %s is now " +
                                       "marked as complete",
                               rqArgs.getOsdUuid(), rqArgs.getFileId());
        }

        String fileID = rqArgs.getFileId();

        MRCHelper.GlobalFileIdResolver idResolver = new MRCHelper
                .GlobalFileIdResolver(fileID);

        VolumeManager volumeManager = master.getVolumeManager();

        StorageManager storageManager =
                volumeManager.getStorageManager(idResolver.getVolumeId());

        org.xtreemfs.mrc.metadata.FileMetadata fileMetadata =
                storageManager.getMetadata(idResolver.getLocalFileId());

        StringBuilder replicaInfo = new StringBuilder();

        if (Logging.isDebug()) {
            for (int i = 0; i < fileMetadata.getXLocList().getReplicaCount(); i++) {

                replicaInfo
                        .append("replica ")
                        .append(i)
                        .append(" osd: ")
                        .append(fileMetadata.getXLocList().getReplica(i)
                                        .getOSD(0))
                        .append(" replication flags: ")
                        .append(fileMetadata.getXLocList().getReplica(i).getReplicationFlags())
                        .append("\n");
            }

            Logging.logMessage(Logging.LEVEL_DEBUG, this,
                               replicaInfo.toString());
        }

        rq.setResponse(emptyResponse.getDefaultInstance());
    }
}

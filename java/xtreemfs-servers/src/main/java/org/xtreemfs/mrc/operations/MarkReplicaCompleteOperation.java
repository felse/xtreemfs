package org.xtreemfs.mrc.operations;

import org.xtreemfs.foundation.logging.Logging;
import org.xtreemfs.mrc.MRCRequest;
import org.xtreemfs.mrc.MRCRequestDispatcher;
import org.xtreemfs.mrc.database.AtomicDBUpdate;
import org.xtreemfs.mrc.database.StorageManager;
import org.xtreemfs.mrc.database.VolumeManager;
import org.xtreemfs.mrc.metadata.XLocList;
import org.xtreemfs.mrc.utils.MRCHelper;
import org.xtreemfs.pbrpc.generatedinterfaces.MRC
        .xtreemfs_replica_mark_completeRequest;

import org.xtreemfs.pbrpc.generatedinterfaces.Common.emptyResponse;

/**
 * MRC operation to mark a previously added replica as a complete replica.
 * This operation should only be used by an OSD when it fetched and stored all
 * objects locally (e.g. when the replication completed successfully).
 * <p>
 * This operation is only necessary for read-only replication.
 * Probably we should throw some exception if the file's replication policy is
 * not read-only.
 */
public class MarkReplicaCompleteOperation extends MRCOperation {


    public MarkReplicaCompleteOperation(MRCRequestDispatcher master) {
        super(master);
    }

    @Override
    public void startRequest(MRCRequest rq) throws Throwable {
        final xtreemfs_replica_mark_completeRequest rqArgs =
                (xtreemfs_replica_mark_completeRequest) rq.getRequestArgs();

        String fileID = rqArgs.getFileId();
        String osdWithCompleteReplica = rqArgs.getOsdUuid();

        if (Logging.isDebug()) {
            Logging.logMessage(Logging.LEVEL_DEBUG, this,
                               "Replica on OSD %s of file with id %s is now " +
                                       "marked as complete",
                               osdWithCompleteReplica, fileID);
        }

        MRCHelper.GlobalFileIdResolver idResolver = new MRCHelper
                .GlobalFileIdResolver(fileID);

        VolumeManager volumeManager = master.getVolumeManager();

        StorageManager storageManager =
                volumeManager.getStorageManager(idResolver.getVolumeId());

        org.xtreemfs.mrc.metadata.FileMetadata fileMetadata =
                storageManager.getMetadata(idResolver.getLocalFileId());

        XLocList xLocList = fileMetadata.getXLocList();

        if (Logging.isDebug()) {
            logXLocInfo(xLocList);
        }

//        // assume that there is exactly one entry in the xLocList for
//        // OSD osdWithCompleteReplica
//        XLoc replicaToBeUpdated = null;
//        for (int i = 0; i < xLocList.getReplicaCount(); i++) {
//            if (osdWithCompleteReplica
//                    .equals(xLocList.getReplica(i).getOSD(i))) {
//                replicaToBeUpdated = xLocList.getReplica(i);
//                break;
//            }
//        }
//
//        if (replicaToBeUpdated == null) {
//            // TODO throw some exception?
//            // or just ignore?
//            return;
//        }

        // it seems that the MRCHelper contains code to mark a replica as
        // complete in the MetaDataDataBase
        AtomicDBUpdate atomicDBUpdate =
                storageManager.createAtomicDBUpdate(master, rq);
        MRCHelper.setSysAttrValue(master,
                                  storageManager,
                                  -1,
                                  fileMetadata,
                                  MRCHelper.SysAttrs.mark_replica_complete.toString(),
                                  osdWithCompleteReplica,
                                  atomicDBUpdate);

        rq.setResponse(emptyResponse.getDefaultInstance());
    }

    private void logXLocInfo(XLocList xLocList) {
        StringBuilder replicaInfo = new StringBuilder();
        for (int i = 0; i < xLocList.getReplicaCount(); i++) {
            replicaInfo
                    .append("replica ")
                    .append(i)
                    .append(" osd: ")
                    .append(xLocList.getReplica(i)
                                    .getOSD(0))
                    .append(" replication flags: ")
                    .append(xLocList.getReplica(i)
                                    .getReplicationFlags())
                    .append("\n");
        }
        Logging.logMessage(Logging.LEVEL_DEBUG, this,
                           replicaInfo.toString());
    }
}

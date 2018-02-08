package org.xtreemfs.mrc.operations;

import org.xtreemfs.foundation.logging.Logging;
import org.xtreemfs.mrc.MRCRequest;
import org.xtreemfs.mrc.MRCRequestDispatcher;
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
                               rqArgs.getFileId(), rqArgs.getOsdUuid());
        }

        rq.setResponse(emptyResponse.getDefaultInstance());
    }
}

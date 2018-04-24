package org.xtreemfs.utils;

import org.xtreemfs.osd.OSDConfig;
import org.xtreemfs.osd.storage.HashStorageLayout;
import org.xtreemfs.osd.storage.MetadataCache;

import java.io.IOException;

public class StorageLayoutDebugHelper {

    public static void main(String[] args) {
        String OSDConfigFile = args[0];
        String fileID = args[1];

        MetadataCache cache = new MetadataCache();

        OSDConfig OSDConfig = null;
        HashStorageLayout storageLayout = null;

        try {
            OSDConfig = new OSDConfig(OSDConfigFile);
            storageLayout = new HashStorageLayout(OSDConfig, cache);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert storageLayout != null;
        System.out.println(storageLayout.generateAbsoluteFilePath(fileID));
    }

}

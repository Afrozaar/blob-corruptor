package com.afrozaar.util.blobcorrupt;

import com.microsoft.windowsazure.services.blob.client.BlobInputStream;
import com.microsoft.windowsazure.services.blob.client.BlobOutputStream;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class AzureAuditRepositoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(AzureAuditRepositoryTest.class);

    private static final String DEFAULT_CONTAINER = "test-corrupt";

    private static final String DEFAULT_BLOB_URL = "testblob.blob";;

    private static CloudStorageAccount account;
    private static CloudBlobClient cloudBlobClient;
    private CloudBlobContainer containerReference;

    private static String container;

    private static String blobUrl;

    private static Properties properties = new Properties();

    @BeforeClass
    public static void initialiseAzureAuditRepository() throws Exception {

        File name = new File("blobconfig.properties");
        LOG.info("loading properties from file {}", name.getAbsolutePath());
        properties.load(new FileInputStream(name));

        container = System.getProperty("container");
        if (container == null) {
            container = properties.getProperty("container");
        }
        if (container == null) {
            container = DEFAULT_CONTAINER;
        }
        LOG.info("container= {}", container);
        blobUrl = System.getProperty("blobUrl");
        if (blobUrl == null) {
            blobUrl = properties.getProperty("blobUrl");
        }
        if (blobUrl == null) {
            blobUrl = DEFAULT_BLOB_URL;
        }
        LOG.info("blobUrl={}", blobUrl);

        String storageConnectionString = System.getProperty("storageAccountString");
        if (storageConnectionString == null) {
            storageConnectionString = properties.getProperty("storageAccountString");
        }
        LOG.info("storage connection string={}", storageConnectionString);

        account = CloudStorageAccount.parse(storageConnectionString);
        cloudBlobClient = account.createCloudBlobClient();

    }

    @Test
    public void test() throws URISyntaxException, StorageException, IOException, InterruptedException {
        containerReference = cloudBlobClient.getContainerReference(container);
        containerReference.createIfNotExist();

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        writeBlob();
                    }
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (StorageException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();
        Thread.sleep(2000);

        while (true) {
            Thread.sleep(5000);
        }
    }

    /**
     * @throws URISyntaxException
     * @throws StorageException
     * @throws IOException
     */
    private void writeBlob() throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blockBlobReference = containerReference.getBlockBlobReference(blobUrl);
        BlobOutputStream openOutputStream = blockBlobReference.openOutputStream();
        openOutputStream.write("alksjdflkasdjflaieujrol87U90O2Q84375R0OPIASrjhflkaisdjhfalskdjhfasldkfjasldfkj".getBytes());
        openOutputStream.close();
        LOG.info("written {}", blockBlobReference.getUri());
    }

    @Test
    public void readBlob() throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blockBlobReference = containerReference.getBlockBlobReference(blobUrl);
        BlobInputStream openInputStream = blockBlobReference.openInputStream();
        LOG.info("blob contents: {}", IOUtils.toString(openInputStream));
    }
}
package com.developmentontheedge.be5.modules.core.mcp;

import org.apache.commons.fileupload.FileItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class McpFileStoreTest
{
    private final McpFileStore store = new McpFileStore();

    @Test
    public void linkWorksOnceForItsUser()
    {
        McpFileStore.Entry entry = store.create("anna", "Contract");
        assertTrue(entry.fileId.startsWith("file_"));
        assertNotEquals(entry.fileId, entry.token);
        assertSame(entry, store.forUpload(entry.token));
        assertTrue(error(() -> store.get(entry.fileId, "anna")).contains("is not uploaded yet"));

        FileItem file = mock(FileItem.class);
        store.upload(entry, file);
        assertSame(file, store.get(entry.fileId, "anna"));
        assertSame("can be used again", file, store.get(entry.fileId, "anna"));
        assertNull("the link works once", store.forUpload(entry.token));

        FileItem second = mock(FileItem.class);
        assertTrue(error(() -> store.upload(entry, second)).contains("used or expired"));
        verify(second).delete();

        assertTrue(error(() -> store.get(entry.fileId, "boris")).startsWith("Unknown or expired file"));
        assertTrue(error(() -> store.get("file_unknown", "anna")).startsWith("Unknown or expired file"));
        assertNull(store.forUpload("unknown"));
        assertNull(store.forUpload(null));
    }

    @Test
    public void expiredFilesAreDeleted()
    {
        McpFileStore.Entry link = store.create("anna", null);
        link.expiresAt = 0;
        assertNull(store.forUpload(link.token));

        McpFileStore.Entry entry = store.create("anna", null);
        FileItem file = mock(FileItem.class);
        store.upload(entry, file);
        verify(file, never()).delete();
        entry.expiresAt = 0;
        assertTrue(error(() -> store.get(entry.fileId, "anna")).startsWith("Unknown or expired file"));

        store.purgeExpired();
        verify(file).delete();
    }

    @Test
    public void filesOfAUserAreLimited()
    {
        for (int i = 0; i < McpFileStore.MAX_FILES_PER_USER; i++) store.create("anna", null);
        assertTrue(error(() -> store.create("anna", null)).startsWith("At most"));
        store.create("boris", null);
    }

    @Test
    public void maxSize()
    {
        assertEquals(McpFileStore.DEFAULT_MAX_SIZE, McpFileStore.maxSize());
        System.setProperty(McpFileStore.MAX_SIZE_PROPERTY, "1000");
        try
        {
            assertEquals(1000, McpFileStore.maxSize());
            System.setProperty(McpFileStore.MAX_SIZE_PROPERTY, "a lot");
            assertEquals(McpFileStore.DEFAULT_MAX_SIZE, McpFileStore.maxSize());
        }
        finally
        {
            System.clearProperty(McpFileStore.MAX_SIZE_PROPERTY);
        }
    }

    private static String error(Runnable call)
    {
        try
        {
            call.run();
        }
        catch (RuntimeException e)
        {
            return e.getMessage();
        }
        fail("no error");
        return null;
    }
}

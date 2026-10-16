package com.developmentontheedge.be5.modules.core.mcp;

import org.apache.commons.fileupload.FileItem;

import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Files an agent gives to operations, uploaded by a one-time link ({@link McpUploadServlet}) and then passed
 * by their id in the values of execute_operation. Kept in memory and temporary files of this node only:
 * they are lost on a restart, as the OAuth tokens are.
 */
@Singleton
public class McpFileStore
{
    private static final Logger log = Logger.getLogger(McpFileStore.class.getName());

    /** The largest file accepted, in bytes. */
    public static final String MAX_SIZE_PROPERTY = "be5.mcp.files.maxSize";
    static final long DEFAULT_MAX_SIZE = 20L * 1024 * 1024;

    /** How long a link waits for the file. */
    static final long UPLOAD_TTL_MS = 15 * 60 * 1000L;
    /** How long an uploaded file can be used, by several operations or attempts. */
    static final long FILE_TTL_MS = 60 * 60 * 1000L;
    static final int MAX_FILES_PER_USER = 20;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Entry> byId = new ConcurrentHashMap<>();
    private final Map<String, Entry> byToken = new ConcurrentHashMap<>();

    /**
     * A file of a user: first a link (the token) to upload it, then the file.
     */
    public static final class Entry
    {
        public final String fileId;
        final String token;
        final String userName;
        public final String purpose;
        volatile long expiresAt;
        private volatile FileItem file;

        Entry(String fileId, String token, String userName, String purpose)
        {
            this.fileId = fileId;
            this.token = token;
            this.userName = userName;
            this.purpose = purpose;
            this.expiresAt = System.currentTimeMillis() + UPLOAD_TTL_MS;
        }

        boolean expired()
        {
            return System.currentTimeMillis() > expiresAt;
        }

        public FileItem getFile()
        {
            return file;
        }
    }

    /**
     * A new link for the user to upload a file with.
     *
     * @param purpose what the file is for, shown on the upload page
     */
    public Entry create(String userName, String purpose)
    {
        purgeExpired();
        long files = byId.values().stream().filter(entry -> entry.userName.equals(userName)).count();
        if (files >= MAX_FILES_PER_USER)
        {
            throw new IllegalStateException("At most " + MAX_FILES_PER_USER
                    + " files can be uploaded within an hour, use the ones uploaded already");
        }
        Entry entry = new Entry("file_" + randomToken(18), randomToken(32), userName, purpose);
        byId.put(entry.fileId, entry);
        byToken.put(entry.token, entry);
        return entry;
    }

    /**
     * The entry the link with this token is for, while a file can be uploaded with it; otherwise null.
     */
    public Entry forUpload(String token)
    {
        Entry entry = token == null ? null : byToken.get(token);
        return entry == null || entry.expired() || entry.file != null ? null : entry;
    }

    /**
     * Keeps the file uploaded by the link of the entry, which cannot be used again.
     */
    public void upload(Entry entry, FileItem file)
    {
        synchronized (entry)
        {
            if (entry.file != null || entry.expired())
            {
                file.delete();
                throw new IllegalStateException("The link is used or expired");
            }
            entry.file = file;
            entry.expiresAt = System.currentTimeMillis() + FILE_TTL_MS;
        }
        byToken.remove(entry.token);
        log.info("MCP file " + entry.fileId + " uploaded user=" + entry.userName + " name=" + file.getName()
                + " size=" + file.getSize());
    }

    /**
     * The uploaded file of the user with the id.
     */
    public FileItem get(String fileId, String userName)
    {
        Entry entry = fileId == null ? null : byId.get(fileId);
        if (entry == null || entry.expired() || !entry.userName.equals(userName))
        {
            throw new IllegalArgumentException("Unknown or expired file " + fileId + ", get a new link with upload_file");
        }
        FileItem file = entry.file;
        if (file == null)
        {
            throw new IllegalArgumentException("File " + fileId + " is not uploaded yet: the user should open the link "
                    + "from upload_file and choose the file (or upload it with curl -F file=@<path> <link>)");
        }
        return file;
    }

    public static long maxSize()
    {
        String value = System.getProperty(MAX_SIZE_PROPERTY);
        try
        {
            return value == null || value.trim().isEmpty() ? DEFAULT_MAX_SIZE : Long.parseLong(value.trim());
        }
        catch (NumberFormatException e)
        {
            log.warning(MAX_SIZE_PROPERTY + "=" + value + " is not a number, " + DEFAULT_MAX_SIZE + " is used");
            return DEFAULT_MAX_SIZE;
        }
    }

    void purgeExpired()
    {
        byToken.values().removeIf(Entry::expired);
        byId.values().removeIf(entry -> {
            if (!entry.expired()) return false;
            FileItem file = entry.file;
            if (file != null) file.delete();
            return true;
        });
    }

    private static String randomToken(int bytes)
    {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}

package com.developmentontheedge.be5.modules.core.mcp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one-time links of the upload_file tool, {@code /api/mcp/upload/<token>}: GET shows a page to choose
 * the file in a browser, POST takes it as multipart/form-data (the page, or {@code curl -F file=@<path> <link>}).
 * The link itself is the credential: it is long, random, expires and works once.
 */
@Singleton
public class McpUploadServlet extends HttpServlet
{
    private static final Jsonb JSONB = JsonbBuilder.create();

    private final McpFileStore fileStore;

    @Inject
    public McpUploadServlet(McpFileStore fileStore)
    {
        this.fileStore = fileStore;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        McpFileStore.Entry entry = fileStore.forUpload(token(req));
        if (entry == null)
        {
            sendPage(resp, HttpServletResponse.SC_NOT_FOUND, "<p>This link is not valid any more: it was used "
                    + "or it expired. Ask the assistant for a new one.</p>");
            return;
        }
        String purpose = entry.purpose == null || entry.purpose.trim().isEmpty() ? ""
                : "<p class=\"purpose\">" + escape(entry.purpose) + "</p>";
        sendPage(resp, HttpServletResponse.SC_OK, purpose
                + "<form method=\"post\" enctype=\"multipart/form-data\">"
                + "<input type=\"file\" name=\"file\" required> <button type=\"submit\">Upload</button></form>"
                + "<p class=\"note\">At most " + megabytes(McpFileStore.maxSize()) + ". The link works once.</p>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        boolean page = wantsPage(req);
        McpFileStore.Entry entry = fileStore.forUpload(token(req));
        if (entry == null)
        {
            sendResult(resp, page, HttpServletResponse.SC_NOT_FOUND, null,
                    "This link is not valid any more: it was used or it expired");
            return;
        }
        if (!ServletFileUpload.isMultipartContent(req))
        {
            sendResult(resp, page, HttpServletResponse.SC_BAD_REQUEST, null,
                    "Send the file as multipart/form-data, e.g. curl -F file=@report.pdf '<link>'");
            return;
        }

        long maxSize = McpFileStore.maxSize();
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(maxSize);
        upload.setSizeMax(maxSize + 64 * 1024);
        FileItem file = null;
        try
        {
            List<FileItem> items = upload.parseRequest(req);
            for (FileItem item : items)
            {
                if (file == null && !item.isFormField() && item.getName() != null && !item.getName().isEmpty())
                {
                    file = item;
                }
                else
                {
                    item.delete();
                }
            }
        }
        catch (FileUploadBase.SizeLimitExceededException | FileUploadBase.FileSizeLimitExceededException e)
        {
            sendResult(resp, page, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, null,
                    "The file is larger than " + megabytes(maxSize));
            return;
        }
        catch (FileUploadException e)
        {
            sendResult(resp, page, HttpServletResponse.SC_BAD_REQUEST, null, "Cannot read the upload: " + e.getMessage());
            return;
        }
        if (file == null)
        {
            sendResult(resp, page, HttpServletResponse.SC_BAD_REQUEST, null, "No file in the upload");
            return;
        }

        try
        {
            fileStore.upload(entry, cleanName(file));
        }
        catch (IllegalStateException e)
        {
            sendResult(resp, page, HttpServletResponse.SC_CONFLICT, null, e.getMessage());
            return;
        }
        sendResult(resp, page, HttpServletResponse.SC_OK, entry, null);
    }

    /**
     * Some browsers send the whole path of the file as its name.
     */
    private static FileItem cleanName(FileItem file)
    {
        String name = file.getName();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash < 0) return file;
        DiskFileItemFactory factory = new DiskFileItemFactory();
        FileItem renamed = factory.createItem(file.getFieldName(), file.getContentType(), false,
                name.substring(slash + 1));
        try (java.io.InputStream in = file.getInputStream(); java.io.OutputStream out = renamed.getOutputStream())
        {
            byte[] buf = new byte[8192];
            for (int n = in.read(buf); n >= 0; n = in.read(buf)) out.write(buf, 0, n);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Cannot store the file", e);
        }
        file.delete();
        return renamed;
    }

    private void sendResult(HttpServletResponse resp, boolean page, int status, McpFileStore.Entry entry,
                            String error) throws IOException
    {
        if (page)
        {
            sendPage(resp, status, error != null ? "<p class=\"error\">" + escape(error) + ".</p>"
                    : "<p><b>" + escape(entry.getFile().getName()) + "</b> (" + megabytes(entry.getFile().getSize())
                    + ") is uploaded. You can go back to the conversation.</p>");
            return;
        }
        Map<String, Object> json = new LinkedHashMap<>();
        if (error != null)
        {
            json.put("error", error);
        }
        else
        {
            json.put("fileId", entry.fileId);
            json.put("name", entry.getFile().getName());
            json.put("size", entry.getFile().getSize());
        }
        addHeaders(resp);
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JSONB.toJson(json));
    }

    private static void sendPage(HttpServletResponse resp, int status, String body) throws IOException
    {
        addHeaders(resp);
        resp.setStatus(status);
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>Upload a file</title>"
                + "<style>body{font-family:system-ui,sans-serif;max-width:36em;margin:3em auto;padding:0 1em;"
                + "line-height:1.5}.purpose{font-size:1.1em}.note{color:#666;font-size:.9em}.error{color:#b00}"
                + "</style></head><body><h1>Upload a file</h1>" + body + "</body></html>");
    }

    /**
     * The token is in the address: it must not leak to other sites, caches or frames.
     */
    private static void addHeaders(HttpServletResponse resp)
    {
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Content-Security-Policy",
                "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'; frame-ancestors 'none'");
    }

    /**
     * The page posts with Accept: text/html; curl and other clients get JSON.
     */
    private static boolean wantsPage(HttpServletRequest req)
    {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    private static String token(HttpServletRequest req)
    {
        String uri = req.getRequestURI();
        if (uri == null) return null;
        String token = uri.substring(uri.lastIndexOf('/') + 1);
        return token.isEmpty() ? null : token;
    }

    private static String megabytes(long bytes)
    {
        return bytes < 1024 * 1024 ? Math.max(1, bytes / 1024) + " KB" : bytes / (1024 * 1024) + " MB";
    }

    private static String escape(String text)
    {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray())
        {
            switch (c)
            {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}

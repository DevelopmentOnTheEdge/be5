package com.developmentontheedge.be5.metadata.exception;

import com.developmentontheedge.be5.metadata.model.base.BeModelElement;
import com.developmentontheedge.be5.metadata.model.base.DataElementPath;
import com.developmentontheedge.beans.annot.PropertyName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@PropertyName("Error")
public class ProjectElementException extends RuntimeException implements Formattable
{
    private static final long serialVersionUID = 1L;
    private final DataElementPath path;
    private final String property;
    private final int row;
    private final int column;

    public ProjectElementException(DataElementPath path, String property, int row, int column, String message, Throwable cause)
    {
        super(message, cause);
        this.path = path;
        this.property = property;
        this.row = row;
        this.column = column;
    }

    public ProjectElementException(DataElementPath path, String property, int row, int column, Throwable cause)
    {
        this(path, property, row, column, null, cause);
    }

    public ProjectElementException(DataElementPath path, String property, Throwable cause)
    {
        this(path, property, 0, 0, null, cause);
    }

    public ProjectElementException(DataElementPath path, String property, String cause)
    {
        this(path, property, 0, 0, cause, null);
    }

    public ProjectElementException(DataElementPath path, Throwable cause)
    {
        this(path, null, cause);
    }

    public ProjectElementException(BeModelElement element, String property, Throwable cause)
    {
        this(element.getCompletePath(), property, 0, 0, null, cause);
    }

    public ProjectElementException(BeModelElement element, String property, String cause)
    {
        this(element.getCompletePath(), property, 0, 0, cause, null);
    }

    public ProjectElementException(BeModelElement element, Throwable cause)
    {
        this(element.getCompletePath(), null, cause);
    }

    public ProjectElementException(BeModelElement element, String cause)
    {
        this(element.getCompletePath(), null, cause);
    }

    @Override
    public String getMessage()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        if (property != null)
        {
            sb.append(": ").append(property);
        }
        if (row != 0)
        {
            sb.append('[').append(row).append(',').append(column).append(']');
        }
        sb.append(": ").append(getBaseMessage());
        return sb.toString();
    }

    @PropertyName("Location")
    public String getPath()
    {
        return path.toString();
    }

    @PropertyName("Property")
    public String getProperty()
    {
        return property;
    }

    @PropertyName("Row")
    public int getRow()
    {
        return row;
    }

    @PropertyName("Column")
    public int getColumn()
    {
        return column;
    }

    @PropertyName("Message")
    public String getBaseMessage()
    {
        String msg = super.getMessage() != null ? super.getMessage() + " " : "";
        if (getCause() != null)
        {
            return msg + String.valueOf(getCause().getMessage()).
                    replaceFirst("\\s+at .+\\[line \\d+, column \\d+\\]", "");
        }
        else
        {
            return super.getMessage();
        }
    }

    public static ProjectElementException notSpecified(BeModelElement de, String property)
    {
        return new ProjectElementException(de.getCompletePath(), property, new IllegalArgumentException("Not specified"));
    }

    public static ProjectElementException invalidValue(BeModelElement de, String property, Object value)
    {
        return new ProjectElementException(de.getCompletePath(), property, new IllegalArgumentException("Invalid " + property + ": "
                + value));
    }

    @Override
    public String format()
    {
        try
        {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8.name());
            format(out);

            return bytes.toString(StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AssertionError("", e);
        }
    }

    public void format(PrintStream out)
    {
        ProjectElementException error = this;
        String prefix = "";
        while (true)
        {
            String id = prefix + error.getPath();
            if (error.getProperty() != null)
            {
                id += ":" + error.getProperty();
            }
            if (error.getRow() > 0)
            {
                id += " [" + error.getRow() + "," + error.getColumn() + "]";
            }
            out.println(id);
            Throwable cause = error.getCause();
            if (prefix.isEmpty())
                prefix = " ";
            prefix = "-" + prefix;
            if (cause == null)
            {
                if (super.getMessage() != null)out.println(prefix + super.getMessage());
                break;
            }
            if (cause instanceof ProjectElementException)
            {
                error = (ProjectElementException) cause;
            }
            else
            {
                out.println(prefix + cause.getMessage());
                out.println();
                break;
            }
        }
    }

}

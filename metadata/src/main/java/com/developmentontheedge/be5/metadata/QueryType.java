package com.developmentontheedge.be5.metadata;


public enum QueryType
{
	/**
	 * 1D query returns results set where each row correspond to one record in corresponding database table.
	 */
	D1("1D"),

	/**
	 * 2D query returns results set where there is no correspondence one to one between returned rows and records in the database.
	 */
	D1_UNKNOWN("1D_unknown"),
	
	/**
	 * A query result is Cartesian product where:
	 * - first column is row title;
	 * - second column is column title
	 * - third - value for corresponding table cell.  
	 */
    D2("2D"),
    
    /**
     * Static query contains a hyperlink.
     */
    STATIC("static"),

    /**
     * Java query - a result is generated by corresponding java code.
     */
    JAVA("java"),

    /**
     * Javascript query - a result is generated by corresponding Javascript code. 
     */
    JAVASCRIPT("javascript"),
    
    /**
     * Groovy query - a result is generated by corresponding groovy code. 
     */
    GROOVY("groovy"),

    // be5 specific
    CONTAINER("container");

    private final String name;

    QueryType(String readableName)
    {
        this.name = readableName;
    }

    /**
     * 
     * @return a human readable name.
     */
    public String getName()
    {
        return name;
    }

    public static QueryType fromString(String name)
    {
        if("1D".equalsIgnoreCase(name))return QueryType.D1;
        if("1D_unknown".equalsIgnoreCase(name))return QueryType.D1_UNKNOWN;
        if("2D".equalsIgnoreCase(name))return QueryType.D2;

        return valueOf(name.toUpperCase());
    }

    @Override
    public String toString() {
        return name;
    }
}

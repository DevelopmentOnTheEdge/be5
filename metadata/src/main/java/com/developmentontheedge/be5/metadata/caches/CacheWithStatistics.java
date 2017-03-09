// $Id: CacheWithStatistics.java,v 1.1 2009/01/08 11:28:47 puz Exp $
package com.developmentontheedge.be5.metadata.caches;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author puz
 *
 */
public interface CacheWithStatistics
{
    List getStatistics() throws Exception;

    public static class CacheStatSection
    {
        private String sectionName;
        private Map stats;

        public CacheStatSection()
        {
            stats = new HashMap();
        }

        public String getSectionName()
        {
            return sectionName;
        }

        public void setSectionName(String sectionName)
        {
            this.sectionName = sectionName;
        }

        public Map getStats()
        {
            return stats;
        }

        public void addStats(String name, String value)
        {
            stats.put( name, value );
        }
    };

}

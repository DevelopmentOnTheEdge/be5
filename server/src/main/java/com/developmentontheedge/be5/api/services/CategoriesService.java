package com.developmentontheedge.be5.api.services;

import java.util.List;
import java.util.Objects;

public interface CategoriesService
{

    class Category {
        
        public final int id;
        public final String name;
        public final List<Category> children;
        
        public Category(int id, String name, List<Category> children) 
        {
            //checkArgument(id >= 0);
            Objects.requireNonNull(name);
            Objects.requireNonNull(children);
            
            this.id = id;
            this.name = name;
            this.children = children; //ImmutableList.copyOf(children);
        }
        
    }
    
    /**
     * Returns a list of root categories.
     */
    List<Category> getCategoriesForest(String entity, boolean hideEmpty);
    
}

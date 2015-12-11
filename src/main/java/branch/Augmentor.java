package branch;

import java.util.*;

/**
 * Augment an object by some means.
 * 
 * @author maclean
   *
 */
public interface Augmentor<T>   {
    
    public List<Augmentation<T>> augment(Augmentation<T> parent); 

}

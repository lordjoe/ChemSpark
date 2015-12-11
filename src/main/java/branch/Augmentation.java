package branch;


import java.io.*;

/**
 * An augmented object.
 * 
 * @author maclean
 * Slewis added Serializable
 *
 */
public interface Augmentation<T> extends Serializable {
    
    /**
     * Check to see if this augmentation is the canonical one.
     * 
     * @return true if it is a canonical augmentation
     */
    public boolean isCanonical();
    
    /**
     * @return the augmented object
     */
    public T getAugmentedMolecule();

}

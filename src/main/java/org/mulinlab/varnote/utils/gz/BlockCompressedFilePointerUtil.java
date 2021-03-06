package org.mulinlab.varnote.utils.gz;

import org.mulinlab.varnote.exceptions.InvalidArgumentException;

public final class BlockCompressedFilePointerUtil {
    private static final int SHIFT_AMOUNT = 16;
    private static final int OFFSET_MASK = 0xffff;
    private static final long ADDRESS_MASK = 0xFFFFFFFFFFFFL;

    public static final long MAX_BLOCK_ADDRESS = ADDRESS_MASK;
    public static final int MAX_OFFSET = OFFSET_MASK;
    
    /**
     * @param vfp1
     * @param vfp2
     * @return negative if vfp1 is earlier in file than vfp2, positive if it is later, 0 if equal.
     */
    public static int compare(final long vfp1, final long vfp2) {
        if (vfp1 == vfp2) return 0;
        // When treating as unsigned, negative number is > positive.
        if (vfp1 < 0 && vfp2 >= 0) return 1;
        if (vfp1 >= 0 && vfp2 < 0) return -1;
        // Either both negative or both non-negative, so regular comparison works.
        if (vfp1 < vfp2) return -1;
        return 1; // vfp1 > vfp2
    }

    /**
     * @return true if vfp2 points to somewhere in the same BGZF block, or the one immediately following vfp1's BGZF block.
     */
    public static boolean areInSameOrAdjacentBlocks(final long vfp1, final long vfp2) {
        final long block1 = getBlockAddress(vfp1);
        final long block2 = getBlockAddress(vfp2);
        return (block1 == block2 || block1 + 1 == block2);        
    }

    /**
     * @param blockAddress File offset of start of BGZF block.
     * @param blockOffset Offset into uncompressed block.
     * @return Virtual file pointer that embodies the input parameters.
     */
    public static long makeFilePointer(final long blockAddress, final int blockOffset) {
        if (blockOffset < 0) {
            throw new InvalidArgumentException("Negative blockOffset " + blockOffset + " not allowed.");
        }
        if (blockAddress < 0) {
            throw new InvalidArgumentException("Negative blockAddress " + blockAddress + " not allowed.");
        }
        if (blockOffset > MAX_OFFSET) {
            throw new InvalidArgumentException("blockOffset " + blockOffset + " too large.");
        }
        if (blockAddress > MAX_BLOCK_ADDRESS) {
            throw new InvalidArgumentException("blockAddress " + blockAddress + " too large.");
        }
        return blockAddress << SHIFT_AMOUNT | blockOffset;
    }

    /**
     * @param virtualFilePointer
     * @return File offset of start of BGZF block for this virtual file pointer.
     */
    public static long getBlockAddress(final long virtualFilePointer) {
        return (virtualFilePointer >> SHIFT_AMOUNT) & ADDRESS_MASK;
    }

    /**
     * @param virtualFilePointer
     * @return Offset into uncompressed block for this virtual file pointer.
     */
    public static int getBlockOffset(final long virtualFilePointer) {
        return (int) (virtualFilePointer & OFFSET_MASK);
    }

    public static String asString(final long vfp) {
        return String.format("%d(0x%x): (block address: %d, offset: %d)", vfp, vfp, getBlockAddress(vfp), getBlockOffset(vfp));
    }
}

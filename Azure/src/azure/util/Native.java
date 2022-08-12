package azure.util;

public final class Native
{
    //The peek methods use the follow base address when accessing VM memory.
    public static final int ABSOLUTE = 0;
    public static final int THREADS = 1;
    public static final int HEAP = 2;
    public static final int IMAGE = 3;
    public static final int STATICS = 4;
    public static final int MEM = 5;

    // Offsets and masks to allow access to a standard Object header
    public static final int OBJ_HDR_SZ = 4;
    public static final int OBJ_FLAGS = 1;
    public static final int OBJ_LEN_MASK = 0x3f;
    public static final int OBJ_LEN_OBJECT = 0x3f;
    public static final int OBJ_LEN_BIGARRAY = 0x3e;
    public static final int OBJ_CLASS = 0;
    public static final int OBJ_BIGARRAY_LEN = 4;

    // Basic variable types used within the VM
    public static final int VM_OBJECT = 0;
    public static final int VM_CLASS = 2;
    public static final int VM_BOOLEAN = 4;
    public static final int VM_CHAR = 5;
    public static final int VM_FLOAT = 6;
    public static final int VM_DOUBLE = 7;
    public static final int VM_BYTE = 8;
    public static final int VM_SHORT = 9;
    public static final int VM_INT = 10;
    public static final int VM_LONG = 11;
    public static final int VM_VOID = 12;
    public static final int VM_OBJECTARRAY = 13;
    public static final int VM_STRING = 37;
    public static final int VM_CHARARRAY = 18;

    // The base address of the in memory program header
    public static final int IMAGE_BASE = memPeekInt(MEM, IMAGE*4);
    // Provide access to the image header structure
    public static final int IMAGE_HDR_LEN = 20;
    public static final int LAST_CLASS_OFFSET = 17;

    public static int pointerTo(Object obj) {
    	return getDataAddress(obj);
    }

    public static int read32(int addr) {
    	//return *((int32_t*)addr)
    	return memPeekInt(ABSOLUTE, addr);
    }

    public static void write32(int addr, int value) {
    	Object dest = memGetReference(ABSOLUTE, addr);
    	Integer valueInt = new Integer(value);
		memCopy(dest, -OBJ_HDR_SZ, ABSOLUTE, getDataAddress(valueInt), 4);
    }

    // Low level memory access functions

    /**
     * Return up to 4 bytes from a specified memory location.
     * @param base Base section of memory.
     * @param offset Offset (in bytes) of the location
     * @param typ The primitive data type to access
     * @return Memory location contents.
     */
    public static native int memPeek(int base, int offset, int typ);

    /**
     * Copy the specified number of bytes from memory into the given object.
     * @param obj Object to copy to
     * @param objoffset Offset (in bytes) within the object
     * @param base Base section to copy from
     * @param offset Offset within the section
     * @param len Number of bytes to copy
     */
    public static native void memCopy(Object obj, int objoffset, int base, int offset, int len);

    /**
     * Return the address of the given objects first data field.
     * @param obj
     * @return the required address
     */
    public native static int getDataAddress(Object obj);

    /**
     * Return the address of the given object.
     * @param obj
     * @return the required address
     */
    public native static int getObjectAddress(Object obj);

    /**
     * Return a Java object reference the points to the location provided.
     * @param base Memory section that offset refers to.
     * @param offset The offset from the base in bytes.
     * @return
     */
    public native static Object memGetReference(int base, int offset);

    /**
     * Return a single byte from the specified memory location.
     * @param base
     * @param offset
     * @return byte value from memory
     */
    public static int memPeekByte(int base, int offset)
    {
        return memPeek(base, offset, VM_BYTE) & 0xff;
    }

    /**
     * Return a 16 bit word from the specified memory location.
     * @param base
     * @param offset
     * @return short value from memory
     */
    public static int memPeekShort(int base, int offset)
    {
        return memPeek(base, offset, VM_SHORT) & 0xffff;
    }

    /**
     * Return a 32 bit word from the specified memory location.
     * @param base
     * @param offset
     * @return int value from memory
     */
    public static int memPeekInt(int base, int offset)
    {
        return memPeek(base, offset, VM_INT);
    }

    /**
     * Return true if the specified object is an array
     * @param obj object to test
     * @return true iff the specified object is an array
     */
    public final boolean isArray(Object obj)
    {
        if (obj == null) return false;
        return (memPeekByte(ABSOLUTE, getObjectAddress(obj)+OBJ_FLAGS) & OBJ_LEN_MASK) != OBJ_LEN_OBJECT;
    }
 }

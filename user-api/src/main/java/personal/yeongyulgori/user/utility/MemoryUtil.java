package personal.yeongyulgori.user.utility;

public class MemoryUtil {

    public static long usedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

}

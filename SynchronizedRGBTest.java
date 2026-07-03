import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizedRGBTest {
    private static final int BLUE_RGB = 0x0000FF;
    private static final int YELLOW_RGB = 0xFFFF00;

    public static void main(String[] args) throws InterruptedException {
        SynchronizedRGB color = new SynchronizedRGB(0, 0, 255, "Blue");
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger unlockedMismatches = new AtomicInteger();
        AtomicInteger lockedMismatches = new AtomicInteger();

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                color.set(0, 0, 255, "Blue");
                color.set(255, 255, 0, "Yellow");
            }
            running.set(false);
        }, "rgb-writer");

        Thread unlockedReader = new Thread(() -> {
            while (running.get()) {
                int rgb = color.getRGB();
                String name = color.getName();
                if (!matches(rgb, name)) {
                    unlockedMismatches.incrementAndGet();
                }
            }
        }, "unlocked-reader");

        Thread lockedReader = new Thread(() -> {
            while (running.get()) {
                synchronized (color) {
                    int rgb = color.getRGB();
                    String name = color.getName();
                    if (!matches(rgb, name)) {
                        lockedMismatches.incrementAndGet();
                    }
                }
            }
        }, "locked-reader");

        writer.start();
        unlockedReader.start();
        lockedReader.start();

        writer.join();
        unlockedReader.join();
        lockedReader.join();

        System.out.println("SynchronizedRGB unlocked composite reads with mismatches: " + unlockedMismatches.get());
        System.out.println("SynchronizedRGB locked composite reads with mismatches: " + lockedMismatches.get());
    }

    private static boolean matches(int rgb, String name) {
        return (rgb == BLUE_RGB && "Blue".equals(name))
                || (rgb == YELLOW_RGB && "Yellow".equals(name));
    }
}

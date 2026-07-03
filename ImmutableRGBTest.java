import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ImmutableRGBTest {
    private static final int BLUE_RGB = 0x0000FF;
    private static final int YELLOW_RGB = 0xFFFF00;

    public static void main(String[] args) throws InterruptedException {
        AtomicReference<ImmutableRGB> current =
                new AtomicReference<>(new ImmutableRGB(0, 0, 255, "Blue"));
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger mismatches = new AtomicInteger();

        Thread writer = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                current.set(new ImmutableRGB(0, 0, 255, "Blue"));
                current.set(new ImmutableRGB(255, 255, 0, "Yellow"));
            }
            running.set(false);
        }, "immutable-writer");

        Thread reader1 = new Thread(() -> readSnapshots(current, running, mismatches), "immutable-reader-1");
        Thread reader2 = new Thread(() -> readSnapshots(current, running, mismatches), "immutable-reader-2");

        writer.start();
        reader1.start();
        reader2.start();

        writer.join();
        reader1.join();
        reader2.join();

        System.out.println("ImmutableRGB snapshot reads with mismatches: " + mismatches.get());
    }

    private static void readSnapshots(AtomicReference<ImmutableRGB> current,
                                      AtomicBoolean running,
                                      AtomicInteger mismatches) {
        while (running.get()) {
            ImmutableRGB snapshot = current.get();
            int rgb = snapshot.getRGB();
            String name = snapshot.getName();
            if (!matches(rgb, name)) {
                mismatches.incrementAndGet();
            }
        }
    }

    private static boolean matches(int rgb, String name) {
        return (rgb == BLUE_RGB && "Blue".equals(name))
                || (rgb == YELLOW_RGB && "Yellow".equals(name));
    }
}

package work.lclpnet.test;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.util.SystemProperties;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.util.Locale;

public class FabricLoaderLauncherSessionListener implements LauncherSessionListener {

    static {
        System.setProperty(SystemProperties.DEVELOPMENT, "true");
        System.setProperty(SystemProperties.UNIT_TEST, "true");
    }

    private final Knot knot;
    private final ClassLoader classLoader;

    private ClassLoader launcherSessionClassLoader;

    public FabricLoaderLauncherSessionListener() {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        final EnvType envType = EnvType.valueOf(System.getProperty(SystemProperties.SIDE, EnvType.CLIENT.name()).toUpperCase(Locale.ROOT));

        try {
            knot = new Knot(envType);
            classLoader = knot.init(new String[]{});
        } finally {
            // Knot.init sets the context class loader, revert it back for now.
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        final Thread currentThread = Thread.currentThread();
        launcherSessionClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
    }

    @Override
    public void launcherSessionClosed(LauncherSession session) {
        final Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(launcherSessionClassLoader);
    }
}

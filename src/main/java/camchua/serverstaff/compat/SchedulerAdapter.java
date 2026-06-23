package camchua.serverstaff.compat;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SchedulerAdapter {

    private static final boolean FOLIA = detectFolia();

    private SchedulerAdapter() {}

    private static boolean detectFolia() {
        try {
            Bukkit.class.getMethod("getGlobalRegionScheduler");
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    public static boolean isFolia() {
        return FOLIA;
    }
    // fix temp 2
    public static void runTaskLater(JavaPlugin plugin, Runnable runnable, long delayTicks) {
        if (FOLIA) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runDelayed = globalScheduler.getClass().getMethod(
                        "runDelayed",
                        Plugin.class,
                        Consumer.class,
                        long.class
                );

                runDelayed.invoke(globalScheduler, plugin, (Consumer<Object>) task -> runnable.run(), delayTicks);
                return;
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to run delayed task on Folia global scheduler", throwable);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }
    // fix temp 3
    public static void runTaskTimer(JavaPlugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (FOLIA) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runAtFixedRate = globalScheduler.getClass().getMethod(
                        "runAtFixedRate",
                        Plugin.class,
                        Consumer.class,
                        long.class,
                        long.class
                );

                runAtFixedRate.invoke(globalScheduler, plugin, (Consumer<Object>) task -> runnable.run(), delayTicks, periodTicks);
                return;
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to run timer task on Folia global scheduler", throwable);
            }
        }

        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
    }

    public static void runEntityTask(JavaPlugin plugin, Entity entity, Runnable runnable) {
        if (FOLIA) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Method run = scheduler.getClass().getMethod(
                        "run",
                        Plugin.class,
                        Consumer.class,
                        Runnable.class
                );

                run.invoke(scheduler, plugin, (Consumer<Object>) task -> runnable.run(), null);
                return;
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to run entity task on Folia scheduler", throwable);
            }
        }

        Bukkit.getScheduler().runTask(plugin, runnable);
    }
  // SHITTT
    public static void runEntityTaskLater(JavaPlugin plugin, Entity entity, Runnable runnable, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Method runDelayed = scheduler.getClass().getMethod(
                        "runDelayed",
                        Plugin.class,
                        Consumer.class,
                        Runnable.class,
                        long.class
                );

                runDelayed.invoke(scheduler, plugin, (Consumer<Object>) task -> runnable.run(), null, delayTicks);
                return;
            } catch (Throwable throwable) {
                throw new IllegalStateException("Failed to run delayed entity task on Folia scheduler", throwable);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }
}
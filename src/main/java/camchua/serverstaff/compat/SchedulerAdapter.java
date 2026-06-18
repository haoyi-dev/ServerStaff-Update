package camchua.serverstaff.compat;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SchedulerAdapter {

    private SchedulerAdapter() {}

    public static boolean isFolia() {
        try {
            Bukkit.class.getMethod("getGlobalRegionScheduler");
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    public static void runTaskLater(JavaPlugin plugin, Runnable runnable, long delayTicks) {
        if (isFolia()) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
                Method runDelayed = globalScheduler.getClass().getMethod(
                        "runDelayed",
                        Plugin.class,
                        java.util.function.Consumer.class,
                        long.class
                );
                runDelayed.invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) t -> runnable.run(), delayTicks);
                return;
            } catch (Throwable ignored) {
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    public static void runTaskTimer(JavaPlugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia()) {
            try {
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
                Method runAtFixedRate = globalScheduler.getClass().getMethod(
                        "runAtFixedRate",
                        Plugin.class,
                        java.util.function.Consumer.class,
                        long.class,
                        long.class
                );
                runAtFixedRate.invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) t -> runnable.run(), delayTicks, periodTicks);
                return;
            } catch (Throwable ignored) {
            }
        }

        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
    }

    public static void runEntityTask(JavaPlugin plugin, Entity entity, Runnable runnable) {
        if (isFolia()) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Method run = scheduler.getClass().getMethod(
                        "run",
                        Plugin.class,
                        java.util.function.Consumer.class,
                        Runnable.class
                );
                run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) t -> runnable.run(), null);
                return;
            } catch (Throwable ignored) {
            }
        }

        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}

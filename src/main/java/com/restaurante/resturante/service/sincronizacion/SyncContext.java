package com.restaurante.resturante.service.sincronizacion;

public class SyncContext {
    private static final ThreadLocal<Boolean> syncing = ThreadLocal.withInitial(() -> false);

    public static boolean isSyncing() {
        return syncing.get();
    }

    public static void setSyncing(boolean val) {
        syncing.set(val);
    }

    public static void clear() {
        syncing.remove();
    }
}

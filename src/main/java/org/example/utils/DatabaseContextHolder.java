package org.example.utils;

public class DatabaseContextHolder {
    private static final ThreadLocal<DatabaseType> CONTEXT = new ThreadLocal<>();

    public static void setDatabaseType(DatabaseType type) {
        CONTEXT.set(type);
    }

    public static DatabaseType getDatabaseType() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

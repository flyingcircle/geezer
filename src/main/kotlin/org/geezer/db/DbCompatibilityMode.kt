package org.geezer.db

enum class DbCompatibilityMode(val h2Mode: String) {
    DB2("DB2"),
    DERBY("Derby"),
    HSQLDB("HSQLDB"),
    MS_SQL("MSSQLServer"),
    MY_SQL("MySQL;DATABASE_TO_LOWER=TRUE"),
    ORACLE("Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;"),
    POSTGRE_SQL("PostgreSQL;DATABASE_TO_LOWER=TRUE"),
    IGNITE("Ignite");
}

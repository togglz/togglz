package de.chkal.togglz.core.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtils {

    public static void closeQuitly(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public static void closeQuitly(Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public static void closeQuitly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

}

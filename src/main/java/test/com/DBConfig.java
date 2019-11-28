package test.com;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConfig {
	private static Connection conn;

	private static String driverClass;
	private static String url;
	private static String user;
	private static String password;

	static {
		try {
			readConfig();
			Class.forName(driverClass);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readConfig() throws IOException {
		InputStream is = DBConfig.class.getClassLoader().getResourceAsStream("resources/jdbc.properties");
		Properties pro = new Properties();
		pro.load(is);

		driverClass = pro.getProperty("driverClass");
		url = pro.getProperty("url");
		user = pro.getProperty("user");
		password = pro.getProperty("password");
	}

	public static Connection getConnection() {
		try {
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static void close(Connection conn, Statement stat) {
		if (stat != null) {
			try {
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void close(Connection conn, Statement stat, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		if (stat != null) {
			try {
				stat.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

	}

}

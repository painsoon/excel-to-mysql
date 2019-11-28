package test.com;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DBManager {
	
	private static DBManager dbManage = new DBManager();
	
	public static DBManager getInstance() {
		return dbManage;
	}

	// 创建数据表
	public void createTable(String tableName, FieldBean primary, List<FieldBean> field) {
		Statement st = null;
		Connection conn = null;

		StringBuffer sql = new StringBuffer("create table ");
		sql.append(tableName + " ( ");
		sql.append(primary.getFieldName() + " " + primary.getType() + " not null primary key ");
		for (int i = 1; i < field.size(); i++) {
			if (!StringUtil.isEmpty(field.get(i).getFieldName()) && !StringUtil.isEmpty(field.get(i).getType())) {
				sql.append(" , " + field.get(i).getFieldName() + " " + field.get(i).getType() + " default null ");
			}
		}
		sql.append(" ) ");
		System.out.println(sql.toString());
		try {
			conn = DBConfig.getConnection();
			st = conn.createStatement();
			//若存在删除老表
			st.execute("drop table if exists " + tableName);
			st.execute(sql.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConfig.close(conn, st);
		}

	}

	// 插入数据
	public void insertdate(String tableName, List<List<String>> list) {
		PreparedStatement st = null;
		Connection conn = null;
		int cols = list.get(0).size();
		StringBuffer sql = new StringBuffer("insert into " + tableName + " values( ? ");
		for (int i = 1; i < cols; i++) {
			sql.append(",? ");
		}
		sql.append(" ) ");
//		System.out.println(sql.toString());
		conn = DBConfig.getConnection();
		try {
			st = conn.prepareStatement(sql.toString());
			//关闭自动提交，批量插入提高效率
			conn.setAutoCommit(false);
			for (int i = 0; i < list.size(); i++) {
				for (int j = 0; j < cols; j++) {
					st.setObject(j + 1, list.get(i).get(j));
				}
				//行标记
				st.addBatch();
			}
			
			st.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConfig.close(conn, st);
		}
	}

}

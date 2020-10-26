package db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
//create a table
public class mySQLTableCreation {
	// Run this as Java application to reset the database.
		public static void main(String[] args) {
			try {
				// Step 1 Connect to MySQL.
				System.out.println("Connecting to " + MySQLDBUtil.URL);
				//java的api 如何调用
				Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
				//建立连接
				Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);
				
				if (conn == null) {
					return;
				}
				
				
				//step2 drop existing table;
				Statement statement = conn.createStatement();
				String sql = "DROP TABLE IF EXISTS keywords";
				statement.executeUpdate(sql);//写操作
//				statement.executeQuery(sql);// 读操作；
				
				//drop是有顺序的， key and key 是relative的；
				//先删除指向别人的，再删除被指向的；
				//先删除kwy
				sql = "DROP TABLE IF EXISTS history";
				statement.executeUpdate(sql);
				
				sql = "DROP TABLE IF EXISTS items";
				statement.executeUpdate(sql);

				sql = "DROP TABLE IF EXISTS users";
				statement.executeUpdate(sql);
				
				//step 3 create new tables
				//1st table
				sql = "CREATE TABLE items ("
						+ "item_id VARCHAR(255) NOT NULL,"
						+ "name VARCHAR(255),"
						+ "address VARCHAR(255),"    //VACHAR 字符，最多255个
						+ "image_url VARCHAR(255),"
						+ "url VARCHAR(255),"
						+ "PRIMARY KEY (item_id)"
						+ ")";
				statement.executeUpdate(sql); //这一步很重要，有了才算创建成功。
				
				//2nd table
				sql = "CREATE TABLE users ("
						+ "user_id VARCHAR(255) NOT NULL,"
						+ "password VARCHAR(255) NOT NULL,"
						+ "first_name VARCHAR(255),"
						+ "last_name VARCHAR(255),"
						+ "PRIMARY KEY (user_id)"
						+ ")";
				statement.executeUpdate(sql);
				
				//3rd table
				sql = "CREATE TABLE keywords ("
						+ "item_id VARCHAR(255) NOT NULL,"
						+ "keyword VARCHAR(255) NOT NULL,"
						+ "PRIMARY KEY (item_id, keyword),"
						+ "FOREIGN KEY (item_id) REFERENCES items(item_id)" //item id 是可以改；
						+ ")";
				statement.executeUpdate(sql);
				
				//4th
				sql = "CREATE TABLE history ("
						+ "user_id VARCHAR(255) NOT NULL,"
						+ "item_id VARCHAR(255) NOT NULL,"
						+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," //TIMESTAMP mySql里指代时间
						//DEFAULT 会提供默认值。
						+ "PRIMARY KEY (user_id, item_id),"
						+ "FOREIGN KEY (user_id) REFERENCES users(user_id),"
						+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"
						+ ")";
				statement.executeUpdate(sql);
				
				//test 插入 
				// Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050
				//"3229c1097c00d497a0fd282d586be050" 是加密后的密码，通过密钥才能解开。
				sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
				statement.executeUpdate(sql);
				
				
				//如果不close不会有大的影响，但是会影响效率。写了是比较好的习惯
				conn.close();
				System.out.println("Import done successfully");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}

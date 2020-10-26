package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;

public class MySQLConnection {
	private Connection conn;


	//创建连接
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//关闭连接
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//set ,item 有的时候需要插入，有的时候不需要插入
	public void setFavoriteItems(String userId, Item item) {
		//跟数据库连接
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
		saveItem(item);
		//maybe insert item to item table;给第一次收藏的人。
		//IGNORE: insert if not exist, 如果出现了duplicate,就跳过，然后返回
		String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)"; // 可以这么写：VALUES("1111",'abcd')
		//但是为了随机性：VALUES (%s, %s); 
		//‘VALUES (?, ?)’ 可以用setString
		//安全隐患
//		sql = "DELETE FROM users WHERE user_id = (Vincent OR 1 = 1)";
		// ？占位，这样较为方便；
//		sql = "DELETE FROM users WHERE user_id = ？";
		try {
			//PreparedStatement 必须跟着setString 一起使用；
			PreparedStatement statement = conn.prepareStatement(sql);
			//setString(1, userId); 1 就是 ？， index 从1开始写
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//unset, 只需要id 就能删除
	public void unsetFavoriteItems(String userId, String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//往item 插入数据；
	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		//IGNORE,skip duplicate keyword
		//区分是不是最后一个人取消的
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
			
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
                    statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//user id from front-end
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		
		//最终要返回的
		Set<String> favoriteItems = new HashSet<>();
		//SELECT 查询
		//SELECT item_id * 如果想读所有列
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery(); // 读操作；update不需要有返回值，query必须要有返回值表明结果
			//ResultSet json, 一维的数据结构。看有多少行符合它的要求。
			while (rs.next()) {  //针对result suit 做循环，遍历所有获取的数据, rs.next 最终return false;
				String itemId = rs.getString("item_id");
			//	String UserId = rs.getString("user_id"); 如果想读user id;
//				int UserId = rs.getInt("user_id"); 
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}
	
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		//获取user 收藏的id
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);

		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			//遍历
			for (String itemId : favoriteItemIds) {
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {   // 读取 item table; 也可以写while,
					favoriteItems.add(Item.builder()
							.itemId(rs.getString("item_id"))
							.name(rs.getString("name"))
							.address(rs.getString("address"))
							.imageUrl(rs.getString("image_url"))
							.url(rs.getString("url"))
							.keywords(getKeywords(itemId))
							.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId); // 把传进来的user id 传进来；
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {  // 有个指针指向数据库。应该是unique的；
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId); 
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return name;
	}
	
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql); 
			statement.setString(1, userId);
			statement.setString(2, password); // hash 
			ResultSet rs = statement.executeQuery();  //读
			if (rs.next()) {
				return true;
			}
			//return re.next();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}

		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql); 
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);

			return statement.executeUpdate() == 1;  // 成功就是1；如果ignore 是返回0，因为返回了就不存在了；
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}

package db;


//配置文件
//如果要check in 到github ,不要传这个文件

public class MySQLDBUtil {
	private static final String INSTANCE = "zoe-database.cjoua2nu71ea.us-east-2.rds.amazonaws.com";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "jupiter";
	private static final String USERNAME = "zoe_admin";
	private static final String PASSWORD = "521mljB2Y";
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}

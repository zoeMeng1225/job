package rpc;
//like or un like, 
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;

/**
 * Server let implementation class ItemHistory
 */
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				HttpSession session = request.getSession(false);
				if (session == null) {
					response.setStatus(403);
					return;
				}
				String userId = request.getParameter("user_id");
				
				MySQLConnection connection = new MySQLConnection();
				Set<Item> items = connection.getFavoriteItems(userId);
				connection.close();
				
				JSONArray array = new JSONArray();
				for (Item item : items) {
					JSONObject obj = item.toJSONObject();
					obj.put("favorite", true); // 前端需要显示红心会不会是红心的，true就显示实心的；
					//在myFavirite 窗口里，里面都得是实心的。
					//在search里不能都是实心的，如何判断？看history table。
					array.put(obj);
				}
				RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	//收藏
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		MySQLConnection connection = new MySQLConnection();
		//get the data from front-end;
		//JASONObject 自动解析string, parse JSON, string class 在java不好用。
		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
		String userId = input.getString("user_id");
		//这个data是JSON, 
	
		Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));		
		connection.setFavoriteItems(userId, item);
		connection.close();
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	//取消收藏
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		MySQLConnection connection = new MySQLConnection();
		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
		String userId = input.getString("user_id");
		Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));

		connection.unsetFavoriteItems(userId, item.getItemId()); //与doPost的区别
		connection.close();
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));


	}

}

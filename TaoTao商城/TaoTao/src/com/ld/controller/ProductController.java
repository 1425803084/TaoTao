package com.ld.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.ld.bean.Cart;
import com.ld.bean.CartItem;
import com.ld.bean.Order;
import com.ld.bean.OrderItem;
import com.ld.bean.PageBean;
import com.ld.bean.Product;
import com.ld.bean.User;
import com.ld.service.ProductService;
import com.ld.utils.AlipayConfig;
import com.ld.utils.CommonsUtils;

@Controller
public class ProductController {
	@Autowired
	ProductService productService;
	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("hostProduct", productService.findHotProduct(1, 9));
		model.addAttribute("newProduct", productService.findNewProduct(1, 9));
		return "index";
	}
	@RequestMapping("/product")//������Ʒ�б�
	public String product(HttpServletRequest request) {
		String cid = request.getParameter("cid");
		String currentPageStr = request.getParameter("currentPage");
		if(currentPageStr==null) currentPageStr = "1";
		int currentPage = Integer.parseInt(currentPageStr);
		int currentCount = 12;//��ǰҳ�����
		
		PageBean<Product> pageBean = productService.findProductByCid(cid,currentPage,currentCount);
		request.setAttribute("pageBean", pageBean);
		request.setAttribute("cid", cid);
		
		//����һ����¼��ʷ��Ʒ��Ϣ�ļ���
		List<Product> historyProductList = new ArrayList<Product>();
		
		//��ÿͻ���Я�����ֽ�pids��cookie
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			for(Cookie cookie:cookies) {
				if("pids".equals(cookie.getName())) {
					String pids = cookie.getValue();
					String[] split = pids.split("-");
					for(String pid:split){
						Product pro = productService.findProductByPid(pid);
						historyProductList.add(pro);
					}
				}
			}
		}
		//����ʷ��¼�ļ��Ϸŵ�����
		request.setAttribute("historyProductList", historyProductList);
		
		return "product_list";
	}
	@RequestMapping("/productInfo")//������Ʒ
	public String productInfo(HttpServletRequest request,Model model,HttpServletResponse response) {
		String pid = request.getParameter("pid");
		String cid = request.getParameter("cid");
		String currentPage = request.getParameter("currentPage");
		model.addAttribute("product", productService.findProductByPid(pid));
		model.addAttribute("cid", cid);
		model.addAttribute("currentPage",currentPage);
		
		//��ÿͻ���Я��cookie---���������pids��cookie
		String pids = pid;
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
			for(Cookie cookie : cookies){
				if("pids".equals(cookie.getName())) {
					pids = cookie.getValue();
					//��pids���һ������
					String[] split = pids.split("-");
					List<String> aslist = Arrays.asList(split);
					LinkedList<String> list = new LinkedList<String>(aslist);
					//�жϼ������Ƿ���ڵ�ǰpid
					if(list.contains(pid)){
						//������ǰ��pid
						list.remove(pid);
					}
					list.addFirst(pid);
					//����ת���ַ���
					StringBuffer sb = new StringBuffer();
					for(int i=0;i<list.size()&&i<7;i++){
						sb.append(list.get(i));
						sb.append("-");
					}
					//ȥ������"-"
					pids = sb.substring(0, sb.length()-1);
				}
			}
		}
		Cookie cookie1 = new Cookie("pids", pids);
		response.addCookie(cookie1);
		return "product_info";
	}
	//�����Ʒ�����ﳵ
	@RequestMapping("/addProductToCart")
	public String addProductToCart(HttpServletRequest request) {
		HttpSession session = request.getSession();
		
		//���Ҫ�ŵ����ﳵ����Ʒ��pid
		String pid = request.getParameter("pid");
		//��ø���Ʒ�Ĺ�������
		int buyNum = Integer.parseInt(request.getParameter("buyNum"));
		//���product����
		Product product = productService.findProductByPid(pid);
		//����С��
		double subtotal = product.getShop_price()*buyNum;
		//��װcartItem
		CartItem item = new CartItem();
		item.setProduct(product);
		item.setBuyNum(buyNum);
		item.setSubtotal(subtotal);
		//��ù��ﳵ---�ж��Ƿ���session�Ѿ����ڹ��ﳵ
		Cart cart = (Cart) session.getAttribute("cart");
		double newsubtotal = 0.0;
		
		if(cart==null) {
			cart = new Cart();
		}
		//����������복��
		//���жϹ��ﳵ���Ƿ��Ѱ���������---�ж�key�Ƿ����
		//������ﳵ�Ѵ��ڸ���Ʒ----���������������ԭ��������������Ӳ���
		Map<String,CartItem> cartItems = cart.getCartItems();
		if(cartItems.containsKey(pid)) {
			//ȡ����Ʒ������
			CartItem cartItem = cartItems.get(pid);
			int oldBuyNum = cartItem.getBuyNum();
			oldBuyNum += buyNum;
			cartItem.setBuyNum(oldBuyNum);
			cart.setCartItems(cartItems);
			//�޸�С��
			 double oldsubtotal = cartItem.getSubtotal();
			 newsubtotal = buyNum*product.getShop_price();
			 cartItem.setSubtotal(oldsubtotal+newsubtotal);
			
		}else {
			//�������û��
			cart.getCartItems().put(product.getPid(), item);
			newsubtotal = buyNum*product.getShop_price();
		}
		
		//�����ܼ�
		 double total = cart.getTotal()+newsubtotal;
		 cart.setTotal(total);
		
		//�����ٴη���session
		session.setAttribute("cart", cart);
		//ֱ����ת�����ﳵҳ��
		
		return "redirect:/cart";
	}
	//ɾ����һ��Ʒ
	@RequestMapping("/delProFromCart")
	public String delProFromCart(HttpServletRequest request) {
		String pid = request.getParameter("pid");
		//ɾ��session���ﳵ�еĹ�������е�item
		HttpSession session = request.getSession();
		Cart cart = (Cart) session.getAttribute("cart");
		if(cart!=null) {
			Map<String, CartItem> cartItems = cart.getCartItems();
			//��Ҫ�޸��ܼ�
			cart.setTotal(cart.getTotal()-cartItems.get(pid).getSubtotal());
			//ɾ��
			cartItems.remove(pid);
		}
		session.setAttribute("cart", cart);
		return "redirect:/cart";
	}
	//��չ��ﳵ
	@RequestMapping("/clearCart")
	public String clearCart(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute("cart");
		return "redirect:/cart";
	}
	//�ύ����
	@RequestMapping("/submitOrder")
	public String submitOrder(HttpServletRequest request) {
		HttpSession session = request.getSession();
		//�ж��û��Ƿ��Ѿ���¼
		User user = (User) session.getAttribute("user");
		if(user == null) {
			return "redirect:/login";
		}
		//��װOrder
		Order order = new Order();
		String oid = CommonsUtils.getUUID();
		order.setOid(oid);
		order.setOrdertime(new Date());
		//���session�еĹ��ﳵ
		Cart cart = (Cart) session.getAttribute("cart");
		double total = cart.getTotal();
		order.setTotal(total);
		order.setState(0);
		order.setAddress(null);
		order.setName(null);
		order.setTelephone(null);
		order.setUser(user);
		//��ù��ﳵ�еĹ�����ļ���map
		Map<String, CartItem> cartItems = cart.getCartItems();
		for(Map.Entry<String, CartItem> entry:cartItems.entrySet()) {
			//ȡ��ÿһ��������
			CartItem cartItem = entry.getValue();
			//�����µĶ�����
			OrderItem orderItem = new OrderItem();
			orderItem.setItemid(CommonsUtils.getUUID());
			orderItem.setCount(cartItem.getBuyNum());
			orderItem.setSubtotal(cartItem.getSubtotal());
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setOrder(order);
			
			//���ö�������ӵ������Ķ��������
			order.getOrderItems().add(orderItem);
		}
		productService.submitOrder(order);
		
		session.setAttribute("order", order);
		return "redirect:/order_info";
	}
	//ȷ�϶���
	@RequestMapping("confirmOrder")
	public void confirmOrder(Order order,HttpServletRequest request,HttpServletResponse response) throws IOException, AlipayApiException {
		//�����ջ�����Ϣ
		productService.updateOrder(order);
		//
		// ��� ֧�������������
				String orderid = request.getParameter("oid");
				//String money = order.getTotal()+"";
				String money = "0.01";
				// ����
				String pd_FrpId = request.getParameter("pd_FrpId");
				//��ó�ʼ����AlipayClient
				AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl, AlipayConfig.app_id, AlipayConfig.merchant_private_key, "json", AlipayConfig.charset, AlipayConfig.alipay_public_key, AlipayConfig.sign_type);
				//�����������
		        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
		        alipayRequest.setReturnUrl(AlipayConfig.return_url);
		        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);
		        
		      //�̻������ţ��̻���վ����ϵͳ��Ψһ�����ţ�����
		        String out_trade_no = orderid;
		        //���������
		        String total_amount = money;
		        //�������ƣ�����
		        String subject = "����";
		        //��Ʒ�������ɿ�
		        String body = "";
		        
		        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\"," 
		                + "\"total_amount\":\""+ total_amount +"\"," 
		                + "\"subject\":\""+ subject +"\"," 
		                + "\"body\":\""+ body +"\"," 
		                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
		        
		        //����
		        String result = alipayClient.pageExecute(alipayRequest).getBody();
		        
		        response.setContentType("text/html;charset="+AlipayConfig.charset);
		        response.getWriter().write(result);
		        response.getWriter().flush();
		        response.getWriter().close();

	}
	//����ҵĶ���
	@RequestMapping("/myOrders")
	public String myOrders(HttpServletRequest request) {
		HttpSession session = request.getSession();
		//�ж��û��Ƿ��Ѿ���¼
		User user = (User) session.getAttribute("user");
		if(user == null) {
			return "redirect:/login";
		}
		
		//��ѯ���û����ж�����Ϣ
		List<Order> orderList = productService.findAllOrders(user.getUid());
		//ѭ�����ж���
		if(orderList !=null) {
			for(Order order : orderList) {
				String oid = order.getOid();
				List<OrderItem> orderItem = productService.findAllOrderItemByOid(oid);
				order.getOrderItems().addAll(orderItem);
			}
		}
		request.setAttribute("orderList", orderList);
		return "order_list";
	}
	//δ֧����������
	@RequestMapping("/payOrder")
	public String payOrder(Order order,HttpServletRequest request) {
		HttpSession session = request.getSession();
		//���δ֧������
		String oid = order.getOid();
		 Order unorder = productService.findOrderByOid(oid);
		 
		 List<OrderItem> orderItem = productService.findAllOrderItemByOid(oid);
		 for(OrderItem orderItems : orderItem) {
			unorder.getOrderItems().add(orderItems); 
		 }
		 session.setAttribute("order", unorder);
		return "redirect:/order_uninfo";
	}
	@RequestMapping("/order_uninfo")
	public String order_uninfo() {
		return "order_uninfo";
	}
	
}

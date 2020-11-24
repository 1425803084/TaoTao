package com.ld.controller;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ld.bean.User;
import com.ld.service.UserService;
import com.ld.utils.CommonsUtils;
import com.ld.utils.MailUtils;

@Controller
public class UserController {
	@Autowired
	UserService userService;
	
	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	@RequestMapping("/register")
	public String register() {
		return "register";
	}
	@RequestMapping("/cart")
	public String cart() {
		return "cart";
	}
	@RequestMapping("/order_info")
	public String order_info() {
		return "order_info";
	}
	@RequestMapping("/registerSuccess")
	public String registerSuccess() {
		return "registerSuccess";
	}
	@RequestMapping("/registerFail")
	public String registerFail() {
		return "registerFail";
	}
	@RequestMapping("/order_list")
	public String order_list() {
		return "order_list";
	}
	@RequestMapping("/registpro")
	public String registpro(User user) {	
		user.setUid(CommonsUtils.getUUID());
		user.setTelephone(null);
		user.setState(0);
		String activeCode = CommonsUtils.getUUID();
		user.setCode(activeCode);
		int a = userService.addUser(user);
		if(a>0) {
			//�����ʼ�
			String emailMsg = "��ϲ��ע��ɹ���������������ӽ��м����˻�"
			+"<a href='http://localhost:8080/TaoTao/active?activeCode="+activeCode+"'>"
					+"http://localhost:8080/TaoTao/active?activeCode="+activeCode+"</a>";
			try {
				MailUtils.sendMail(user.getEmail(), emailMsg);
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//�����ɹ�����
			return "redirect:/registerSuccess";
		}else {
			//����ʧ�ܽ���
			return "redirect:/registerFail";
		}
	}
	@RequestMapping("/active")
	public String active(HttpServletRequest request) {
		int a = 1;
		String activeCode = request.getParameter("activeCode");
		userService.active(a,activeCode);
		return "redirect:/login";
		
	}
	@RequestMapping("/loginpro")
	public String loginpro(User user,HttpServletRequest request){
		HttpSession session = request.getSession(); 
		String username = user.getUsername();
		String pass = user.getPassword();
		User user1 = null;
		user1 = userService.findName(username,pass);
		String checkcode = request.getParameter("checkcode");
		String checkcode1 = session.getAttribute("checkcode_session").toString();
		if(user1 != null&&user1.getState()==1&&checkcode.equals(checkcode1))
		{
			session.setAttribute("user", user1);
			return "redirect:/index";
		}else {
			return "redirect:/login";
		}
	}
	@RequestMapping("/logout")
	public String logout(HttpServletRequest request,HttpServletResponse response) {
		HttpSession session = request.getSession();
		//��session�н�userɾ��
		session.removeAttribute("user");
		//���洢�ڿͻ��˵�cookieɾ����
		Cookie cookie_username = new Cookie("cookie_username", "");
		cookie_username.setMaxAge(0);
		Cookie cookie_password = new Cookie("cookie_password","");
		cookie_password.setMaxAge(0);
		
		response.addCookie(cookie_username);
		response.addCookie(cookie_password);
	
		return "redirect:/login";
		
	}
	
}

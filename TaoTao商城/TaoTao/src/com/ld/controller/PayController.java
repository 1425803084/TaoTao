package com.ld.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.ld.bean.Order;
import com.ld.bean.User;
import com.ld.service.PlayService;
import com.ld.utils.AlipayConfig;

@Controller
public class PayController {
	@Autowired
	PlayService payService;
	@RequestMapping("/return_url")
	public String notify_url(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException {
		//��ȡ֧����POST����������Ϣ
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
            }
            //����������δ����ڳ�������ʱʹ�á�
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
      //�м�alipaypublickey��֧�����Ĺ�Կ����ȥopen.alipay.com��ӦӦ���²鿴��
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset,AlipayConfig.sign_type);
        if(signVerified) {
            //�̻�������
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
 
            //֧�������׺�
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
 
            //������
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");
 
            request.setAttribute("out_trade_no", out_trade_no);
            request.setAttribute("trade_no", trade_no);
            request.setAttribute("total_amount", total_amount);

            //ϵͳ�������֧�����ص����Ķ���״̬�������������������
            payService.updateOrderState(out_trade_no);
        }
        request.setAttribute("signVerified", signVerified);
        return "index";
	}
}

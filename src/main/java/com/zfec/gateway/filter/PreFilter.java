package com.zfec.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.zfec.common.util.ResponseCode;
import com.zfec.common.util.ResponseData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
@Component
public class PreFilter extends ZuulFilter {
 
    private static Logger log = LoggerFactory.getLogger(PreFilter.class);
    private static final String ERROR_STATUS_CODE_KEY = "error.status_code";
 
    @Override
    public String filterType() {
        //前置过滤器
        return "pre";
    }
 
    @Override
    public int filterOrder() {
        //优先级，数字越大，优先级越低
        return 0;
    }
 
    @Override
    public boolean shouldFilter() {
    	return true;
    }
 
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		String auth = request.getHeader("token");
		String uri = request.getRequestURI().toString();
		log.info("send {} request to {}", request.getMethod(), uri);
		
		if (!uri.equalsIgnoreCase("/logservice/checkUser")) {
			if (auth == null) {
				log.warn("access token is empty");
				ResponseData responseData = ResponseData.fail("非法请求【缺少Authorization信息】", ResponseCode.NO_AUTH_CODE.getCode());
				// 过滤该请求，不往下级服务去转发请求，到此结束
				ctx.setSendZuulResponse(false);
				ctx.setResponseStatusCode(responseData.getCode());
				ctx.setResponseBody(JSON.toJSONString(responseData));
				ctx.getResponse().setContentType("text/html;charset=UTF-8");

//				ctx.set("error.status_code", 401);
//				ctx.set("error.message", JsonUtils
//						.toJson(ResponseData.fail("非法请求【缺少Authorization信息】", ResponseCode.NO_AUTH_CODE.getCode())));
//				ctx.set("error.exception", ctx.get("error.exception"));
				
				return ctx;
			}
			// 如果有token，则进行路由转发
			log.info("access token ok");
		}
		
		return null;
	}
}


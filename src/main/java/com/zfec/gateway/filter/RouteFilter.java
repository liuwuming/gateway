package com.zfec.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

//@Component
public class RouteFilter extends ZuulFilter {
 
    private static Logger log = LoggerFactory.getLogger(RouteFilter.class);
    private static final String ERROR_STATUS_CODE_KEY = "error.status_code";
 
    @Override
    public String filterType() {
        //前置过滤器
        return "route";
    }
 
    @Override
    public int filterOrder() {
        //优先级，数字越大，优先级越低
        return 1;
    }
 
    @Override
    public boolean shouldFilter() {
    	RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.containsKey(ERROR_STATUS_CODE_KEY);
		
//		return true;
    }
 
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
 
        log.info("send {} request to {}", request.getMethod(), request.getRequestURL().toString());
 
        //获取传来的参数accessToken
        Object accessToken = request.getParameter("accessToken");
        if(accessToken == null) {
            log.warn("access token is empty");
            //过滤该请求，不往下级服务去转发请求，到此结束
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            ctx.setResponseBody("{\"result\":\"accessToken为空!\"}");
            ctx.getResponse().setContentType("text/html;charset=UTF-8");
            
            ctx.set("error.status_code", 401);
			ctx.set("error.message", "{\"result\":\"accessToken为空!\"}");
			ctx.set("error.exception", ctx.get("error.exception"));
            return null;
        }
        //如果有token，则进行路由转发
        log.info("routing is ok");
        //这里return的值没有意义，zuul框架没有使用该返回值
        return null;
    }
 
}

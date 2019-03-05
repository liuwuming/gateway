package com.zfec.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.context.RequestContext;
import io.micrometer.core.instrument.util.StringUtils;

@Component
public class ErrorFilter extends ZuulFilter {

	private static final String ERROR_STATUS_CODE_KEY = "error.status_code";

	private Logger log = LoggerFactory.getLogger(ErrorFilter.class);

	public static final String DEFAULT_ERR_MSG = "系统繁忙,请稍后再试";

	@Override
	public String filterType() {
		return "error";
	}

	@Override
	public int filterOrder() {
		return 10;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		log.info(ctx.get(ERROR_STATUS_CODE_KEY) + "");
		return ctx.containsKey(ERROR_STATUS_CODE_KEY);
//        return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
        Throwable throwable = ctx.getThrowable();
        log.error("this is a ErrorFilter :" + throwable.getCause().getMessage(), throwable);
        ctx.set("error.status_code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ctx.set("error.exception", throwable.getCause());
        return null;
	}

	private Throwable getOriginException(Throwable e) {
		e = e.getCause();
		while (e.getCause() != null) {
			e = e.getCause();
		}
		return e;
	}
}

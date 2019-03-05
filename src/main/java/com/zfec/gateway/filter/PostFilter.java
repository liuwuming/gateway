package com.zfec.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.context.RequestContext;
import io.micrometer.core.instrument.util.StringUtils;

@Component
public class PostFilter extends ZuulFilter {

	private static final String ERROR_STATUS_CODE_KEY = "error.status_code";

	private Logger log = LoggerFactory.getLogger(PostFilter.class);

	public static final String DEFAULT_ERR_MSG = "系统繁忙,请稍后再试";

	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public int filterOrder() {
		return 2;
	}

	@Override
	public boolean shouldFilter() {
        return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();

		try {
			HttpServletRequest request = ctx.getRequest();

			if(null == ctx.get(ERROR_STATUS_CODE_KEY)) {
				return null;
			}
			
			int statusCode = (Integer) ctx.get(ERROR_STATUS_CODE_KEY);
			String message = (String) ctx.get("error.message");
			if (ctx.containsKey("error.exception")) {
				Throwable e = (Exception) ctx.get("error.exception");
				Throwable re = getOriginException(e);
				if (re instanceof java.net.ConnectException) {
					message = "Real Service Connection refused";
					log.warn("uri:{},error:{}", request.getRequestURI(), re.getMessage());
				} else if (re instanceof java.net.SocketTimeoutException) {
					message = "Real Service Timeout";
					log.warn("uri:{},error:{}", request.getRequestURI(), re.getMessage());
				} else if (re instanceof com.netflix.client.ClientException) {
					message = re.getMessage();
					log.warn("uri:{},error:{}", request.getRequestURI(), re.getMessage());
				} else {
					log.warn("Error during filtering", e);
				}
			}

			if (StringUtils.isBlank(message))
				message = DEFAULT_ERR_MSG;

			ctx.set("error.status_code", statusCode);
			ctx.set("error.message", message);
			ctx.set("error.exception", ctx.get("error.exception"));
		} catch (Exception e) {
			String error = "Error during filtering[ErrorFilter]";
			log.error(error, e);
			ctx.set("error.status_code", 500);
			ctx.set("error.message", error);
			ctx.set("error.exception", e);
		}
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

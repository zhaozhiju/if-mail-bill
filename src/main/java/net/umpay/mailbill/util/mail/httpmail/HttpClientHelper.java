package net.umpay.mailbill.util.mail.httpmail;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HttpClient 封装
 *
 * @author bangis.wangdf
 */
public class HttpClientHelper {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpClientHelper.class);
    private HttpClient       httpclient       = new DefaultHttpClient();
    private HttpContext      localContext     = new BasicHttpContext();
    private BasicCookieStore basicCookieStore = new BasicCookieStore();                          // cookie存储用来完成登录后记录相关信息
 
    private int              TIME_OUT         = 10;                                              // 连接超时时间
 
    public HttpClientHelper() {
        instance();
    }
 
    /**
     * 启用cookie存储
     */
    private void instance() {
        httpclient.getParams().setIntParameter("http.socket.timeout", TIME_OUT * 1000);
        localContext.setAttribute("http.cookie-store", basicCookieStore);// Cookie存储
    }
 
    /**
     * @param ssl boolean=true 支持https网址，false同默认构造
     */
    @SuppressWarnings({ "deprecation" })
	public HttpClientHelper(boolean ssl) {
        instance();
        if (ssl) {
            try {
                X509TrustManager tm = new X509TrustManager() {
 
                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }
 
                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }
 
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[] { tm }, null);
                SSLSocketFactory ssf = new SSLSocketFactory(ctx);
                ClientConnectionManager ccm = httpclient.getConnectionManager();
                SchemeRegistry sr = ccm.getSchemeRegistry();
                sr.register(new Scheme("https", ssf, 443));
            } catch (Exception e) {
               MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.HTTP_SSL_EXCEPTION_CODE, ErrorCodeContants.HTTP_SSL_EXCEPTION.getMsg(), log);
            }
        }
    }
 
    /**
     * @param url
     * @param headers 指定headers
     * @return HttpResult
     */
    public HttpResult get(String url, Header... headers) {
        HttpResponse response;
        HttpGet httpget = new HttpGet(url);
        if (headers != null) {
            for (Header h : headers) {
                httpget.addHeader(h);
            }
        } else {// 如不指定则使用默认
            Header header = new BasicHeader(
                                            "User-Agent",
                                            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;  .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.2)");
            httpget.addHeader(header);
        }
        HttpResult httpResult = HttpResult.empty();
        try {
            response = httpclient.execute(httpget, localContext);
            httpResult = new HttpResult(localContext, response);
        } catch (IOException e) {
            httpget.abort();
        }
        return httpResult;
    }
 
    public HttpResult post(String url, Map<String, String> data, Header... headers) {
        HttpResponse response;
        HttpPost httppost = new HttpPost(url);
        String contentType = null;
        if (headers != null) {
            int size = headers.length;
            for (int i = 0; i < size; ++i) {
                Header h = (Header) headers[i];
                if (!(h.getName().startsWith("$x-param"))) {
                    httppost.addHeader(h);
                }
                if ("Content-Type".equalsIgnoreCase(h.getName())) {
                    contentType = h.getValue();
                }
            }
 
        }
        if (contentType != null) {
            httppost.setHeader("Content-Type", contentType);
        } else if (data != null) {
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        }
 
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for (String key : data.keySet()) {
            formParams.add(new BasicNameValuePair(key, (String) data.get(key)));
        }
        HttpResult httpResult = HttpResult.empty();
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
            httppost.setEntity(entity);
            response = httpclient.execute(httppost, localContext);
            httpResult = new HttpResult(localContext, response);
        } catch (IOException e) {
//          httppost.abort();	//中断请求,接下来可以开始另一段请求
        	e.printStackTrace();
        } finally {
        }
        return httpResult;
    }
 
    /**
     * post 发送xml
     * @author zhaozj
     * @param url		post请求的链接
     * @param xml   	请求的xml报文
     * @param headers	收集头信息集合
     * @return HttpResult
     */
	public HttpResult postXML(String url, String xml, Header... headers) {
		HttpResponse response;
		HttpPost httppost = new HttpPost(url);
		setHttpPost(httppost, xml, headers);
		HttpResult httpResult = HttpResult.empty();
		try {
			StringEntity myEntity = new StringEntity(xml, "UTF-8");
			httppost.addHeader("Content-Type", "text/xml");
			httppost.setEntity(myEntity);
			response = httpclient.execute(httppost, localContext);
			httpResult = new HttpResult(localContext, response);
		} catch (IOException e) {
			//System.out.println("post xml 请求失败" + e.getMessage());
			log.error("post xml 请求失败"+e.getMessage());
		} finally {
			httppost.abort();
		}

		return httpResult;
	}
    
    // 设置httppost信息
    private void setHttpPost(HttpPost httppost, String xml, Header... headers){
    	String contentType = null;
        if (headers != null) {
            int size = headers.length;
            for (int i = 0; i < size; ++i) {
                Header h = (Header) headers[i];
                if (!(h.getName().startsWith("$x-param"))) {
                    httppost.addHeader(h);
                }
                if ("Content-Type".equalsIgnoreCase(h.getName())) {
                    contentType = h.getValue();
                }
            }
        }
        if (contentType != null) {
            httppost.setHeader("Content-Type", contentType);
        } else if (xml != null) {
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        }
    }
    public String getCookie(String name, String... domain) {
        String dm = "";
        if (domain != null && domain.length >= 1) {
            dm = domain[0];
        }
        for (Cookie c : basicCookieStore.getCookies()) {
            if (StringUtils.equals(name, c.getName()) && StringUtils.equals(dm, c.getDomain())) {
                return c.getValue();
            }
        }
        return null;
    }
 
    public void pringCookieAll() {
        for (Cookie c : basicCookieStore.getCookies()) {
            System.out.println(c);
        }
    }
}


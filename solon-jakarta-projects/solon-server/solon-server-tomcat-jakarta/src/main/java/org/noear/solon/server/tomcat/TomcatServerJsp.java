package org.noear.solon.server.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.noear.solon.server.prop.impl.HttpServerProps;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 * @author noear 2025/11/29 created
 *
 */
public class TomcatServerJsp extends TomcatServer {
    public TomcatServerJsp(HttpServerProps props) {
        super(props);
    }

    @Override
    protected void initContext() throws IOException {
        Context ctx = getContext();

        //jsp
        addJasperInitializerIfExists(ctx);
    }

    /**
     * 使用反射动态检查JasperInitializer类是否存在，并添加到StandardContext中
     */
    private void addJasperInitializerIfExists(Context ctxt) {
        try {
            // 1. 检查JasperInitializer类是否存在
            Class<?> jasperClass = Class.forName("org.apache.jasper.servlet.JasperInitializer");
            log.debug("JasperInitializer类存在，准备创建实例");

            addDefaultServlet(ctxt);
            addJspServlet(ctxt);
            // 2. 获取构造函数并创建实例
            Constructor<?> constructor = jasperClass.getDeclaredConstructor();
            Object jasperInstance = constructor.newInstance();
            // 3. 反射调用addServletContainerInitializer方法
            Method addMethod = Context.class.getMethod(
                    "addServletContainerInitializer",
                    jakarta.servlet.ServletContainerInitializer.class,
                    java.util.Set.class
            );
            addMethod.invoke(ctxt, jasperInstance, null);

            log.debug("已成功添加JasperInitializer到ServletContext");
            // Configure the defaults and then tweak the JSP servlet settings
            // Note: Min value for maxLoadedJsps is 2
//            Tomcat.initWebappDefaults(ctxt);
        } catch (ClassNotFoundException e) {
            log.debug("JasperInitializer类不存在: " + e.getMessage());
            log.debug("当前环境可能不支持JSP");
//        	e.printStackTrace();
        } catch (Exception e) {
            log.debug("执行过程中出现错误: " + e.getMessage());
//            e.printStackTrace();
        }
    }

    private void addDefaultServlet(Context context) {
        Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);

        defaultServlet.setOverridable(true);
        context.addChild(defaultServlet);
        context.addServletMappingDecoded("/", "default");
    }

    private void addJspServlet(Context context) {
        Wrapper jspServlet = context.createWrapper();
        jspServlet.setName("jsp");
        jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
        jspServlet.addInitParameter("fork", "false");
        jspServlet.addInitParameter("xpoweredBy", "false");
        jspServlet.setLoadOnStartup(3);

        context.addChild(jspServlet);
        context.addServletMappingDecoded("*.jsp", "jsp");
        context.addServletMappingDecoded("*.jspx", "jsp");
    }
}
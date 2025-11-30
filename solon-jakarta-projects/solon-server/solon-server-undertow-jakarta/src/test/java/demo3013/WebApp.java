package demo3013;

import org.noear.solon.Solon;
import org.noear.solon.annotation.SolonMain;

/**
 *
 * //资源路径说明（不用配置）
 * resources/app.properties（或 app.yml） 为应用配置文件
 * resources/WEB-INF/static/ 为静态文件根目标
 * resources/WEB-INF/templates/ 为视图文件根目标（支持多视图共存）
 *
 * */
@SolonMain
public class WebApp {
    public static void main(String[] args) {
//    	System.setProperty("jdk.module.illegalAccess", "permit");
        Solon.start(WebApp.class, args);
    }
}

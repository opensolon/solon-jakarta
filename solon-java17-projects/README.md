Solon 本身并不基于 java-ee 构建，可以同时适配 javax 和 jakarta 两套 java-ee 包名。 本仓库的项目基于 java 17 进行编译和发布，专门争对 “jakarta 10”（java-ee 的新名） 相关接口进行“适配”。

| 插件                             | 描述                           | 备注  |
|--------------------------------|------------------------------|-----|
| solon-boot-jetty-jakarta       | 对 jetty(12)-jakarta 进行适配     |     |
| solon-boot-undertow-jakarta    | 对 undertow(2.3)-jakarta 进行适配 |     |
|                                |                              |     |
| solon-logging-logback-jakarta  | 对 logback(1.15) 进行适配         | 已完成    |
|                                |                              |     |
| solon-web-servlet-jakarta      | 对 servlet-jakarta 进行适配       | 已完成 |
|                                |                              |     |
| solon-view-jsp-jakarta         | 对 jsp-jakarta 进行适配           | 已完成 |
|                                |                              |     |
| hibernate-jakarta-solon-plugin | 对 jap-jakarta 进行适配           |     |
|                                |                              |     |

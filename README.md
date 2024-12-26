<h1 align="center" style="text-align:center;">
<img src="solon_icon.png" width="128" />
<br />
Solon
</h1>
<p align="center">
	<strong>面向全场景的 Java 应用开发框架：克制、高效、开放、生态</strong>
    <br/>
    <strong>【开放原子开源基金会，孵化项目】</strong>
</p>
<p align="center">
	<a href="https://solon.noear.org/">https://solon.noear.org</a>
</p>

<p align="center">
    <a target="_blank" href="https://central.sonatype.com/search?q=org.noear%3Asolon-parent">
        <img src="https://img.shields.io/maven-central/v/org.noear/solon.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE">
		<img src="https://img.shields.io/:License-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-11-green.svg" alt="jdk-11" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-21-green.svg" alt="jdk-21" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-23-green.svg" alt="jdk-23" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/noear/solon/stargazers'>
		<img src='https://gitee.com/noear/solon/badge/star.svg' alt='gitee star'/>
	</a>
    <a target="_blank" href='https://github.com/noear/solon/stargazers'>
		<img src="https://img.shields.io/github/stars/noear/solon.svg?style=flat&logo=github" alt="github star"/>
	</a>
    <a target="_blank" href='https://gitcode.com/opensolon/solon/star'>
		<img src='https://gitcode.com/opensolon/solon/star/badge.svg' alt='gitcode star'/>
	</a>
</p>

<hr />

<p align="center">
并发高 300%；内存省 50%；启动快 10 倍；打包小 90%；同时支持 java8 ~ java22, native 运行时。
<br/>
从零开始构建，有更灵活的接口规范与开放生态
</p>

<hr />

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

# 指定基础镜像 这里springboot项目运行只需要java jdk环境即可
FROM java:latest
# 维护者信息
MAINTAINER wwhm
# 将本地的可执行文件拷贝到Docker容器中的根目录下
COPY start.sh /
#给app.sh赋予可执行权限
RUN chmod +x /start.sh
# 重命名
ADD *.jar demo-cloud-config.jar
# 对外暴漏的端口号
EXPOSE 8003
# 运行
#方式一
ENTRYPOINT ["/start.sh"]
# 方式二
#ENTRYPOINT ["java", "-jar", "demo-cloud-config.jar"]

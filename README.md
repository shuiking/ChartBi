## 项目介绍
一个基于React、springboot、dubbo、nacos、rabbitmq、mysql、mongodb的rpc框架的智能分析平台，区别于传统的BI项目，用户只需要导入原始的数据集、并输入分析诉求，就能自动生成可视化图表及分析结论，实现数据分析的降本增效。

## 组织结构
ChartBi  
├── analyze -- ai智能分析模块  
├── backend -- 用户信息管理系统服务  
├── common -- 工具类及通用代码模块  
├── gateway -- 基于Spring Cloud Gateway的微服务API网关服务

## 技术选型
| 技术 | 说明 |
| --- | --- |
| dubbo | RPC框架 |
| Spring Boot | 容器+MVC框架 |
| MyBatis-Plus | ORM框架 |
| Redis | 分布式缓存 |
| RabbitMq | 消息队列 |
| MongoDb | NoSql数据库 |
| MinIO | 对象存储 |
| Knife4j | 文档生产工具 |
| nacos | 服务注册和发现 |
| mysql | 数据库 |
## 系统架构图
![image](https://github.com/shuiking/ChartBi/assets/86963048/efa82de4-5bbe-4a9d-9d3b-2f4ab2cd77a2)

## 代码运行相关截图
![image](https://github.com/shuiking/ChartBi/assets/86963048/aca3cca6-1c0e-489e-b9e5-c4c9ac5f3550)
![image](https://github.com/shuiking/ChartBi/assets/86963048/470c413d-0024-45e7-9c3e-068de30c5344)
![image](https://github.com/shuiking/ChartBi/assets/86963048/e55f33b1-a885-4441-b752-2b67ff11a3e7)
![image](https://github.com/shuiking/ChartBi/assets/86963048/a37545d2-ae22-4261-b2e2-d4159028a6b2)
![image](https://github.com/shuiking/ChartBi/assets/86963048/58afe03a-ebda-462d-a0c9-3fff146593bf)
![image](https://github.com/shuiking/ChartBi/assets/86963048/b68a9a89-25c7-4aec-b9a5-b64a1faa746a)




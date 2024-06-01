## 一个简易的 RPC 框架

### 项目结构
 - annotation 注解模块
 - communication 通信模块, 默认提供netty通信实现
 - config 自动配置类, 提供 bean 的默认实现
 - entity 用到的一些消息和服务实体类
 - exception 异常对象
 - loadbalance 负载均衡器, 提供随机, 轮询, 加权, 信用
 - provider 服务端的服务搜集和提供者
 - proxy 动态代理生成器
 - registry 注册中心, 默认提供基于 zookeeper 的实现
 - serialize 序列化组件
 - spring 基于spring功能的一些自动化组件

### 完成程度
- 实现了服务注册,服务发现,服务通信,负载均衡,服务重试,服务降级的功能
- 基于spring做了自动配置, 可以实现开箱即用
- 配置项:
   - register-address: "127.0.0.1:2181"
   - server-port: 8889
   - serializer-algorithm: "Kryo"

### 架构
- 注册中心依赖 zookeeper, 需要单独启动 zookeeper 服务
- rpc服务器需要在启动类上使用 EnableRpcServer 启动, 默认不会启动
- 服务名为接口的全限定名, 客户端根据服务名在注册中心拿到服务详情,根据负载均衡选择一个发起调用
- 客户端连接关闭时间为30s, 30s没有发起请求就会关闭连接, 服务器40s没有收到请求也会关闭连接, 让客户端主动关闭 
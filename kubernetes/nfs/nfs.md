# k8s中使用nfs做为持久化存储

[参考链接](https://mp.weixin.qq.com/s/FRZppup6W_AS2O-_CR1KFg)

## 1. 安装 NFS 客户端

在安装 Kubernetes NFS Subdir External Provisioner 之前，Kubernetes 集群所有节点需要提前安装 NFS 客户端，否则部署过程会报错。不同操作系统 NFS 客户端的包名不同，CentOS 和 openEuler 中包名为 nfs-utils。

- 所有节点安装 NFS 客户端：
```bash
yum install nfs-utils
```

- 服务端：
````bash
mkdir -p /root/data/nfs
chown nobody:nobody /root/data/nfs
````

## 2. 编辑服务配置文件
配置 NFS 服务器数据导出目录及访问 NFS 服务器的客户端机器权限。

编辑配置文件 vi /etc/exports，添加如下内容：

```txt
/root/data/nfs 192.168.146.0/24(rw,sync,all_squash,anonuid=65534,anongid=65534,no_subtree_check)
```

```bash
systemctl enable nfs-server --now

exportfs -v
```

正确执行后，输出结果如下 :

```bash
$ exportfs -v
/root/data/nfs   192.168.146.0/24(sync,wdelay,hide,no_subtree_check,sec=sys,rw,secure,root_squash,all_squash)
```

在节点上查看 NFS 共享目录列表
```bash
$showmount -e 192.168.146.130
Export list for 192.168.146.130:
/root/data/nfs 192.168.146.0/24
```

如果无法列出，可能是被防火墙拦截了

## 3. 获取 NFS Subdir External Provisioner 部署文件

### 3.1 下载部署文件 
在 K8S 控制节点 group-01-master-01 ，下载最新版 nfs-subdir-external-provisioner-4.0.18 Releases 文件，并解压。

````bash
wget https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner/archive/refs/tags/nfs-subdir-external-provisioner-4.0.18.zip
unzip nfs-subdir-external-provisioner-4.0.18.zip
cd nfs-subdir-external-provisioner-nfs-subdir-external-provisioner-4.0.18/
````

### 3.2 创建 NameSpace
可选配置，默认为 default，新建方便资源管理。
````bash
kubectl create ns nfs-system
````

### 3.3 配置 RBAC authorization
替换命名空间名称
````bash
sed -i'' "s/namespace:.*/namespace: nfs-system/g" ./deploy/rbac.yaml ./deploy/deployment.yaml
````
创建 RBAC 资源
```bash
kubectl create -f deploy/rbac.yaml
```

### 3.4 配置 NFS subdir external provisioner
编辑 provisioner's deployment 文件 deploy/deployment.yaml，重点修改以下内容：

image: 默认使用 registry.k8s.io 镜像仓库的镜像 nfs-subdir-external-provisioner:v4.0.2，网络受限时需要想办法下载并上传到方便访问的镜像仓库
NFS 服务器的主机名或是 IP 地址
NFS 服务器导出的共享数据目录的路径（exportfs）
文件 deployment.yaml 默认内容如下：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nfs-client-provisioner
  labels:
    app: nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: nfs-system
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: nfs-client-provisioner
  template:
    metadata:
      labels:
        app: nfs-client-provisioner
    spec:
      serviceAccountName: nfs-client-provisioner
      containers:
      - name: nfs-client-provisioner
        image: swr.cn-north-4.myhuaweicloud.com/ddn-k8s/registry.k8s.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2
        volumeMounts:
          - name: nfs-client-root
            mountPath: /persistentvolumes
        env:
        - name: PROVISIONER_NAME
          value: k8s-sigs.io/nfs-subdir-external-provisioner
        - name: NFS_SERVER
          value: 192.168.146.130
        - name: NFS_PATH
          value: /root/data/nfs
      volumes:
        - name: nfs-client-root
          nfs:
            server: 192.168.146.130
            path: /root/data/nfs
```
说明：主要修改内容，用实际 NFS 配置信息替换默认值（受限于篇幅，未展示最终修改后的内容）

```yaml
image:  registry.k8s.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2
  
value: 192.168.146.130

value: /root/data/nfs
```

## 3.5 部署 NFS Subdir External Provisioner
执行部署命令
```bash
kubectl apply -f deploy/deployment.yaml
```

## 3.6 部署 Storage Class

### Step 1:
编辑 NFS subdir external provisioner 定义 Kubernetes Storage Class 的配置文件  deploy/class.yaml，重点修改以下内容：

存储类名称
存储卷删除后的默认策略
文件默认内容如下：

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: nfs-client
provisioner: k8s-sigs.io/nfs-subdir-external-provisioner # or choose another name, must match deployment's env PROVISIONER_NAME'
parameters:
  archiveOnDelete: "false"
```
修改后的文件内容如下：

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: nfs-sc
provisioner: k8s-sigs.io/nfs-subdir-external-provisioner
parameters:
  archiveOnDelete: "true"

```

### Step 2:
执行部署命令，部署 Storage Class。

```bash
kubectl apply -f deploy/class.yaml
```
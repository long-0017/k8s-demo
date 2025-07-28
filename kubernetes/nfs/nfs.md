https://mp.weixin.qq.com/s/FRZppup6W_AS2O-_CR1KFg

1.2 安装 NFS 客户端
在安装 Kubernetes NFS Subdir External Provisioner 之前，Kubernetes 集群所有节点需要提前安装 NFS 客户端，否则部署过程会报错。不同操作系统 NFS 客户端的包名不同，CentOS 和 openEuler 中包名为 nfs-utils。

安装 NFS 客户端
yum install nfs-utils


在服务端：
mkdir -p /datanfs/k8s
chown nobody:nobody /datanfs/k8s

2.4 编辑服务配置文件
配置 NFS 服务器数据导出目录及访问 NFS 服务器的客户端机器权限。

编辑配置文件 vi /etc/exports，添加如下内容：

/datanfs/k8s 192.168.9.0/24(rw,sync,all_squash,anonuid=65534,anongid=65534,no_subtree_check)

systemctl enable nfs-server --now

exportfs -v

正确执行后，输出结果如下 :

$ exportfs -v
/datanfs/k8s    192.168.9.0/24(sync,wdelay,hide,no_subtree_check,sec=sys,rw,secure,root_squash,all_squash)


查看 NFS 共享目录列表
$showmount -e 192.168.9.97
Export list for 192.168.9.97:
/datanfs/k8s 192.168.9.0/24


3.1 获取 NFS Subdir External Provisioner 部署文件
在 K8S 控制节点 ksp-control-1 ，下载最新版 nfs-subdir-external-provisioner-4.0.18 Releases 文件，并解压。

wget https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner/archive/refs/tags/nfs-subdir-external-provisioner-4.0.18.zip
unzip nfs-subdir-external-provisioner-4.0.18.zip
cd nfs-subdir-external-provisioner-nfs-subdir-external-provisioner-4.0.18/

3.2 创建 NameSpace
可选配置，默认为 default，新建方便资源管理。

kubectl create ns nfs-system
3.3 配置 RBAC authorization
替换命名空间名称
sed -i'' "s/namespace:.*/namespace: nfs-system/g" ./deploy/rbac.yaml ./deploy/deployment.yaml
创建 RBAC 资源
kubectl create -f deploy/rbac.yaml

3.4 配置 NFS subdir external provisioner
编辑 provisioner's deployment 文件 deploy/deployment.yaml，重点修改以下内容：

image: 默认使用 registry.k8s.io 镜像仓库的镜像 nfs-subdir-external-provisioner:v4.0.2，网络受限时需要想办法下载并上传到方便访问的镜像仓库
NFS 服务器的主机名或是 IP 地址
NFS 服务器导出的共享数据目录的路径（exportfs）
文件 deployment.yaml 默认内容如下：

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
image: registry.k8s.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2
volumeMounts:
- name: nfs-client-root
mountPath: /persistentvolumes
env:
- name: PROVISIONER_NAME
value: k8s-sigs.io/nfs-subdir-external-provisioner
- name: NFS_SERVER
value: 10.3.243.101
- name: NFS_PATH
value: /ifs/kubernetes
volumes:
- name: nfs-client-root
nfs:
server: 10.3.243.101
path: /ifs/kubernetes
说明：主要修改内容，用实际 NFS 配置信息替换默认值（受限于篇幅，未展示最终修改后的内容）

image:  registry.k8s.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2

value: 10.3.243.101

value: /ifs/kubernetes

3.5 部署 NFS Subdir External Provisioner
执行部署命令
kubectl apply -f deploy/deployment.yaml


3.6 部署 Storage Class
Step 1: 编辑 NFS subdir external provisioner 定义 Kubernetes Storage Class 的配置文件  deploy/class.yaml，重点修改以下内容：

存储类名称
存储卷删除后的默认策略
文件默认内容如下：

apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
name: nfs-client
provisioner: k8s-sigs.io/nfs-subdir-external-provisioner # or choose another name, must match deployment's env PROVISIONER_NAME'
parameters:
archiveOnDelete: "false"
修改后的文件内容如下：

apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
name: nfs-sc
provisioner: k8s-sigs.io/nfs-subdir-external-provisioner
parameters:
archiveOnDelete: "true"

Step 2: 执行部署命令，部署 Storage Class。

kubectl apply -f deploy/class.yaml
# 跨时间域山魈面部识别系统

基于深度学习的山魈（Mandrillus）个体面部识别系统，支持跨时间域的个体识别、注意力热力图可视化、个体时间轴管理与识别报告生成。

## 项目结构

```
A31/
├── backend/                    # Spring Boot 后端服务
│   ├── src/main/java/          # Java 源码（controller/service/mapper/entity）
│   ├── src/main/resources/     # 配置文件、SQL 脚本、MyBatis 映射
│   └── pom.xml
├── frontend/                   # Vue 3 前端应用
│   ├── src/                    # 页面、组件、路由、服务
│   └── package.json
├── Code/
│   ├── ml_service/             # Python FastAPI 推理服务
│   │   ├── main.py             # 推理 API 入口
│   │   ├── model.py            # Swin-Tiny 模型定义
│   │   └── requirements.txt
│   └── Dataprepare.py          # 数据集预处理脚本
├── Data/                       # 数据集（不纳入版本控制）
│   └── Mandrillus/
├── .env.example                # 环境变量模板
├── docker-compose.yml          # 一键部署编排
├── LICENSE                     # MIT 许可证
└── README.md
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + ECharts + Tailwind CSS |
| 后端 | Spring Boot 4 + MyBatis + MySQL + BCrypt |
| 算法 | Python + FastAPI + PyTorch + timm (Swin-Tiny) |
| 部署 | Docker Compose |

## 快速开始

### 前置要求

- JDK 21+
- Node.js 20.19+ / 22.12+
- Python 3.10+
- MySQL 8.0+
- CUDA（可选，用于 GPU 推理）

### 1. 克隆仓库

```bash
git clone <repository-url>
cd A31
```

### 2. 配置环境变量

```bash
# 前端
cp .env.example .env.development

# 后端
cp backend/src/main/resources/application-example.properties \
   backend/src/main/resources/application.properties
# 编辑 application.properties，填入数据库密码等配置
```

### 3. 初始化数据库

```bash
mysql -u root -p
CREATE DATABASE mandrill CHARACTER SET utf8mb4;
USE mandrill;
SOURCE backend/src/main/resources/sql/schema.sql;
```

### 4. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

后端默认运行在 `http://localhost:8080`，首次启动自动创建管理员账号 `admin / admin123`（**请及时修改密码**）。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`。

### 6. 启动算法服务（可选）

```bash
cd Code/ml_service
pip install -r requirements.txt
# 设置模型权重路径
export MODEL_PATH=/path/to/clean_model.pth
export GALLERY_DIR=/path/to/gallery
uvicorn main:app --host 0.0.0.0 --port 8000
```

未启动算法服务时，后端默认使用 Mock 模式返回演示结果。

## Docker 部署

```bash
docker-compose up -d
```

服务启动后：
- 前端：http://localhost:80
- 后端 API：http://localhost:8080
- 算法服务：http://localhost:8000

## 环境变量说明

### 后端

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SERVER_PORT` | 8080 | 服务端口 |
| `DB_HOST` | localhost | MySQL 主机 |
| `DB_PORT` | 3306 | MySQL 端口 |
| `DB_NAME` | mandrill | 数据库名 |
| `DB_USERNAME` | root | 数据库用户 |
| `DB_PASSWORD` | (空) | 数据库密码（**必填**） |
| `ALGORITHM_SERVICE_URL` | http://localhost:8000/predict | 算法推理服务地址 |
| `UPLOAD_PATH` | ./uploads | 上传文件存储路径 |
| `ALGORITHM_MOCK_ENABLED` | false | 是否启用 Mock 算法 |
| `CORS_ALLOWED_ORIGINS` | http://localhost:5173 | 允许的跨域源（逗号分隔） |

### 前端

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VITE_API_BASE_URL` | /api | API 基础路径 |
| `VITE_APP_TITLE` | Mandrill Recognition System | 应用标题 |
| `VITE_DEV_PROXY_TARGET` | http://localhost:8080 | 开发代理目标 |

## API 文档

后端启动后访问 `http://localhost:8080/api/health` 检查服务状态。

主要接口：
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册
- `POST /api/recognition/upload` - 上传识别
- `GET /api/recognition/records` - 识别记录列表
- `GET /api/recognition/individuals` - 个体列表

## 安全说明

- 密码使用 BCrypt 加密存储
- Token 基于内存会话存储（生产环境建议升级 Redis）
- **切勿将 `.env`、`application.properties` 等含真实密钥的文件提交到版本库**
- 部署前务必修改默认管理员密码
- 生产环境请设置 `ALGORITHM_MOCK_ENABLED=false` 并限制 `CORS_ALLOWED_ORIGINS`

## 数据集

本项目使用 [Mandrillus Face Database](https://zenodo.org/records/7467318)，包含 2012-2021 年间拍摄的山魈面部图像。数据集不包含在本仓库中，请自行下载并放置于 `Data/Mandrillus/` 目录。

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可证

[MIT License](LICENSE)

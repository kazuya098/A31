# 注册接口：`POST /api/auth/register`

## 基本信息

- **URL**：`/api/auth/register`
- **Method**：POST
- **Content-Type**：`application/json`
- **是否需要登录**：否（已加入白名单）

## Request Body

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 用户名，不能为空 |
| `password` | string | 是 | 密码，不能为空 |

示例：

```json
{
  "username": "alice",
  "password": "alice123"
}
```

## Response（统一 `Result` 结构）

### 1）注册成功（200）

返回 `Result<LoginResponse>`，注册成功后会自动登录并返回 token。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "user": {
      "id": 1,
      "username": "alice",
      "role": "user"
    }
  }
}
```

### 2）用户名已存在（409）

```json
{
  "code": 409,
  "message": "用户名已存在",
  "data": null
}
```

### 3）参数校验失败（400）

例如 `username` / `password` 为空等，来自全局校验异常处理。

```json
{
  "code": 400,
  "message": "username: 用户名不能为空; password: 密码不能为空",
  "data": null
}
```

### 4）服务器异常（500）

```json
{
  "code": 500,
  "message": "服务器内部错误",
  "data": null
}
```

## 调用说明

- 注册成功后，前端需保存 `token`，后续请求在 Header 携带：
  - `X-Auth-Token: <token>`


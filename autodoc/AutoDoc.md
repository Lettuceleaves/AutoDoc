# 接口文档: TestController

**基础路径 (Base URL):** `/test`

---

## 1. hello (`GET /test/hello`)

**完整方法签名:** `com.letuc.test.controller.TestController.hello()`

### 请求参数 (Request)

*无请求参数。*

### 响应内容 (Response)

**响应类型:** `String`

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `value` | `byte[]` | |
| `coder` | `byte` | |
| `hash` | `int` | |
| `hashIsZero` | `boolean` | |

#### 响应体示例 (Response Body Example)

```json
{
  "value" : "base64-encoded-string",
  "coder" : null,
  "hash" : 1,
  "hashIsZero" : true
}
```

---

## 2. test2 (`POST /test/test2`)

**完整方法签名:** `com.letuc.test.controller.TestController.test2(com.letuc.test.model.UserDTO)`

### 请求参数 (Request)

| 参数名 | 类型 | 位置 | 描述 |
| :--- | :--- | :--- | :--- |
| `data` | `com.letuc.test.model.UserDTO` | `BODY` | |
| &nbsp;&nbsp;&nbsp;↳ `username` | `String` | BODY | |
| &nbsp;&nbsp;&nbsp;↳ `password` | `String` | BODY | |
| &nbsp;&nbsp;&nbsp;↳ `age` | `Integer` | BODY | |
| &nbsp;&nbsp;&nbsp;↳ `status` | `int` | BODY | |
| &nbsp;&nbsp;&nbsp;↳ `sex` | `char` | BODY | |
| &nbsp;&nbsp;&nbsp;↳ `admin` | `boolean` | BODY | |
| &nbsp;&nbsp;&nbsp;↳ `photo` | `byte[]` | BODY | |

#### 请求体示例 (Request Body Example)

```json
{
  "username" : "string",
  "password" : "string",
  "age" : 1,
  "status" : 1,
  "sex" : "string",
  "admin" : true,
  "photo" : "base64-encoded-string"
}
```

### 响应内容 (Response)

**响应类型:** `com.letuc.test.result.ResultVO<com.letuc.test.model.UserDTO>`

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `code` | `String` | |
| `data` | `com.letuc.test.model.UserDTO` | |
| &nbsp;&nbsp;&nbsp;↳ `username` | `String` | |
| &nbsp;&nbsp;&nbsp;↳ `password` | `String` | |
| &nbsp;&nbsp;&nbsp;↳ `age` | `Integer` | |
| &nbsp;&nbsp;&nbsp;↳ `status` | `int` | |
| &nbsp;&nbsp;&nbsp;↳ `sex` | `char` | |
| &nbsp;&nbsp;&nbsp;↳ `admin` | `boolean` | |
| &nbsp;&nbsp;&nbsp;↳ `photo` | `byte[]` | |
| `message` | `String` | |

#### 响应体示例 (Response Body Example)

```json
{
  "code" : "string",
  "data" : {
    "username" : "string",
    "password" : "string",
    "age" : 1,
    "status" : 1,
    "sex" : "string",
    "admin" : true,
    "photo" : "base64-encoded-string"
  },
  "message" : "string"
}
```

---


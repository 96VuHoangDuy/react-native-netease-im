# 国内厂商离线推送通道配置指南（CSKH 语音来电通知）

> 目的：让「客服语音来电」在 App 被杀死（离线）时，也能以**来电级别**的通知送达用户 —— 响铃、横幅（heads-up）、锁屏展示，而不是被当成普通 IM 消息（静音、无横幅）。
>
> 本文档供中国团队执行厂商控制台侧的申请/配置。客户端代码已就绪（见 §4），缺的是各厂商控制台的通道/分类审批。
>
> 生成时间：2026-07-31。文中控制台入口和审批时限以各厂商官方文档为准，可能随版本变化。

---

## 1. 核心概念：两层 Channel，缺一不可

| 层 | 谁创建 | 作用 |
|---|---|---|
| **客户端本地 NotificationChannel** | App 代码（首次启动时创建） | 决定通知的响铃声音、重要级（heads-up）、震动、锁屏可见性。**创建后声音/重要级不可修改** |
| **厂商控制台通道/消息分类** | 开发者在厂商推送控制台申请 | 决定厂商推送服务是否**放行**该消息、以什么优先级展示。未申请 → 消息被降级或**直接丢弃** |

- **FCM（海外 GMS 机型）只需要第一层**：`channel_id` 指向 App 本地 channel 即可。
- **国内厂商（小米/华为/OPPO/vivo/荣耀）都有第二层管控**，机制各不相同（详见 §3）。

**实测现象**（2026-07-31，小米 MIX 2S / MIUI）：payload 中携带未在小米控制台注册的
`channel_id=cskh_incoming_call_v2` 时，来电推送**不展示任何通知**；而不带 channel_id 的普通 IM
消息推送正常展示（走已注册的 channel `147183`「用户聊天消息」）。符合小米「私信消息必须携带已审核
channel_id」的管控逻辑。

---

## 2. 消息链路（谁负责什么）

```
主叫端（客服侧 App / 客服系统）
   └─ 发起呼叫时在 NECallPushConfig 中携带 pushTitle / pushContent / pushPayload
        └─ 网易云信服务器（只做透传，把 payload 映射到各厂商推送字段）
             └─ 厂商推送服务（小米/华为/OPPO/vivo/荣耀/FCM）——按控制台审批结果放行或降级
                  └─ 被叫端手机系统绘制通知（使用 payload 里的 channel_id 匹配本地 channel）
```

> ⚠️ **关键注意：payload 由主叫端（客服侧）发出，被叫端无法补救。**
> 客服无论从哪个客户端发起呼叫（手机 App、Web 客服工作台、第三方客服系统），**发起呼叫的那个客户端
> 必须携带完全一致的 pushPayload + pushTitle + pushContent**。只要有一个客服入口没带 payload，
> 从该入口发起的来电在用户手机上就会退化成普通消息通知（错误声音/无横幅）甚至无通知。

### 2.1 如果客服端不携带 payload，会发生什么（实测结论）

仅影响**被叫端 App 已被杀死**的场景（App 存活时铃声由 App 内部播放，与 payload 无关）：

| 被叫机型（App 已杀死） | 不带 channel_id payload 的表现 | 是否实测 |
|---|---|---|
| 海外机型 / FCM | 落入 IM 消息 channel `nim_message_channel_001`（importance 默认级）：**短信提示音、无铃声、无横幅、不亮锁屏** | ✅ 已实测（vivo V2332 国际版） |
| 小米 | 落入控制台默认 channel `147183`（用户聊天消息）：有通知、消息提示音、无铃声 | ✅ 已实测（MIX 2S） |
| 华为 / OPPO / vivo 国内版 / 荣耀 | 同理按普通消息级别展示，视各厂商分类策略可能进一步被静音 | 未实测 |

若客服端连 `pushTitle/pushContent` 也未携带，通知内容为云信默认值：标题「您有新的来电」、
内容为**原始 accid**（一串哈希）—— 用户完全无法识别是谁来电。

**一句话总结：用户关掉 App 后，来电看起来就是一条普通消息，极易被错过。**
这也是 §5 行动清单第 6 项要求逐一排查所有客服呼叫入口的原因：漏掉任何一个入口，
从该入口发起的来电就是上表的退化体验。

---

## 3. 各厂商要求与申请事项

网易云信 payload 与各厂商字段的映射，见官方文档
[《推送payload配置》](https://doc.yunxin.163.com/messaging/server-apis/DQyNjc5NjE?platform=server)。

### 3.1 小米（Xiaomi / MIUI / 澎湃OS）——必须在控制台注册 channel

- **机制**：私信消息必须携带**小米推送运营平台注册并审核通过的 channel_id**。未注册的 channel_id
  → 消息被丢弃/不展示（已实测）。
- **申请路径**：小米推送运营平台 → 「消息分类管理 → channel列表」 → 「新建」。
  单应用最多 30 个 channel；审核约 **5 个工作日**，通过后系统自动生成 channel_id。
- **2026 新规**：私信消息需同时携带 channel_id 及**模板 id**（模板接入见下方文档）。请一并申请
  「来电通知」模板。
- **payload 位置**：payload 根级 `channel_id`。
- **需要申请的内容**：新建一个「语音来电」channel（私信类、重要级别，建议带响铃权益），拿到
  channel_id 后回传给客户端团队替换 payload 常量。
- 文档：
  - [小米推送2026年消息分类新规](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2321)
  - [小米推送模板接入指南](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2314)
  - [推送消息限制说明](https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1656)
- **现状**：本应用 IM 消息目前使用已注册 channel `147183`（用户聊天消息）。来电尚无注册 channel。

### 3.2 华为（Huawei / HMS）——channel 本地创建即可，但需申请「自分类权益」

- **机制**：华为不在控制台注册 channel（`hwField.channel_id` 指向 App 本地 channel），但华为智能
  分类系统会把消息分为「服务与通讯」/「资讯营销」两类，分类错误会导致来电消息被静默展示。
- **申请路径**：在 AppGallery Connect 申请**通知消息自分类权益**，审批约 **15 个工作日**。
  获得权益后消息按开发者声明的类别（服务与通讯）展示，不被降级。
- **可选增强**：华为支持来电类 category（如 VOIP 场景的特殊权益），可在申请自分类权益时一并咨询
  hwpush@huawei.com。
- **payload 位置**：`hwField.channel_id`（本地 channel id：`cskh_incoming_call_v2`）。
- 文档：
  - [Push Kit 官方页](https://developer.huawei.com/consumer/cn/hms/huawei-pushkit/)
  - [华为消息分类问题集合（官方论坛整理）](https://www.cnblogs.com/developer-huawei/p/16503425.html)

### 3.3 OPPO（ColorOS）——需申请「私信通道」并在控制台登记通道

- **机制**：OPPO 对普通推送限量；即时通讯/来电类需求需申请**私信通道**（不限量）。通道需在
  OPPO 推送平台「配置管理 → 通道配置」登记，并通过邮件申请私信权益，约 **7 个工作日**答复。
- **payload 位置**：`oppoField.channel_id` —— 必须与 OPPO 控制台登记的通道一致。
- **需要申请的内容**：登记「语音来电」通道 + 私信权益，拿到 channel id 后回传客户端团队。
- 文档：
  - [OPPO PUSH 通知通道（Channel）适配（官方社区）](https://open.oppomobile.com/bbs/forum.php?mod=viewthread&tid=1914)
  - [OPPO 开放平台推送服务](https://open.oppomobile.com/newservice/capability?pagename=push)
  - [申请OPPO私信通道与接入新消息分类（阿里云 EMAS 整理，流程参考）](https://help.aliyun.com/zh/document_detail/613899.html)

### 3.4 vivo（OriginOS/FuntouchOS）——没有 channel_id，走消息分类 + 工单

- **机制**：vivo 推送**不支持 channel_id**。消息分为「系统消息」（用户强关联、仅 API 下发）和
  「运营消息」。来电属于系统消息；可通过**工单系统申请系统消息不限量**。
- **payload 位置**：`vivoField` 里设置 `classification`/`category`（IM 类），无 channel 字段。
- **注意**：vivo 国际版机型（Global，带 GMS）走的是 **FCM**，不受本节限制（本次测试用的
  vivo V2332 国际版即走 FCM，来电响铃已验证可行）。
- 文档：
  - [推送消息分类说明](https://dev.vivo.com.cn/documentCenter/doc/359)
  - [vivo推送产品说明](https://dev.vivo.com.cn/documentCenter/doc/180)
  - [系统消息不限量申请（工单）](https://dev.vivo.com.cn/documentCenter/doc/c7facbddba34455aa7bd0dd99767801e)

### 3.5 荣耀（Honor / MagicOS）——importance 分类，无 channel 注册

- **机制**：荣耀按 `importance`（NORMAL=服务通讯，LOW=资讯营销）分类展示；需签署推送服务协议并
  按分类标准正确标记。来电应标记为 NORMAL（服务通讯类）。如需自分类权益同样需申请。
- **payload 位置**：`honorField`（importance 等），无 channel_id 字段。
- 文档：
  - [荣耀推送服务消息分类标准（官方博客）](https://blog.csdn.net/HONOR_Developer/article/details/134034503)
  - [HONOR Push 下行消息（开发者官网）](https://developer.honor.com/doc/guides/101087)

### 3.6 FCM（海外 GMS 机型）——无需控制台审批 ✅ 已验证

- `fcmFieldV1.message.android.notification.channel_id` 指向 App 本地 channel 即可。
- 云信默认 `android_channel_id="0"`；我们在每条来电推送里显式覆盖为 `cskh_incoming_call_v2`。
- 已在 vivo V2332（国际版, Android 15, FCM）实测：响铃 + 横幅正常。

---

## 4. 客户端现状（代码已完成的部分）

App（Android）首次启动时创建两个本地 channel：

| Channel ID | 用途 | 配置 |
|---|---|---|
| `cskh_incoming_call_v2` | App 被杀死时，系统按离线推送绘制的来电通知 | IMPORTANCE_HIGH，铃声 = App 内置 `caller_ring.mp3`（25s），震动，锁屏可见 |
| `cskh_incoming_call_alive_v2` | App 存活时 SDK 的来电 fallback 通知 | IMPORTANCE_HIGH，**无声音**（铃声由 App 内 SoundHelper 播放，避免双重响铃），震动 |

主叫端发起呼叫时携带的 pushPayload（当前版本）：

```json
{
  "channel_id": "cskh_incoming_call_v2",
  "hwField":   { "channel_id": "cskh_incoming_call_v2" },
  "oppoField": { "channel_id": "cskh_incoming_call_v2" },
  "fcmFieldV1": {
    "message": {
      "android": { "notification": { "channel_id": "cskh_incoming_call_v2" } }
    }
  }
}
```

> 待各厂商控制台审批完成后需要调整：
> - 小米：根级 `channel_id` 改为小米控制台生成的 channel_id（并按 2026 新规补模板 id）。
> - OPPO：`oppoField.channel_id` 改为 OPPO 控制台登记的通道 id。
> - 华为：保持本地 channel id 不变，权益生效后自动按「服务与通讯」展示。
> - vivo 国内版 / 荣耀：补充 `vivoField` / `honorField` 分类字段（拿到审批结论后由客户端团队补）。

---

## 5. 中国团队行动清单

| # | 厂商 | 事项 | 预计时限 | 产出物（回传给客户端团队） |
|---|---|---|---|---|
| 1 | 小米 | 推送运营平台新建「语音来电」channel（私信/重要）+ 来电模板 | ~5 工作日 | channel_id + 模板 id |
| 2 | OPPO | 登记「语音来电」通道 + 申请私信权益 | ~7 工作日 | channel id |
| 3 | 华为 | 申请通知消息自分类权益（服务与通讯类），咨询来电/VOIP 特殊权益 | ~15 工作日 | 权益生效确认 |
| 4 | vivo | 工单申请系统消息（IM/来电类）不限量 | 依工单 | 审批确认 + 应配置的 classification 值 |
| 5 | 荣耀 | 确认签署推送协议、来电按 NORMAL（服务通讯）分类；如需自分类权益一并申请 | 依审批 | 确认结论 |
| 6 | 全部 | **梳理所有客服发起呼叫的入口**（手机 App / Web 工作台 / 其他系统），确认每个入口发呼叫时都携带 §4 的 payload 与 pushTitle/pushContent | — | 入口清单 + 确认 |

---

## 6. 常见问题

**Q: 为什么被叫端 App 升级了还不够？**
A: payload 是主叫端发出的。客服侧客户端不带 payload，用户端装了新版也收不到来电级通知（见 §2）。

**Q: 为什么不能所有厂商共用一个 channel id？**
A: FCM/华为的 channel_id 指向 App 本地 channel（可以统一）；小米/OPPO 的 channel_id 是厂商控制台
分配/登记的，必须用各自控制台的值。vivo/荣耀根本没有 channel_id，走消息分类。

**Q: App 被杀死时通知铃声响多久？**
A: 通知铃声播放一次（当前铃声 25 秒），不循环；主叫挂断后铃声不会提前停止（系统绘制的通知，
App 无进程可干预）。这是所有厂商系统通知的共同限制。

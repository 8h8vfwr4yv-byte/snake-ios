# 贪吃蛇（iOS 版）

SwiftUI 写的贪吃蛇，和安卓版同样的玩法：滑动或点方向键控制，吃红点 +10 分、变长、加速，撞墙或咬到自己结束，最高分会存下来。

## 在 Mac 上装到 iPad（约 5 分钟）

1. 打开 **Xcode**，选 File → New → Project → iOS → **App**，点 Next。
2. Product Name 填 **Snake**，Interface 选 **SwiftUI**，Language 选 **Swift**，其余默认，Next，找个地方保存。
3. 在左侧文件列表里点开 **ContentView.swift**，把里面内容**全选删掉**，换成本仓库的 `ContentView.swift` 的全部内容（SnakeApp.swift 不用动，Xcode 生成的就对）。
4. 用数据线把 iPad 连上 Mac，在 Xcode 顶部把运行设备选成你的 iPad。
5. 点项目设置里的 Signing & Capabilities，Team 选你自己的 Apple ID（没有就点 Add Account 登录一下，免费账号就行）。
6. 点左上角 ▶ 运行，等它装到 iPad 上。第一次打开如果提示"不受信任的开发者"，去 iPad 的 设置 → 通用 → VPN 与设备管理 里信任一下。

注意：免费 Apple ID 签的 App 有效期 7 天，过期后把 iPad 连上 Mac 再点一次 ▶ 就续上了。付费开发者账号签的是一年。

## 玩法

- 点屏幕或方向键开始
- 在棋盘上滑动转向，或用底部 ◀▲▼▶ 按钮
- 吃红点 +10 分，速度越来越快；撞墙/咬到自己结束，点一下再来一局

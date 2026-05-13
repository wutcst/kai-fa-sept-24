# World of Zuul 小组协同开发项目

本项目是武汉理工大学软件工程实践二的小组作业，基于 `world-of-zuul` 文本冒险游戏进行扩展。项目使用 Maven 管理构建、测试和打包，并通过 GitHub Actions 自动检查。

## 功能说明

玩家在校园场景中移动、查看房间、拾取和丢弃物品，并通过魔法饼干提升负重能力。当前实现包含以下扩展：

- 房间支持任意数量物品，每个物品都有描述和重量。
- `look` 命令显示当前房间、出口和房间内物品。
- `back` 命令支持连续回退到之前经过的房间。
- 独立 `Player` 类保存玩家姓名、当前位置、背包和最大负重。
- `take`、`drop`、`drop all`、`items` 命令支持拾取、丢弃和查看物品。
- `eat cookie` 命令可以吃掉魔法饼干并提升最大负重。
- 传送房间会在玩家进入后随机传送到其他房间。
- 命令处理使用注册表分发，新增命令时不需要在主流程里堆叠大量 `if` 判断。
- 游戏包含明确目标：取得钥匙、打开资料室、拿到宝藏并回到入口通关。
- 增加分数系统，拾取物品、解锁房间和通关都会增加分数。
- 增加生命值和危险房间，进入实验室会扣除生命值。
- 增加 `quest`、`score`、`status`、`map` 命令，方便查看任务、分数、状态和已探索地图。
- 增加 `save`、`load` 命令，可保存和读取当前进度。
- 支持方向别名和命令别名，例如 `e`、`w`、`north`、`get book`。

## 运行环境

- JDK 8 或更高版本
- Maven 3.8 或更高版本

## 本地运行

```bash
mvn test
mvn package
java -jar target/zuul-1.0-SNAPSHOT.jar
```

进入游戏后可以尝试：

```text
look
quest
go west
take key
back
go south
go east
go east
take treasure
back
back
back
```

也可以尝试较短的别名：

```text
e
get book
items
```

## 常用命令

```bash
mvn test
```

运行所有自动化测试。

```bash
mvn package
```

编译、测试并生成可执行 jar。

```bash
java -jar target/zuul-1.0-SNAPSHOT.jar
```

启动游戏。

游戏存档会写入当前目录的 `zuul-save.properties`，该文件已加入 `.gitignore`，不会被提交。

## 项目结构

```text
src/main/java/cn/edu/whut/sept/zuul/
  Main.java          游戏入口
  Game.java          游戏主流程和命令处理
  Parser.java        命令解析
  Command.java       命令对象
  CommandWords.java  有效命令表
  Room.java          房间、出口和房间物品
  Player.java        玩家状态和背包
  Item.java          游戏物品

src/test/java/cn/edu/whut/sept/zuul/
  GameTest.java      游戏功能测试
```

## 协作流程

- `master` 分支保存稳定版本。
- 每个功能使用独立分支开发，例如 `feature/player-inventory`。
- 提交信息使用简短祈使句，例如 `add player inventory`。
- 功能合并前先运行 `mvn test`，确保测试通过。
- 推送或创建 Pull Request 后，GitHub Actions 会自动运行测试和打包。

## 测试覆盖

当前测试覆盖未知命令、帮助和退出、正常移动、无效方向、房间查看、连续回退、拾取和丢弃物品、负重限制、魔法饼干、传送房间、方向别名、钥匙锁门、通关、生命值、任务/地图/状态命令、保存和读取等主要流程。

import SwiftUI
import Combine

struct ContentView: View {
    // MARK: - Game constants
    private let cols = 20
    private let rows = 20

    // MARK: - State
    @State private var snake: [CGPoint] = []
    @State private var food = CGPoint(x: 15, y: 10)
    @State private var dir = CGPoint(x: 1, y: 0)
    @State private var nextDir = CGPoint(x: 1, y: 0)
    @State private var score = 0
    @State private var best = UserDefaults.standard.integer(forKey: "snakeBest")
    @State private var state: GameState = .ready
    @State private var timer = Timer.publish(every: 0.15, on: .main, in: .common).autoconnect()

    enum GameState { case ready, running, over }

    var body: some View {
        ZStack {
            // 鲜艳的渐变背景，让液态玻璃有东西可以折射
            LinearGradient(
                colors: [
                    Color(red: 0.10, green: 0.16, blue: 0.45),
                    Color(red: 0.38, green: 0.16, blue: 0.62),
                    Color(red: 0.02, green: 0.45, blue: 0.50)
                ],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(spacing: 16) {
                // 顶部计分玻璃胶囊
                GlassEffectContainer {
                    HStack(spacing: 10) {
                        Text("得分 \(score)")
                            .font(.headline)
                            .padding(.horizontal, 16).padding(.vertical, 8)
                            .glassEffect(.regular, in: Capsule())
                        Text("最高 \(best)")
                            .font(.headline)
                            .padding(.horizontal, 16).padding(.vertical, 8)
                            .glassEffect(.regular, in: Capsule())
                    }
                }
                .foregroundStyle(.white)
                .padding(.top, 10)

                GeometryReader { geo in
                    let side = min(geo.size.width, geo.size.height)
                    let cell = side / CGFloat(cols)
                    ZStack {
                        // 棋盘底下的玻璃卡片
                        RoundedRectangle(cornerRadius: 28)
                            .fill(.clear)
                            .frame(width: side + 16, height: side + 16)
                            .glassEffect(.regular, in: RoundedRectangle(cornerRadius: 28))

                        Canvas { ctx, _ in
                            let board = CGRect(x: 0, y: 0, width: cell * CGFloat(cols), height: cell * CGFloat(rows))
                            ctx.fill(Path(roundedRect: board, cornerRadius: cell * 0.6),
                                     with: .color(Color(red: 0.05, green: 0.07, blue: 0.10).opacity(0.82)))
                            let foodRect = CGRect(x: food.x * cell + cell * 0.14, y: food.y * cell + cell * 0.14,
                                                  width: cell * 0.72, height: cell * 0.72)
                            ctx.fill(Path(ellipseIn: foodRect), with: .color(.red))
                            for (i, seg) in snake.enumerated() {
                                let r = CGRect(x: seg.x * cell + cell * 0.08, y: seg.y * cell + cell * 0.08,
                                               width: cell * 0.84, height: cell * 0.84)
                                let color: Color = i == 0
                                    ? Color(red: 0.49, green: 0.83, blue: 0.13)
                                    : Color(red: 0.32, green: 0.63, blue: 0.16)
                                ctx.fill(Path(roundedRect: r, cornerRadius: cell * 0.25), with: .color(color))
                            }
                        }
                        .frame(width: side, height: side)

                        if state != .running {
                            VStack(spacing: 14) {
                                Text(state == .ready ? "贪 吃 蛇" : "游戏结束")
                                    .font(.largeTitle.bold())
                                    .foregroundColor(.white)
                                Text(state == .ready ? "滑动屏幕或点方向键控制" : "得分 \(score)")
                                    .foregroundColor(.white.opacity(0.85))
                                Button(state == .ready ? "开始游戏" : "再来一局") { start() }
                                    .buttonStyle(.glassProminent)
                                    .controlSize(.large)
                            }
                            .padding(24)
                            .glassEffect(.regular, in: RoundedRectangle(cornerRadius: 24))
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture(minimumDistance: 24)
                            .onEnded { v in
                                let dx = v.translation.width, dy = v.translation.height
                                if abs(dx) > abs(dy) { setDirection(CGPoint(x: dx > 0 ? 1 : -1, y: 0)) }
                                else { setDirection(CGPoint(x: 0, y: dy > 0 ? 1 : -1)) }
                            }
                    )
                    .onTapGesture {
                        if state != .running { start() }
                    }
                }

                // 方向键：液态玻璃按钮
                GlassEffectContainer {
                    HStack(spacing: 10) {
                        dirButton("◀") { setDirection(CGPoint(x: -1, y: 0)) }
                        dirButton("▲") { setDirection(CGPoint(x: 0, y: -1)) }
                        dirButton("▼") { setDirection(CGPoint(x: 0, y: 1)) }
                        dirButton("▶") { setDirection(CGPoint(x: 1, y: 0)) }
                    }
                }
                .padding(.bottom, 16)
            }
        }
        .onAppear { reset() }
        .onReceive(timer) { _ in
            if state == .running { step() }
        }
    }

    private func dirButton(_ label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.title2)
                .foregroundStyle(.white)
                .frame(width: 68, height: 50)
        }
        .buttonStyle(.glass)
    }

    // MARK: - Logic

    private func reset() {
        snake = (0..<4).map { CGPoint(x: CGFloat(cols / 2 - $0), y: CGFloat(rows / 2)) }
        dir = CGPoint(x: 1, y: 0)
        nextDir = dir
        score = 0
        placeFood()
    }

    private func start() {
        reset()
        state = .running
        restartTimer(interval: 0.15)
    }

    private func restartTimer(interval: Double) {
        timer.upstream.connect().cancel()
        timer = Timer.publish(every: interval, on: .main, in: .common).autoconnect()
    }

    private func setDirection(_ d: CGPoint) {
        if state == .ready { start(); return }
        if state == .over { return }
        if d.x == -dir.x && d.y == -dir.y { return } // no reversing
        nextDir = d
    }

    private func placeFood() {
        while true {
            let p = CGPoint(x: CGFloat(Int.random(in: 0..<cols)), y: CGFloat(Int.random(in: 0..<rows)))
            if !snake.contains(p) { food = p; return }
        }
    }

    private func step() {
        dir = nextDir
        guard let head = snake.first else { return }
        let newHead = CGPoint(x: head.x + dir.x, y: head.y + dir.y)

        let hitsWall = newHead.x < 0 || newHead.x >= CGFloat(cols) || newHead.y < 0 || newHead.y >= CGFloat(rows)
        let bodyToCheck = Array(snake.dropLast())
        if hitsWall || bodyToCheck.contains(newHead) {
            state = .over
            if score > best {
                best = score
                UserDefaults.standard.set(best, forKey: "snakeBest")
            }
            return
        }

        snake.insert(newHead, at: 0)
        if newHead == food {
            score += 10
            placeFood()
            restartTimer(interval: max(0.07, 0.15 - Double(score) * 0.0004))
        } else {
            snake.removeLast()
        }
    }
}

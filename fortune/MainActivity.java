package com.muse.dailyfortune;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

private static final String[][] FORTUNES = {
{"上上签", "宜：大胆开局\n忌：反复横跳", "今天适合把拖了很久的事直接动手，好运偏爱先行动的人。"},
{"大吉", "宜：出门走走\n忌：宅家刷手机", "换个环境就有新灵感，幸运色是天空蓝。"},
{"中吉", "宜：请朋友吃饭\n忌：斤斤计较", "人情往来会有回报，一顿饭能解决的都不是大事。"},
{"小吉", "宜：整理桌面\n忌：冲动下单", "清爽的空间带来清爽的脑子，钱包也能保住。"},
{"上上签", "宜：表白心意\n忌：口是心非", "想说的话今天就说，错过这班车要等很久。"},
{"大吉", "宜：早睡早起\n忌：熬夜赶工", "精神好了，难题自己就松动了。"},
{"中平", "宜：按部就班\n忌：抄近道", "今天没有捷径，但每一步都算数。"},
{"小吉", "宜：学个新技能\n忌：三分钟热度", "哪怕只学十分钟，也是给明天的自己存钱。"},
{"上上签", "宜：投资自己\n忌：自我怀疑", "你比自己以为的更能干，今天适合接个有挑战的活。"},
{"中吉", "宜：给家人打电话\n忌：把忙当借口", "一句问候比红包更暖，幸运数字是 7。"},
{"大吉", "宜：尝试新菜谱\n忌：点同一家外卖", "味蕾的新鲜感会传染给整个心情。"},
{"小吉", "宜：断舍离\n忌：什么都舍不得", "扔掉一件旧物，好运就腾地方进来。"},
{"中平", "宜：先听后说\n忌：抢话打断", "今天贵人藏在对话里，先把耳朵打开。"},
{"上上签", "宜：抬头看天\n忌：低头赶路", "傍晚的云会给你一个答案，别错过。"},
{"大吉", "宜：完成比完美重要\n忌：无限改稿", "先交出去，世界没有你想的那么挑剔。"},
{"小吉", "宜：存一笔小钱\n忌：报复性消费", "小金库长大一点点，安全感就多一点点。"}
};

private TextView resultView;
private final Random random = new Random();

@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);

LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setGravity(Gravity.CENTER);
root.setPadding(64, 64, 64, 64);
root.setBackgroundColor(Color.rgb(250, 246, 238));

TextView title = new TextView(this);
title.setText("每日一签");
title.setTextSize(36);
title.setTypeface(Typeface.DEFAULT_BOLD);
title.setTextColor(Color.rgb(146, 32, 32));
title.setGravity(Gravity.CENTER);
root.addView(title);

TextView date = new TextView(this);
String today = new SimpleDateFormat("yyyy 年 M 月 d 日 EEEE", Locale.CHINA).format(new Date());
date.setText(today);
date.setTextSize(15);
date.setTextColor(Color.rgb(120, 110, 95));
date.setGravity(Gravity.CENTER);
root.addView(date);

resultView = new TextView(this);
resultView.setText("\n点下面的按钮，抽今天的签\n");
resultView.setTextSize(19);
resultView.setTextColor(Color.rgb(60, 55, 45));
resultView.setGravity(Gravity.CENTER);
resultView.setLineSpacing(8, 1.2f);
LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
LinearLayout.LayoutParams.MATCH_PARENT,
LinearLayout.LayoutParams.WRAP_CONTENT);
rp.setMargins(0, 80, 0, 80);
root.addView(resultView, rp);

Button button = new Button(this);
button.setText("抽 一 签");
button.setTextSize(20);
button.setTextColor(Color.WHITE);
button.setBackgroundColor(Color.rgb(178, 44, 44));
button.setPadding(48, 28, 48, 28);
button.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
draw();
}
});
root.addView(button);

setContentView(root);
}

private void draw() {
String[] f = FORTUNES[random.nextInt(FORTUNES.length)];
int lucky = random.nextInt(99) + 1;
resultView.setText("\n\n" + f[1] + "\n\n" + f[2] + "\n\n今日幸运数字：" + lucky);
}
}

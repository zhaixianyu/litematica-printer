package me.aleksilassila.litematica.printer.printer;

import me.aleksilassila.litematica.printer.printer.bedrockUtils.Messager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static me.aleksilassila.litematica.printer.printer.zxy.Utils.ZxyUtils.client;

public class UpdateChecker {
    static boolean updated = false;

    public static void checkForUpdates() {
        if(updated) return;
        updated = true;
//        new Thread(() -> {
            MutableComponent bv1 = Messager.createOpenUrlText("BV1q44y1T7hE", "https://www.bilibili.com/video/BV1q44y1T7hE");
            MutableComponent bv2 = Messager.createOpenUrlText("BV1Fv411P7Vc", "https://www.bilibili.com/video/BV1Fv411P7Vc");
            MutableComponent source = Messager.createOpenUrlText("Github", "https://github.com/aleksilassila/litematica-printer");
            client.gui.getChat().addMessage(
                    Component.literal("").copy().append("[Litematica-Printer]\n此版本为宅闲鱼二改版，初版视频：")
                            .append(bv1)
                            .append("\n投影打印机原作😁：")
                            .append(source)
                            .append("\n破基岩视频：")
                            .append(bv2));
//        }).start();
    }
}

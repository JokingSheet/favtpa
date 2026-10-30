package com.favtpa;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FavScreen extends Screen {
    private static final int ROWS = 8;
    private static final int COL_W = 160;
    private static final int ROW_H = 24;
    private static final int TOP = 50;

    private int favScroll = 0;
    private int onlineScroll = 0;
    private String input = "";
    private int leftX, rightX;

    public FavScreen() {
        super(Text.literal("Favoriten TPA"));
    }

    @Override
    protected void init() {
        leftX = width / 2 - COL_W - 10;
        rightX = width / 2 + 10;

        // ---------- Favoriten (links) ----------
        List<String> favs = Favorites.list();
        favScroll = Math.max(0, Math.min(favScroll, favs.size() - ROWS));
        for (int i = 0; i < ROWS; i++) {
            int idx = favScroll + i;
            if (idx >= favs.size()) break;
            String name = favs.get(idx);
            int y = TOP + i * ROW_H;
            addDrawableChild(ButtonWidget.builder(Text.literal("TPA: " + name), b -> tpa(name))
                    .dimensions(leftX, y, COL_W - 24, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> {
                Favorites.remove(name);
                clearAndInit();
            }).dimensions(leftX + COL_W - 20, y, 20, 20).build());
        }

        // Name manuell hinzufuegen
        int fieldY = TOP + ROWS * ROW_H + 8;
        TextFieldWidget field = new TextFieldWidget(textRenderer, leftX, fieldY, COL_W - 24, 20, Text.literal("Name"));
        field.setMaxLength(16);
        field.setPlaceholder(Text.literal("Name eingeben..."));
        field.setText(input);
        field.setChangedListener(s -> input = s);
        addDrawableChild(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            Favorites.add(input);
            input = "";
            clearAndInit();
        }).dimensions(leftX + COL_W - 20, fieldY, 20, 20).build());

        // ---------- Online-Spieler (rechts) ----------
        List<String> online = onlineNames();
        onlineScroll = Math.max(0, Math.min(onlineScroll, online.size() - ROWS));
        for (int i = 0; i < ROWS; i++) {
            int idx = onlineScroll + i;
            if (idx >= online.size()) break;
            String name = online.get(idx);
            int y = TOP + i * ROW_H;
            addDrawableChild(ButtonWidget.builder(Text.literal("TPA: " + name), b -> tpa(name))
                    .dimensions(rightX, y, COL_W - 24, 20).build());
            ButtonWidget add = ButtonWidget.builder(Text.literal("+"), b -> {
                Favorites.add(name);
                clearAndInit();
            }).dimensions(rightX + COL_W - 20, y, 20, 20).build();
            add.active = !Favorites.contains(name);
            addDrawableChild(add);
        }
    }

    private List<String> onlineNames() {
        List<String> names = new ArrayList<>();
        if (client == null || client.getNetworkHandler() == null) return names;
        String self = client.getSession().getUsername();
        for (PlayerListEntry e : client.getNetworkHandler().getPlayerList()) {
            String n = e.getProfile().name();
            if (!n.equalsIgnoreCase(self)) names.add(n);
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private void tpa(String name) {
        if (client != null && client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand("tpa " + name);
        }
        close();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int dir = verticalAmount > 0 ? -1 : 1;
        if (mouseX < width / 2.0) favScroll += dir;
        else onlineScroll += dir;
        clearAndInit();
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Favoriten"), leftX, 36, 0xFFFFFF55);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Online"), rightX, 36, 0xFF55FF55);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

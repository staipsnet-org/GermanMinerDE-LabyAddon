package de.germanminer.labyaddon.features;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.function.Consumer;

public class GuiTextboxPrompt extends GuiScreen {
    private GuiTextField field;

    private final String title;
    private final String textSubmit;
    private final String textCancel;
    private final String textPlaceholder;
    private final Consumer<String> submitCallback;
    private final Consumer<String> cancelCallback;

    private GuiButton buttonSubmit;
    private GuiButton buttonCancel;

    public GuiTextboxPrompt(String title, String textSubmit, String textCancel, String textPlaceholder, Consumer<String> submitCallback, Consumer<String> cancelCallback) {
        this.title = title;
        this.textSubmit = textSubmit;
        this.textCancel = textCancel;
        this.submitCallback = submitCallback;
        this.cancelCallback = cancelCallback;
        this.textPlaceholder = textPlaceholder;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.field = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, this.width / 2 - 100, this.height / 2, 200, 20);
        this.field.setFocused(true);
        this.field.setMaxStringLength(256);
        this.field.setCursorPositionEnd();
        this.field.setText(textPlaceholder);

        this.buttonSubmit = new GuiButton(1, this.width / 2 + 10, this.height / 2 + 25, 90, 20, this.textSubmit);
        this.buttonCancel = new GuiButton(2, this.width / 2 - 100, this.height / 2 + 25, 90, 20, this.textCancel);

        this.buttonList.add(this.buttonSubmit);
        this.buttonList.add(this.buttonCancel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawWorldBackground(60);
        this.field.setFocused(true);
        this.field.drawTextBox();
        this.buttonSubmit.enabled = !this.field.getText().isEmpty();

        this.drawCenteredMultilineString(Minecraft.getMinecraft().fontRenderer, this.title, (this.width / 2), (this.height / 2 - 25), 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.field.updateCursorCounter();
    }

    public void drawCenteredMultilineString(net.minecraft.client.gui.FontRenderer fontRendererIn, String text, int x, int y, int color) {
        String[] splitText = text.split("\n");
        int lineHeight = fontRendererIn.FONT_HEIGHT + 5;

        y -= (splitText.length - 1) * lineHeight;
        for (String line : splitText) {
            this.drawCenteredString(fontRendererIn, line, x, y, color);
            y += lineHeight;
        }
    }


    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == this.buttonSubmit.id) {
            this.submitCallback.accept(this.field.getText());
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
        }
        if (button.id == this.buttonCancel.id) {
            this.cancelCallback.accept(this.field.getText());
            Minecraft.getMinecraft().displayGuiScreen((GuiScreen) null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            actionPerformed(this.buttonCancel);
            return;
        }
        if (keyCode == 28) { // ENTER
            if (!this.field.getText().isEmpty()) {
                actionPerformed(this.buttonSubmit);
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
        this.field.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.field.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
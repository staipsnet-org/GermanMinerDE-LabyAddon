package de.germanminer.addon.features;

import de.germanminer.addon.GermanMinerAddon;
import com.google.gson.JsonObject;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class GuiTextboxPrompt extends GuiScreen {
    private final String message;
    private final String value;
    private final String textSubmit;
    private final String textCancel;
    private GuiTextField field;
    private GuiButton buttonSubmit;
    private GuiButton buttonCancel;

    public GuiTextboxPrompt(String message, String value, String textSubmit, String textCancel) {
        this.message = message;
        this.value = value;
        this.textSubmit = TextFormatting.WHITE + textSubmit;
        this.textCancel = TextFormatting.WHITE + textCancel;
    }

    public static void initialize() {
        GermanMinerAddon.getInstance().registerMessageConsumer("gmde-input-prompt", jsonObject -> {
            String message = jsonObject.has("message") && jsonObject.get("message").isJsonPrimitive() ? jsonObject.get("message").getAsString() : "";
            String value = jsonObject.has("value") && jsonObject.get("value").isJsonPrimitive() ? jsonObject.get("value").getAsString() : "";
            String textSubmit = jsonObject.has("button_submit") && jsonObject.get("button_submit").isJsonPrimitive() ? jsonObject.get("button_submit").getAsString() : "Bestätigen";
            String textCancel = jsonObject.has("button_cancel") && jsonObject.get("button_cancel").isJsonPrimitive() ? jsonObject.get("button_cancel").getAsString() : "Abbrechen";

            if (message != null && textSubmit != null && textCancel != null) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiTextboxPrompt(message, value, textSubmit, textCancel));
            }
        });
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.field = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, this.width / 2 - 150, this.height / 2, 300, 20);
        this.field.setFocused(true);
        this.field.setMaxStringLength(256);
        this.field.setText(value);

        this.field.setCursorPositionEnd();

        this.buttonSubmit = new GuiButton(1, this.width / 2 + 20, this.height / 2 + 25, 90, 20, this.textSubmit);
        this.buttonCancel = new GuiButton(2, this.width / 2 - 110, this.height / 2 + 25, 90, 20, this.textCancel);

        this.buttonList.add(this.buttonSubmit);
        this.buttonList.add(this.buttonCancel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawWorldBackground(75);
        this.field.setFocused(true);
        this.field.drawTextBox();
        this.buttonSubmit.enabled = !this.field.getText().isEmpty();

        this.drawCenteredMultilineString(Minecraft.getMinecraft().fontRenderer, this.message, (this.width / 2), (this.height / 2 - 25), 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.field.updateCursorCounter();
    }

    public void drawCenteredMultilineString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        String[] splitText = text.split("\n");
        int lineHeight = fontRendererIn.FONT_HEIGHT + 5;

        y -= (splitText.length - 1) * lineHeight;
        for (String line : splitText) {
            this.drawCenteredString(fontRendererIn, TextFormatting.YELLOW + line, x, y, color);
            y += lineHeight;
        }
    }


    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == this.buttonSubmit.id) {
            JsonObject textInput = new JsonObject();
            textInput.addProperty("value", this.field.getText());

            LabyMod.getInstance().getLabyModAPI().sendJsonMessageToServer("gmde-input-prompt", textInput);
            Minecraft.getMinecraft().displayGuiScreen(null);
        } else if (button.id == this.buttonCancel.id) {
            JsonObject textInput = new JsonObject();
            textInput.addProperty("exit", true);

            LabyMod.getInstance().getLabyModAPI().sendJsonMessageToServer("gmde-input-prompt", textInput);
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }



    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            Minecraft.getMinecraft().displayGuiScreen(this);
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
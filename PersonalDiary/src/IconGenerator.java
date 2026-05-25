import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class IconGenerator {
    public static ImageIcon createDiaryIcon() {
        int size = 128;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Настройка сглаживания
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон
        g2d.setColor(new Color(52, 152, 219));
        g2d.fillRoundRect(0, 0, size, size, 20, 20);

        // Обложка дневника
        g2d.setColor(new Color(41, 128, 185));
        g2d.fillRoundRect(20, 20, 88, 88, 10, 10);

        // Заголовок
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Дневник", 25, 65);

        // Линии для текста
        g2d.setColor(new Color(236, 240, 241));
        for (int i = 0; i < 3; i++) {
            g2d.fillRect(30, 80 + i * 15, 70, 3);
        }

        // Звёздочка (оценка)
        g2d.setColor(new Color(241, 196, 15));
        g2d.fillRoundRect(85, 35, 25, 25, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("5", 93, 55);

        g2d.dispose();

        // Сохраняем иконку
        try {
            File outputfile = new File("diary_icon.png");
            ImageIO.write(image, "png", outputfile);
            System.out.println("Иконка создана: diary_icon.png");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ImageIcon(image);
    }
}
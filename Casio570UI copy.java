import java.awt.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import javax.swing.*;

/** Máy Casio fx-580VN X ảo. Chạy: java Casio570UI */
public class Casio570UI   extends JFrame {
    private final casio570 calculator = new casio570();
    private final JTextField entry = new JTextField();
    private final JLabel lcdTop = new JLabel();
    private final JLabel lcdBottom = new JLabel();
    private double answer;
    private boolean shift;
    private ScreenMode screenMode = ScreenMode.COMPUTE;
    private final double[] equation = new double[3];
    private int equationIndex;
    private enum ScreenMode { COMPUTE, MODE_MENU, FUNCTION_MENU, EQUATION, STATISTICS }

    public Casio570UI() {
        super("CASIO fx-580VN X - Máy tính ảo");
        setDefaultCloseOperation(EXIT_ON_CLOSE); setResizable(false);
        setContentPane(createCalculator()); setMinimumSize(new Dimension(420, 760));
        pack(); setLocationRelativeTo(null); showComputeInfo("Sẵn sàng tính toán");
    }

    private JComponent createCalculator() {
        JPanel backdrop = new JPanel(new GridBagLayout()); backdrop.setBackground(new Color(35, 37, 43));
        JPanel machine = new JPanel(null) {
            @Override protected void paintComponent(Graphics raw) {
                super.paintComponent(raw); Graphics2D g = (Graphics2D) raw.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(25, 27, 33)); g.fillRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 42, 42);
                g.setColor(new Color(58, 61, 68)); g.fillRoundRect(14, 14, getWidth() - 28, getHeight() - 28, 36, 36);
                g.setColor(new Color(129, 132, 139)); g.drawRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 42, 42);
                g.setColor(new Color(20, 38, 35)); g.fillRoundRect(35, 100, getWidth() - 70, 102, 10, 10);
                g.setColor(new Color(156, 178, 160)); g.drawRoundRect(35, 100, getWidth() - 70, 102, 10, 10); g.dispose();
            }
        };
        machine.setOpaque(false); machine.setPreferredSize(new Dimension(400, 710));
        label(machine, "CASIO", 43, 30, 115, 24, new Font(Font.SANS_SERIF, Font.BOLD, 19), new Color(238, 238, 238));
        label(machine, "fx-580VN X", 261, 34, 105, 18, new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 12), new Color(222, 222, 222));
        label(machine, "CLASSWIZ", 149, 61, 105, 17, new Font(Font.SANS_SERIF, Font.BOLD, 12), new Color(84, 190, 161));
        label(machine, "SHIFT", 43, 214, 46, 11, new Font(Font.SANS_SERIF, Font.BOLD, 8), new Color(238, 195, 76));
        label(machine, "ALPHA", 94, 214, 46, 11, new Font(Font.SANS_SERIF, Font.BOLD, 8), new Color(242, 103, 104));
        lcdTop.setBounds(50, 111, 300, 18); lcdTop.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11)); lcdTop.setForeground(new Color(220, 240, 219)); machine.add(lcdTop);
        lcdBottom.setBounds(50, 130, 300, 17); lcdBottom.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10)); lcdBottom.setForeground(new Color(189, 215, 190)); machine.add(lcdBottom);
        entry.setBounds(50, 151, 300, 40); entry.setFont(new Font(Font.MONOSPACED, Font.BOLD, 19)); entry.setForeground(new Color(236, 250, 234)); entry.setBackground(new Color(20, 38, 35)); entry.setCaretColor(new Color(236, 250, 234)); entry.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4)); entry.setHorizontalAlignment(JTextField.RIGHT); entry.addActionListener(event -> equalsPressed()); machine.add(entry);
        JPanel keypad = new JPanel(new GridLayout(0, 5, 7, 7)); keypad.setOpaque(false); keypad.setBounds(39, 235, 322, 422);
        String[][] rows = {{"SHIFT","ALPHA","MODE","FUNC","DEL"},{"sin","cos","tan","log","ln"},{"x²","x^y","√","1/x","x!"},{"(",")","π","e","Ans"},{"MC","MR","STO","M+","ENG"},{"7","8","9","÷","AC"},{"4","5","6","×","^"},{"1","2","3","-","+"},{"0",".",";","+/-","="}};
        for (String[] row : rows) for (String caption : row) keypad.add(key(caption)); machine.add(keypad); backdrop.add(machine); return backdrop;
    }

    private void label(JPanel panel, String text, int x, int y, int width, int height, Font font, Color color) { JLabel result = new JLabel(text); result.setBounds(x, y, width, height); result.setFont(font); result.setForeground(color); panel.add(result); }
    private JButton key(String caption) {
        JButton button = new JButton(caption); button.setFocusPainted(false); button.setMargin(new Insets(1, 1, 1, 1)); button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, caption.length() > 4 ? 9 : 12));
        Color color = new Color(54, 58, 64); if (caption.matches("[0-9.]")) color = new Color(231, 233, 232); else if (caption.equals("AC")) color = new Color(229, 109, 52); else if (caption.equals("=")) color = new Color(53, 142, 116); else if ("÷×+-^".contains(caption)) color = new Color(186, 192, 195); else if (caption.equals("SHIFT")) color = new Color(126, 101, 47); else if (caption.equals("ALPHA")) color = new Color(126, 65, 70);
        button.setBackground(color); button.setForeground(color.getRed() < 150 ? Color.WHITE : new Color(28, 30, 34)); button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(15, 16, 20), 1, true), BorderFactory.createEmptyBorder(1, 1, 1, 1))); button.setToolTipText(toolTip(caption)); button.addActionListener(event -> press(caption)); return button;
    }

    private void press(String key) {
        if (screenMode == ScreenMode.MODE_MENU) { chooseMode(key); return; }
        if (screenMode == ScreenMode.FUNCTION_MENU) { chooseFunction(key); return; }
        switch (key) {
            case "=" -> equalsPressed(); case "AC" -> reset(); case "DEL" -> delete();
            case "SHIFT" -> { shift = !shift; lcdBottom.setText(shift ? "SHIFT đang bật" : "SHIFT đã tắt"); }
            case "ALPHA" -> lcdBottom.setText("ALPHA: dùng M, Ans, π và e"); case "MODE" -> showModeMenu(); case "FUNC" -> showFunctionMenu();
            case "MC" -> { calculator.memoryClear(); showComputeInfo("Đã xóa bộ nhớ M"); } case "MR" -> insert("M"); case "M+" -> memoryAdd(); case "STO" -> memoryStore();
            case "Ans" -> insert("Ans"); case "π" -> insert("pi"); case "x²" -> insert(shift ? "^3" : "^2"); case "x^y" -> insert(shift ? "root(" : "^"); case "√" -> insert(shift ? "cbrt(" : "sqrt("); case "1/x" -> wrap("inv("); case "x!" -> insert(shift ? "nCr(" : "!");
            case "log" -> insert(shift ? "logbase(" : "log("); case "ln" -> insert(shift ? "exp(" : "ln("); case "sin" -> insert(shift ? "asin(" : "sin("); case "cos" -> insert(shift ? "acos(" : "cos("); case "tan" -> insert(shift ? "atan(" : "tan("); case "ENG" -> insert("*10^"); case "×" -> insert("*"); case "÷" -> insert("/"); case "+/-" -> insert("-"); default -> insert(key);
        }
        if (!key.equals("SHIFT")) shift = false;
    }

    private void equalsPressed() {
        if (screenMode == ScreenMode.EQUATION) { captureEquationCoefficient(); return; } if (screenMode == ScreenMode.STATISTICS) { calculateStatistics(); return; }
        try { String source = entry.getText().trim(); if (source.isEmpty()) return; answer = calculator.evaluate(source, answer); entry.setText(format(answer)); showComputeInfo(source + " = " + format(answer)); } catch (ArithmeticException error) { showError(error.getMessage()); }
    }
    private void showModeMenu() { screenMode = ScreenMode.MODE_MENU; entry.setText(""); lcdTop.setText("MODE: chọn một chế độ"); lcdBottom.setText("1:COMP  2:DEG  3:RAD  4:GRAD  5:EQN  6:STAT"); }
    private void chooseMode(String choice) {
        switch (choice) { case "1" -> { screenMode = ScreenMode.COMPUTE; showComputeInfo("COMP: tính biểu thức"); } case "2" -> { calculator.setAngleUnit(casio570.AngleUnit.DEGREE); screenMode = ScreenMode.COMPUTE; showComputeInfo("Đơn vị góc: DEG"); } case "3" -> { calculator.setAngleUnit(casio570.AngleUnit.RADIAN); screenMode = ScreenMode.COMPUTE; showComputeInfo("Đơn vị góc: RAD"); } case "4" -> { calculator.setAngleUnit(casio570.AngleUnit.GRADIAN); screenMode = ScreenMode.COMPUTE; showComputeInfo("Đơn vị góc: GRAD"); } case "5" -> startEquation(); case "6" -> startStatistics(); default -> lcdBottom.setText("Hãy bấm từ 1 đến 6"); }
    }
    private void showFunctionMenu() { screenMode = ScreenMode.FUNCTION_MENU; entry.setText(""); lcdTop.setText("FUNC: hàm bổ sung"); lcdBottom.setText("1:nPr 2:nCr 3:abs 4:sinh 5:cosh 6:tanh"); }
    private void chooseFunction(String choice) {
        String function = switch (choice) { case "1" -> "nPr("; case "2" -> "nCr("; case "3" -> "abs("; case "4" -> "sinh("; case "5" -> "cosh("; case "6" -> "tanh("; default -> null; };
        if (function == null) { lcdBottom.setText("Hãy bấm từ 1 đến 6"); return; } screenMode = ScreenMode.COMPUTE; entry.setText(function); showComputeInfo("Nhập đối số; ngăn cách bằng ; nếu cần");
    }
    private void startEquation() { screenMode = ScreenMode.EQUATION; equationIndex = 0; entry.setText(""); lcdTop.setText("EQN: ax² + bx + c = 0"); lcdBottom.setText("Nhập hệ số a rồi bấm ="); }
    private void captureEquationCoefficient() {
        try { equation[equationIndex++] = Double.parseDouble(entry.getText().trim().replace(',', '.')); entry.setText(""); if (equationIndex < 3) { lcdBottom.setText("Nhập hệ số " + (equationIndex == 1 ? "b" : "c") + " rồi bấm ="); return; } double[] roots = calculator.solveQuadratic(equation[0], equation[1], equation[2]); String result = roots.length == 0 ? "Không có nghiệm thực" : roots.length == 1 ? "x=" + format(roots[0]) : "x1=" + format(roots[0]) + "  x2=" + format(roots[1]); entry.setText(result); screenMode = ScreenMode.COMPUTE; showComputeInfo("EQN: " + result); } catch (NumberFormatException error) { lcdBottom.setText("Hệ số phải là một số hợp lệ"); }
    }
    private void startStatistics() { screenMode = ScreenMode.STATISTICS; entry.setText(""); lcdTop.setText("STAT: trung bình và độ lệch chuẩn"); lcdBottom.setText("Nhập dãy số cách nhau bằng ; rồi bấm ="); }
    private void calculateStatistics() {
        try { String[] values = entry.getText().trim().split(";"); if (values.length == 0 || values[0].isBlank()) throw new NumberFormatException(); double sum = 0, squares = 0; for (String value : values) { double x = Double.parseDouble(value.trim().replace(',', '.')); sum += x; squares += x * x; } double mean = sum / values.length; double sigma = Math.sqrt(Math.max(0, squares / values.length - mean * mean)); entry.setText("mean=" + format(mean) + " sd=" + format(sigma)); screenMode = ScreenMode.COMPUTE; showComputeInfo("STAT: n=" + values.length); } catch (NumberFormatException error) { lcdBottom.setText("Dãy số không hợp lệ; ví dụ: 2;4;6"); }
    }
    private void memoryStore() { try { calculator.memoryStore(calculator.evaluate(entry.getText(), answer)); showComputeInfo("Đã lưu vào M"); } catch (ArithmeticException error) { showError(error.getMessage()); } }
    private void memoryAdd() { try { calculator.memoryAdd(calculator.evaluate(entry.getText(), answer)); showComputeInfo("Đã cộng vào M"); } catch (ArithmeticException error) { showError(error.getMessage()); } }
    private void reset() { entry.setText(""); shift = false; screenMode = ScreenMode.COMPUTE; showComputeInfo("Sẵn sàng tính toán"); }
    private void insert(String text) { entry.replaceSelection(text); entry.requestFocusInWindow(); }
    private void wrap(String function) { entry.setText(function + entry.getText() + ")"); entry.requestFocusInWindow(); }
    private void delete() { int position = entry.getCaretPosition(); if (position > 0) { String text = entry.getText(); entry.setText(text.substring(0, position - 1) + text.substring(position)); entry.setCaretPosition(position - 1); } }
    private void showComputeInfo(String message) { String unit = switch (calculator.getAngleUnit()) { case DEGREE -> "DEG"; case RADIAN -> "RAD"; case GRADIAN -> "GRAD"; }; lcdTop.setText(unit + (shift ? "  SHIFT" : "") + "    M=" + format(calculator.memoryRecall())); lcdBottom.setText(message); }
    private void showError(String message) { lcdTop.setText("Math ERROR"); lcdBottom.setText(message); }
    private static String format(double value) { if (value == Math.rint(value) && Math.abs(value) < 1e15) return String.format(Locale.US, "%.0f", value); return BigDecimal.valueOf(value).round(new MathContext(12)).stripTrailingZeros().toPlainString(); }
    private static String toolTip(String key) { return switch (key) { case "MODE" -> "Chế độ COMP, DEG/RAD, EQN, STAT"; case "FUNC" -> "Tổ hợp, chỉnh hợp và hàm hyperbolic"; case "SHIFT" -> "Bật chức năng vàng của phím kế tiếp"; case "ENG" -> "Chèn ×10^"; default -> key; }; }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new Casio570UI().setVisible(true)); }
}
